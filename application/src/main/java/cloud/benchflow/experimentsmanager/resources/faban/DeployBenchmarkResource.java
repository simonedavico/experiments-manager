package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import cloud.benchflow.experimentsmanager.exceptions.NoDriversException;
import cloud.benchflow.experimentsmanager.responses.DeployStatusResponse;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;
import cloud.benchflow.experimentsmanager.utils.TemporaryFileHandler;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/11/15.
 */
@Path("/faban/deploy")
public class DeployBenchmarkResource {

    private static final String TMP_BENCHMARK_LOCATION = "./tmp/benchmarks/";

    private String address;
    private String accessKey;
    private String secretKey;

    public DeployBenchmarkResource(String accessKey, String secretKey, String address) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.address = address;
    }

    @POST
    @Path("/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public DeployStatusResponse deployBenchmark(@PathParam("name") String name,
                                                @FormDataParam("file") InputStream benchmarkInputStream,
                                                @FormDataParam("file") FormDataContentDisposition benchmarkDetail) {

        Logger logger = LoggerFactory.getLogger("DeployBenchmarkResourceLogger");
        //logger.debug(name);
        //logger.debug(benchmarkDetail.getFileName());

        String simpleZipName = benchmarkDetail.getFileName();
        if(simpleZipName.contains("."))
            simpleZipName = simpleZipName.substring(0, simpleZipName.lastIndexOf('.'));

        //TODO: check proper structure of benchmark
        //if structure is not ok, return a NonCompliantBenchmarkException

        java.nio.file.Path path = Paths.get(TMP_BENCHMARK_LOCATION + name + ".zip");
        try (TemporaryFileHandler tmp = new TemporaryFileHandler(benchmarkInputStream, path)) {

            ZipFile benchmark = new ZipFile(tmp.getFile());
            ZipEntry entry = benchmark.getEntry(simpleZipName + "/drivers/" + name + ".jar");

            if(entry == null) throw new NoDriversException("The drivers folder in the archive attached to the request " +
                                                           "does not contain any driver in JAR format.");

            java.nio.file.Path driverPath = Paths.get("./tmp/drivers/" + name + ".jar");

            try(TemporaryFileHandler tmpDriver = new TemporaryFileHandler(benchmark.getInputStream(entry), driverPath)) {

                FabanClient fc = new FabanClient();
                DeployStatus response = fc.deploy(tmpDriver.getFile());

                if(response.getCode() == DeployStatus.Code.CREATED) {

                    MinioHandler mh = new MinioHandler(address, accessKey, secretKey);
                    mh.storeBenchmark(name + ".jar", Files.readAllBytes(driverPath));

                }

                return new DeployStatusResponse(response.getCode().toString());
            }

        } catch (IOException | JarFileNotFoundException | ClientException  e) {
            throw new BenchmarkDeployException("An unknown error occurred while processing your request.", e);
        }

    }

}
