package cloud.benchflow.experimentsmanager.resources.lifecycle.v2;

import cloud.benchflow.experimentsmanager.db.DbSession;
import cloud.benchflow.experimentsmanager.db.DbSessionManager;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkGenerationException;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkRunException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.DeployStatusResponse;
import cloud.benchflow.experimentsmanager.utils.DriversMaker;
import cloud.benchflow.experimentsmanager.utils.minio.v2.BenchFlowMinioClient;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.responses.RunId;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/03/16.
 */
@Path("run")
public class RunBenchmarkResource {


    private BenchFlowMinioClient minio;
    private Logger logger;
    private DbSessionManager db;
    private FabanClient faban;
    private DriversMaker driversMaker;
    private ExecutorService runBenchmarkPool;
    private ExecutorService submitRunsPool;
    private Integer submitRetries;

    @Inject
    public RunBenchmarkResource(@Named("minio.v2") BenchFlowMinioClient minio,
                                @Named("db") DbSessionManager db,
                                @Named("faban") FabanClient faban,
                                @Named("retries") Integer submitRetries,
                                @Named("drivers-maker") DriversMaker driversMaker,
                                @Named("runBenchmarkExecutorService") ExecutorService runBenchmarkPool,
                                @Named("submitRunExecutorService") ExecutorService submitRunsPool) {
        this.minio = minio;
        this.db = db;
        this.faban = faban;
        this.driversMaker = driversMaker;
        this.runBenchmarkPool = runBenchmarkPool;
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
        this.submitRetries = submitRetries;
        this.submitRunsPool = submitRunsPool;
    }


    private class AsyncExperimentRun implements Runnable {

        private Experiment experiment;
        private DbSession dbSession;

        public AsyncExperimentRun(Experiment experiment, DbSession dbSession) {
            this.experiment = experiment;
            this.dbSession = dbSession;
        }

        @Override
        public void run() {

            try {
                String benchmarkName = experiment.getBenchmarkName();
//                String benchmarkId = experiment.getUsername() + "." + benchmarkName;
                String benchmarkId = experiment.getBenchmarkId();
                String minioBenchmarkId = experiment.getUsername() + "/" + benchmarkName;
                long experimentNumber = experiment.getExperimentNumber();

                driversMaker.generateDriver(benchmarkName, experiment.getExperimentNumber(), experiment.getTrials().size());
                logger.debug("Generated Faban driver");

                InputStream driver = minio.getGeneratedDriver(minioBenchmarkId, experimentNumber);

                faban.deploy(driver, experiment.getExperimentId());
                logger.debug("Driver successfully deployed");

                //experiment.status = RUNNING

                //send the runs to faban
                CompletionService<Trial> cs = new ExecutorCompletionService<>(submitRunsPool);

                //make concurrent run requests to faban
                for (Trial t : experiment.getTrials()) {

                    //trial.status = QUEUED

                    cs.submit(() -> {
                        int retries = submitRetries;
                        String config = minio.getFabanConfiguration(benchmarkId, experimentNumber, t.getTrialNumber());

                        RunId runId = null;
                        while (runId == null) {
                            try {
                                runId = faban.submit(experiment.getExperimentId(), t.getTrialId(),
                                        IOUtils.toInputStream(config, Charsets.UTF_8));
//                                break;
                            } catch (FabanClientException e) {
                                if (retries > 0) retries--;
                                else {
                                    throw e;
                                }
                            }
                        }

                        t.setFabanRunId(runId.toString());
                        return t;
                    });
                }

                int received = 0;
                while (received < experiment.getTrials().size()) {

                    //TODO: handle the case in which some trials fails to submit
                    Future<Trial> updatedTrialResponse = cs.take();
                    Trial updatedTrial = updatedTrialResponse.get();

                    //trial.status = SUBMITTED
                    updatedTrial.setSubmitted();

                    dbSession.update(updatedTrial);

                    logger.debug("Received trial " + updatedTrial.getTrialNumber() +
                            "with run ID: " + updatedTrial.getFabanRunId());

                    received++;

                }

                //update the trials
                //dbSession.updateTrials(experiment.getTrials());

            } catch(Exception e) {
                dbSession.rollback();
                throw new BenchmarkRunException(e.getMessage(), e);
            } finally {
                dbSession.close();
            }
        }
    }

