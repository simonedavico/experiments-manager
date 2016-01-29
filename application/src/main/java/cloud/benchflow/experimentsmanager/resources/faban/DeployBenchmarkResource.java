package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import cloud.benchflow.experimentsmanager.exceptions.NoDriversException;
import cloud.benchflow.experimentsmanager.exceptions.UndeployableDriverException;
import cloud.benchflow.experimentsmanager.responses.faban.DeployStatusResponse;
import cloud.benchflow.experimentsmanager.utils.MinioHandlerImpl;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.responses.DeployStatus;

import com.google.common.base.Predicate;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;



import javax.ws.rs.*;
import javax.ws.rs.Path;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/11/15.
 */

@Path("/faban/deploy")
public class DeployBenchmarkResource {

    private final MinioHandlerImpl mh;
    private final FabanClient fc;

    @Inject
    public DeployBenchmarkResource(@Named("faban") FabanClient fc, @Named("minio") MinioHandlerImpl mh) {
        this.fc = fc;
        this.mh = mh;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public DeployStatusResponse deployBenchmark(@FormDataParam("file") InputStream benchmarkInputStream,
                                                @FormDataParam("file") FormDataContentDisposition benchmarkDetail) throws IOException {

        byte[] cachedBenchmark = ByteStreams.toByteArray(benchmarkInputStream);
        byte[] cachedConfiguration = null;

        InputStream in = new ByteArrayInputStream(cachedBenchmark);
        ZipEntry entry;

        //OS X archive utility adds junk when compressing
        Predicate<ZipEntry> isDriver = e ->  e.getName().endsWith(".jar") &&
                                            !e.getName().contains("__MACOSX");

        Predicate<ZipEntry> isConfigFile = e -> e.getName().endsWith("benchflow-benchmark.yml");

        int driversCount = 0;

        try(ZipInputStream zin = new ZipInputStream(in)) {
            DeployStatus status = null;
            while((entry = zin.getNextEntry()) != null) {
                if(isDriver.apply(entry)) {
                    driversCount++;
                    String driverName = entry.getName()
                                             .substring(entry.getName().lastIndexOf("/")+1);

                    status = fc.deploy(zin, driverName);

                    if(status.getCode() != DeployStatus.Code.CREATED) {
                        throw new UndeployableDriverException("The driver " + driverName + " couldn't be deployed. Faban reported" +
                                " a status of " + status.getCode().toString());
                    }



                } else if(isConfigFile.apply(entry)) {
                    //cache the config file to store it on minio
                    cachedConfiguration = ByteStreams.toByteArray(zin);
                }
            }

            if(driversCount == 0) throw new NoDriversException();
            String benchmarkFileName = benchmarkDetail.getFileName().substring(0, benchmarkDetail.getFileName().lastIndexOf("."));
            mh.storeBenchmark(benchmarkFileName, cachedBenchmark);

            //TODO: can this happen? or we generate a default configuration on the fly?
            if(cachedConfiguration != null)  {
                mh.storeConfig(benchmarkFileName, cachedConfiguration);
            }

            return new DeployStatusResponse(status.getCode().toString());

            //Questions still to answer:
            //1. what should happen if there are multiple drivers to deploy, but one of them returns error?

        } catch (FabanClientException | IOException | ClientException e) {
            throw new BenchmarkDeployException("An unknown error occurred while processing your request.", e);
        }
    }

}
