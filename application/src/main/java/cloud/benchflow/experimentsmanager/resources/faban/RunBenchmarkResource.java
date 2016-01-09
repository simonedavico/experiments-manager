package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.configurations.FabanConfiguration;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkRunException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchBenchmarkException;
import cloud.benchflow.experimentsmanager.responses.faban.RunIdResponse;
import cloud.benchflow.experimentsmanager.utils.DriversMaker;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;

import cloud.benchflow.faban.client.FabanClient;

import cloud.benchflow.faban.client.configurations.FabanClientConfigImpl;

import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;

import cloud.benchflow.faban.client.responses.RunId;
import io.minio.errors.ClientException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.inject.name.Named;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 04/12/15.
 */
@Path("/faban/run")
public class RunBenchmarkResource {

    private final MinioHandler mh;
    private final DriversMaker dm;

    @Named("faban")
    private FabanConfiguration fabanConf;

    public RunBenchmarkResource(final MinioHandler mh, final DriversMaker dm) {
        this.mh = mh;
        this.dm = dm;
    }

    @POST
    @Path("{benchmarkId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public RunIdResponse runBenchmark(@PathParam("benchmarkId") String benchmarkId,
                                 @DefaultValue("null") @FormDataParam("config") InputStream configInputStream,
                                 @DefaultValue("null") @FormDataParam("config") FormDataContentDisposition configDetail) {

        try {
            if(configInputStream != null) {
                //retrieve it from minio
                configInputStream = mh.getConfig(benchmarkId);
            }
            InputStream converted = dm.convert(configInputStream);

            FabanClientConfigImpl fabanConfig = new FabanClientConfigImpl(fabanConf.getUser(), fabanConf.getPassword(), new URI(fabanConf.getAddress()));
            FabanClient fc = new FabanClient().withConfig(fabanConfig);
            RunId rs = fc.submit(benchmarkId, benchmarkId, converted);

            return new RunIdResponse(rs.toString());
        } catch (BenchmarkNameNotFoundException e) {
            throw new NoSuchBenchmarkException(benchmarkId);
        } catch (FabanClientException | ClientException | IOException | URISyntaxException e) {
            throw new BenchmarkRunException();
        }

    }

}
