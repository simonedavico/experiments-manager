package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.exceptions.FabanException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchRunIdException;
import cloud.benchflow.experimentsmanager.responses.faban.RunStatusResponse;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/12/15.
 */
@Path("faban/status")
public class StatusBenchmarkResource {

    @Path("/{runId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RunStatusResponse getRunStatus(@PathParam("runId") String runId) {

        FabanClient fb = new FabanClient();
        try {

            RunStatus status = fb.status(new RunId(runId));
            return new RunStatusResponse(status.getStatus().toString());

        } catch (RunIdNotFoundException e) {
            throw new NoSuchRunIdException("No run scheduled for id " + runId + ".");
        } catch (FabanClientException e) {
            throw new FabanException(e);
        }
    }


}
