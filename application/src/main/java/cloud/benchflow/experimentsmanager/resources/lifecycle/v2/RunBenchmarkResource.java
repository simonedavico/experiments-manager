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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    private ExecutorService threadPool;
    private Integer submitRetries;

    @Inject
    public RunBenchmarkResource(@Named("minio.v2") BenchFlowMinioClient minio,
                                @Named("db") DbSessionManager db,
                                @Named("faban") FabanClient faban,
                                @Named("retries") Integer submitRetries,
                                @Named("drivers-maker") DriversMaker driversMaker,
                                @Named("executorService") ExecutorService threadPool) {
        this.minio = minio;
        this.db = db;
        this.faban = faban;
        this.driversMaker = driversMaker;
        this.threadPool = threadPool;
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
    }

    @POST
    @Path("{benchmarkName}")
    @Produces("application/vnd.experiments-manager.v2+json")
    public DeployStatusResponse run(@PathParam("benchmarkName") String benchmarkName) throws IOException {

        String user = "BenchFlow";
        String benchmarkId = user + "/" + benchmarkName;

        try(DbSession dbSession = db.getSession()) {

            String dd = minio.getOriginalDeploymentDescriptor(benchmarkId);
            Map<String, Object> parsedDD = (Map<String, Object>) new Yaml().load(dd);
            int trials = Integer.valueOf((String) parsedDD.get("trials"));

            logger.debug("Retrieved number of trials for benchmark " + benchmarkId + ": " + trials);

            Experiment experiment = new Experiment(user, benchmarkName);
            for (int i = 1; i <= trials; i++) {
                Trial trial = new Trial(i);
                experiment.addTrial(trial);
            }

            dbSession.saveExperiment(experiment);

            logger.debug("Saved experiment");

            //ask drivers-maker to generate the packet
            try {
                driversMaker.generateDriver(benchmarkName, experiment.getExperimentNumber(), experiment.getTrials().size());
            } catch (BenchmarkGenerationException e) {
                dbSession.rollback();
                throw new BenchmarkRunException(e.getMessage(), e);
            }

            logger.debug("Generated Faban driver");

            long experimentNumber = experiment.getExperimentNumber();

            //get the packet from minio
            InputStream driver = minio.getGeneratedDriver(benchmarkId, experimentNumber);

            //deploy the driver
            try {
                faban.deploy(driver, experiment.getExperimentId());
            } catch (FabanClientException e) {
                dbSession.rollback();
                throw new BenchmarkRunException(e.getMessage(), e);
            }

            logger.debug("Driver successfully deployed");

            //send the runs to faban
            CompletionService<Trial> cs = new ExecutorCompletionService<>(threadPool);

            //make concurrent run requests to faban
            for (Trial t : experiment.getTrials()) {
                cs.submit(() -> {
                    int retries = submitRetries;
                    String config = minio.getFabanConfiguration(benchmarkId, experimentNumber, t.getTrialNumber());

                    RunId runId;
                    while(true) {
                        try {
                            runId = faban.submit(experiment.getExperimentId(), experiment.getExperimentId(),
                                                 IOUtils.toInputStream(config, Charsets.UTF_8));
                            break;
                        } catch(FabanClientException e) {
                            if(retries > 0) retries--;
                            else {
                                dbSession.rollback();
                                throw new BenchmarkRunException(e.getMessage(), e);
                            }
                        }
                    }

                    t.setFabanRunId(runId.toString());
                    return t;
                });
            }

            //TODO: the same can probably be done with a CountDownLatch
            int received = 0;
            while (received < experiment.getTrials().size()) {

                try {
                    Future<Trial> updatedTrialResponse = cs.take();
                    Trial updatedTrial = updatedTrialResponse.get();

                    logger.debug("Received trial " + updatedTrial.getTrialNumber() +
                                 "with run ID: " + updatedTrial.getFabanRunId());

                    received++;

                } catch (InterruptedException | ExecutionException e) {
                    dbSession.rollback();
                    throw new BenchmarkRunException(e.getMessage(), e);
                }

            }

            //update the trials
            dbSession.updateTrials(experiment.getTrials());

        }

        catch(Exception e) {
            //decide what to do here
        }

        return new DeployStatusResponse("Deployed");
    }



}
