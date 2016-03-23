package cloud.benchflow.experimentsmanager.resources.lifecycle.v1;

import cloud.benchflow.experimentsmanager.db.DbSession;
import cloud.benchflow.experimentsmanager.db.DbSessionManager;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkRunException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchBenchmarkException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.TrialIdResponse;
import cloud.benchflow.experimentsmanager.utils.DriversMaker;
import cloud.benchflow.experimentsmanager.utils.minio.v1.MinioHandler;

import cloud.benchflow.experimentsmanager.utils.minio.v2.BenchFlowMinioClientException;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;

import cloud.benchflow.faban.client.responses.RunId;
import com.google.inject.Inject;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.inject.name.Named;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 04/12/15.
 */
@Path("/run")
public class RunBenchmarkResource {

    private final MinioHandler mh;
    private final DriversMaker dm;
    private final FabanClient fc;
    private final DbSession db;

    @Inject
    public RunBenchmarkResource(@Named("minio.v1") final MinioHandler mh,
                                @Named("drivers.maker") final DriversMaker  dm,
                                @Named("faban") final FabanClient fc,
                                @Named("db") final DbSessionManager db) {
        this.mh = mh;
        this.dm = dm;
        this.fc = fc;
        this.db = db.getSession();
    }

    @POST
    @Path("{benchmarkName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/vnd.experiments-manager.v1+json")
    public TrialIdResponse runBenchmark(@PathParam("benchmarkName") String benchmarkName,
                                        @DefaultValue("null") @FormDataParam("benchflow-benchmark")
                                        InputStream configInputStream,
                                        @DefaultValue("null") @FormDataParam("benchflow-benchmark")
                                        FormDataContentDisposition configDetail) {

        try {
            if(configInputStream == null) {
                //retrieve it from minio
                configInputStream = mh.getConfig(benchmarkName);
            }
            InputStream converted = dm.convert(configInputStream);

            //create the experiment
            Experiment e = new Experiment("BenchFlow", benchmarkName);
            RunId rs = fc.submit(benchmarkName, benchmarkName, converted);

            //for now, we only have one trial
            Trial t = new Trial(1);
            t.setFabanRunId(rs.toString());
            e.addTrial(t);

            //save the experiment in the database
            db.saveExperiment(e);

            //return the id for the trial
            return new TrialIdResponse(t.getExperiment().getBenchmarkName(),
                                       t.getExperiment().getExperimentNumber(),
                                       t.getTrialNumber());
        } catch (BenchmarkNameNotFoundException e) {
            throw new NoSuchBenchmarkException(benchmarkName);
        } catch (FabanClientException | BenchFlowMinioClientException e) {
            throw new BenchmarkRunException(e.getMessage(), e);
        }

    }

}
