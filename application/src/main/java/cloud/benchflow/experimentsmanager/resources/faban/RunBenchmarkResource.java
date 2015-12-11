package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkRunException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchBenchmarkException;
import cloud.benchflow.experimentsmanager.responses.faban.RunIdResponse;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;

import cloud.benchflow.faban.client.FabanClient;

import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.responses.RunId;

import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 04/12/15.
 */
@Path("/faban/run/")
public class RunBenchmarkResource {

    private final MinioHandler mh;

    public RunBenchmarkResource(final MinioHandler mh) {
        this.mh = mh;
    }

    @Path("{benchmarkId}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public RunIdResponse runBenchmark(@PathParam("benchmarkId") String benchmarkId,
                               @FormDataParam("config") InputStream configInputStream,
                               @FormDataParam("config") FormDataContentDisposition configDetail) {

        try {
            if(configInputStream == null) {
                //retrieve it from minio
                configInputStream = mh.getConfig(benchmarkId);
                //TODO: generate faban config file...
            }

            FabanClient fc = new FabanClient();
            RunId rs = fc.submit(benchmarkId, benchmarkId, configInputStream);
            return new RunIdResponse(rs.toString());

        } catch (BenchmarkNameNotFoundException e) {
            throw new NoSuchBenchmarkException(benchmarkId);
        } catch (FabanClientException | ClientException | IOException e) {
            throw new BenchmarkRunException();
        }

    }

}