    @POST
    @Path("{benchmarkName}")
    @Produces("application/vnd.experiments-manager.v2+json")
    public String runAsync(@PathParam("benchmarkName") String benchmarkName) {

        String user = "BenchFlow";
//        String benchmarkId = user + "." + benchmarkName;
        String minioBenchmarkId = user + "/" + benchmarkName;
        Experiment experiment;
        //experiment.status = GENERATING

        DbSession dbSession = db.getSession();
        String dd = minio.getOriginalDeploymentDescriptor(minioBenchmarkId);

        Map<String, Object> parsedDD = (Map<String, Object>) new Yaml().load(dd);
        int trials = Integer.valueOf((String) parsedDD.get("trials"));

        experiment = new Experiment(user, benchmarkName);
        String benchmarkId = experiment.getBenchmarkId();

        logger.debug("Retrieved number of trials for benchmark " + benchmarkId + ": " + trials);

        for (int i = 1; i <= trials; i++) {
            Trial trial = new Trial(i);
            experiment.addTrial(trial);
        }

        dbSession.saveExperiment(experiment);
        logger.debug("Saved experiment");

        try {
           runBenchmarkPool.submit(new AsyncExperimentRun(experiment, dbSession));
        } catch(Exception e) {
            dbSession.rollback();
            throw new WebApplicationException(e.getMessage(), e);
        }

        //TODO: chose more appropriate response (e.g, experimentId + totalTrials)
        return experiment.getExperimentId();
    }

//    @POST
//    @Path("{benchmarkName}")
//    @Produces("application/vnd.experiments-manager.v2+json")
//    public DeployStatusResponse run(@PathParam("benchmarkName") String benchmarkName) throws IOException {
//
//        String user = "BenchFlow";
//        String benchmarkId = user + "/" + benchmarkName;
//
//        try(DbSession dbSession = db.getSession()) {
//
//            String dd = minio.getOriginalDeploymentDescriptor(benchmarkId);
//            Map<String, Object> parsedDD = (Map<String, Object>) new Yaml().load(dd);
//            int trials = Integer.valueOf((String) parsedDD.get("trials"));
//
//            logger.debug("Retrieved number of trials for benchmark " + benchmarkId + ": " + trials);
//
//            Experiment experiment = new Experiment(user, benchmarkName);
//            for (int i = 1; i <= trials; i++) {
//                Trial trial = new Trial(i);
//                experiment.addTrial(trial);
//            }
//
//            dbSession.saveExperiment(experiment);
//
//            logger.debug("Saved experiment");
//
//            //ask drivers-maker to generate the packet
//            try {
//                driversMaker.generateDriver(benchmarkName, experiment.getExperimentNumber(), experiment.getTrials().size());
//            } catch (BenchmarkGenerationException e) {
//                dbSession.rollback();
//                throw new BenchmarkRunException(e.getMessage(), e);
//            }
//
//            logger.debug("Generated Faban driver");
//
//            long experimentNumber = experiment.getExperimentNumber();
//
//            //get the packet from minio
//            InputStream driver = minio.getGeneratedDriver(benchmarkId, experimentNumber);
//
//            //deploy the driver
//            try {
//                faban.deploy(driver, experiment.getExperimentId());
//            } catch (FabanClientException e) {
//                dbSession.rollback();
//                throw new BenchmarkRunException(e.getMessage(), e);
//            }
//
//            logger.debug("Driver successfully deployed");
//
//            //send the runs to faban
//            CompletionService<Trial> cs = new ExecutorCompletionService<>(runBenchmarkPool);
//
//            //make concurrent run requests to faban
//            for (Trial t : experiment.getTrials()) {
//                cs.submit(() -> {
//                    int retries = submitRetries;
//                    String config = minio.getFabanConfiguration(benchmarkId, experimentNumber, t.getTrialNumber());
//
//                    RunId runId;
//                    while(true) {
//                        try {
//                            runId = faban.submit(experiment.getExperimentId(), experiment.getExperimentId(),
//                                                 IOUtils.toInputStream(config, Charsets.UTF_8));
//                            break;
//                        } catch(FabanClientException e) {
//                            if(retries > 0) retries--;
//                            else {
//                                dbSession.rollback();
//                                throw new BenchmarkRunException(e.getMessage(), e);
//                            }
//                        }
//                    }
//
//                    t.setFabanRunId(runId.toString());
//                    return t;
//                });
//            }
//
//            //TODO: the same can probably be done with a CountDownLatch
//            int received = 0;
//            while (received < experiment.getTrials().size()) {
//
//                try {
//                    Future<Trial> updatedTrialResponse = cs.take();
//                    Trial updatedTrial = updatedTrialResponse.get();
//
//                    logger.debug("Received trial " + updatedTrial.getTrialNumber() +
//                                 "with run ID: " + updatedTrial.getFabanRunId());
//
//                    received++;
//
//                } catch (InterruptedException | ExecutionException e) {
//                    dbSession.rollback();
//                    throw new BenchmarkRunException(e.getMessage(), e);
//                }
//
//            }
//
//            //update the trials
//            dbSession.updateTrials(experiment.getTrials());
//
//        }
//
//        catch(Exception e) {
//            //decide what to do here
//        }
//
//        //TODO: return the experiment id here
//        return new DeployStatusResponse("Deployed");
//    }

}
