package cloud.benchflow.experimentsmanager.resources.lifecycle.v2;

import cloud.benchflow.experimentsmanager.db.ExperimentsDAO;
import cloud.benchflow.experimentsmanager.db.DbManager;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import cloud.benchflow.experimentsmanager.exceptions.DriverGenerationException;
import cloud.benchflow.experimentsmanager.exceptions.ExperimentRunException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.ExperimentIdResponse;
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
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/03/16.
 */
@Path("run")
public class RunExperimentResource {

    private static Logger logger = LoggerFactory.getLogger(RunExperimentResource.class.getName());

    private BenchFlowMinioClient minio;
    private DbManager db;
    private FabanClient faban;
    private DriversMaker driversMaker;
    private ExecutorService runBenchmarkPool;
    private ExecutorService submitRunsPool;
    private Integer submitRetries;

    @Inject
    public RunExperimentResource(@Named("minio") BenchFlowMinioClient minio,
                                 @Named("db") DbManager db,
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
        this.submitRetries = submitRetries;
        this.submitRunsPool = submitRunsPool;
    }


    private class AsyncRunExperiment implements Runnable {

        private Experiment experiment;
        private ExperimentsDAO experimentsDAO;

        public AsyncRunExperiment(Experiment experiment,
                                  ExperimentsDAO experimentsDAO) {
            this.experiment = experiment;
            this.experimentsDAO = experimentsDAO;
        }

        @Override
        public void run() {

            try {
                String benchmarkName = experiment.getBenchmarkName();
                String benchmarkId = experiment.getBenchmarkId();
                String minioBenchmarkId = experiment.getUsername() + "/" + benchmarkName;
                long experimentNumber = experiment.getExperimentNumber();

                driversMaker.generateDriver(benchmarkName, experiment.getExperimentNumber(), experiment.getTrials().size());
                logger.debug("Generated Faban driver");

                experiment.setQueued();

                InputStream driver = minio.getGeneratedDriver(minioBenchmarkId, experimentNumber);
                faban.deploy(driver, experiment.getExperimentId());
                logger.debug("Driver successfully deployed");

                //send the runs to faban
                CompletionService<Trial> cs = new ExecutorCompletionService<>(submitRunsPool);

                //make concurrent run requests to faban
                for (Trial t : experiment.getTrials()) {

                    cs.submit(() -> {
                        int retries = submitRetries;
                        String config = minio.getFabanConfiguration(benchmarkId, experimentNumber, t.getTrialNumber());

                        RunId runId = null;
                        while (runId == null) {
                            try {
//                                runId = new RunId(benchmarkName,"foo");
                                runId = faban.submit(experiment.getExperimentId(), t.getTrialId(),
                                        IOUtils.toInputStream(config, Charsets.UTF_8));
                            } catch (FabanClientException e) {
                                if (retries > 0) retries--;
                                else {
                                    throw e;
                                }
                            }
                        }
                        t.setFabanRunId(runId.toString());
                        t.setSubmitted();
                        return t;
                    });
                }

                int received = 0;
                while (received < experiment.getTrials().size()) {
                    //TODO: handle the case in which some trials fails to submit
                    Future<Trial> updatedTrialResponse = cs.take();
                    Trial updatedTrial = updatedTrialResponse.get();
                    updatedTrial.setSubmitted();
                    experimentsDAO.update(updatedTrial);
                    logger.debug("Received trial " + updatedTrial.getTrialNumber() +
                                 "with run ID: " + updatedTrial.getFabanRunId());
                    received++;
                }

                experiment.setRunning();

            } catch(Exception e) {
                experiment.setAborted();
                //TODO: set all trials as aborted, if any
                //check if any of them was queued on faban, to kill it
                experimentsDAO.update(experiment);
                throw new ExperimentRunException(e.getMessage(), e);
            } finally {
                experimentsDAO.close();
            }
        }
    }

    //TODO: in the future the run api will receive both descriptors directly
    //TODO: it will first generate a uuid for the experiment
    //TODO: and then save the files on minio
    @POST
    @Path("{benchmarkName}")
    @Produces("application/vnd.experiments-manager.v2+json")
    public ExperimentIdResponse runAsync(@PathParam("benchmarkName") String benchmarkName) {

        String user = "BenchFlow";
        String minioBenchmarkId = user + "/" + benchmarkName;
        Experiment experiment;

        ExperimentsDAO experimentsDAO = db.getExperimentsDAO();
        String bb = minio.getOriginalBenchFlowBenchmark(minioBenchmarkId);

        Map<String, Object> parsedDD = (Map<String, Object>) new Yaml().load(bb);
        int trials = (Integer) parsedDD.get("trials");

        experiment = new Experiment(user, benchmarkName);
        String benchmarkId = experiment.getBenchmarkId();

        logger.debug("Retrieved number of trials for benchmark " + benchmarkId + ": " + trials);

        for (int i = 1; i <= trials; i++) {
            Trial trial = new Trial(i);
            experiment.addTrial(trial);
        }

        experimentsDAO.saveExperiment(experiment);
        logger.debug("Saved experiment");

        try {
           runBenchmarkPool.submit(new AsyncRunExperiment(experiment, experimentsDAO));
        } catch(Exception e) {
            //leaves the database in a consistent state
            //and reports the exception so that we can investigate
            experimentsDAO.cleanUp(experiment);
            throw new WebApplicationException(e.getMessage(), e);
        }

        return new ExperimentIdResponse(experiment.getExperimentId(), experiment.getTrials().size());
    }

}