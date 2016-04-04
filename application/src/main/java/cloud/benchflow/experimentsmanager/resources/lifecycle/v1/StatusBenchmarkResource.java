package cloud.benchflow.experimentsmanager.resources.lifecycle.v1;

import cloud.benchflow.experimentsmanager.db.DbSession;
import cloud.benchflow.experimentsmanager.db.DbSessionManager;
import cloud.benchflow.experimentsmanager.exceptions.FabanException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchRunIdException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.RunStatusResponse;

import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/12/15.
 */
@Path("/status")
public class StatusBenchmarkResource {

    private FabanClient fabanClient;
    private DbSession db;

    @Inject
    public StatusBenchmarkResource(@Named("faban") FabanClient fabanClient,
                                   @Named("db")DbSessionManager db) {
        this.fabanClient = fabanClient;
        this.db = db.getSession();
    }

    @Path("{benchmarkName}/{experimentNumber}/{trialNumber}")
    @GET
    @Produces("application/vnd.experiments-manager.v1+json")
    public RunStatusResponse getRunStatus(@PathParam("benchmarkName") String benchmarkName,
                                          @PathParam("experimentNumber") long experimentNumber,
                                          @PathParam("trialNumber") int trialNumber) {

        String userId = "BenchFlow";

        try {

            String fabanRunId = db.getFabanRunId(userId, benchmarkName, experimentNumber, trialNumber);
            RunStatus status = fabanClient.status(new RunId(fabanRunId));
            return new RunStatusResponse(status.getStatus().toString());

        } catch (RunIdNotFoundException e) {
            throw new NoSuchRunIdException("No run scheduled for id.");
        } catch (FabanClientException e) {
            throw new FabanException(e);
        }

    }


}
