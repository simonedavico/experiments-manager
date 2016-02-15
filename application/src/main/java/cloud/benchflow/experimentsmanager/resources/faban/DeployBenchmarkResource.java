package cloud.benchflow.experimentsmanager.resources.faban;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import cloud.benchflow.experimentsmanager.exceptions.NoDriversException;
import cloud.benchflow.experimentsmanager.exceptions.UndeployableDriverException;
import cloud.benchflow.experimentsmanager.responses.faban.DeployStatusResponse;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.responses.DeployStatus;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.yaml.snakeyaml.Yaml;


import javax.ws.rs.*;
import javax.ws.rs.Path;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/11/15.
 *
 * config files get saved to bucket:
 * - /benchmarks/{benchmarkId}/config/{configFileName.yml}
 *
 * parsed config files maps get saved to bucket:
 * - /benchmarks/{benchmarkId}/config/{configFileName.yml}/parsed
 *
 */
@Path("/deploy")
public class DeployBenchmarkResource {

    private final MinioHandler mh;
    private final FabanClient fc;

    @Inject
    public DeployBenchmarkResource(@Named("faban") FabanClient fc, @Named("minio") MinioHandler mh) {
        this.fc = fc;
        this.mh = mh;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public DeployStatusResponse deployBenchmark(@FormDataParam("benchmark") InputStream benchmarkInputStream,
                                                @FormDataParam("benchmark") FormDataContentDisposition benchmarkDetail) throws IOException {

        final String benchmarkFileName = benchmarkDetail.getFileName();
        final String benchmarkFileNameNoExt = benchmarkFileName.substring(0, benchmarkFileName.lastIndexOf("."));

        byte[] cachedBenchmark = ByteStreams.toByteArray(benchmarkInputStream);
        InputStream in = new ByteArrayInputStream(cachedBenchmark);
        ZipEntry entry;
        int driversCount = 0;

        try(ZipInputStream zin = new ZipInputStream(in)) {
            DeployStatus status = null;
            while((entry = zin.getNextEntry()) != null) {
                if(BenchFlowArchiveUtils.isDriver.test(entry)) {
                    driversCount++;
//                    String driverName = entry.getName()
//                                             .substring(entry.getName().lastIndexOf("/")+1);
                    String driverName = BenchFlowArchiveUtils.getEntryFileName.apply(entry);
                    status = fc.deploy(zin, driverName);

                    if(status.getCode() != DeployStatus.Code.CREATED) {
                        throw new UndeployableDriverException(
                                "The driver " + driverName + " couldn't be deployed. Faban reported" +
                                " a status of " + status.getCode().toString());
                    }

                } else if(BenchFlowArchiveUtils.isBenchFlowConfigFile.test(entry)) {
                    byte[] cachedConfiguration = ByteStreams.toByteArray(zin);
                    String configFileName = BenchFlowArchiveUtils.getEntryFileName.apply(entry);
                    mh.storeConfig(benchmarkFileNameNoExt, configFileName, cachedConfiguration);
                    Map parsed = (Map) new Yaml().load(new ByteArrayInputStream(cachedConfiguration));
                    //TODO: save on minio
                }
            }

            if(driversCount == 0) throw new NoDriversException();
            mh.storeBenchmark(benchmarkFileNameNoExt, cachedBenchmark);

            return new DeployStatusResponse(status.getCode().toString());

        } catch (FabanClientException | IOException | ClientException e) {
            throw new BenchmarkDeployException("An unknown error occurred while processing your request.", e);
        }
    }

    /***
     *
     * Collection of static utility functions/predicate
     * to analyse the archive sent to the experiments-manager
     *
     */
    private static class BenchFlowArchiveUtils {

        private static BiPredicate<ZipEntry, String> isConfigFile = (e,s) -> e.getName().endsWith(s);
        private static Predicate<ZipEntry> isBenchflowBenchmarkConfig = e -> isConfigFile.test(e, "benchflow-benchmark.yml");
        private static Predicate<ZipEntry> isBenchflowComposeConfig = e -> isConfigFile.test(e, "benchflow-compose.yml");
        private static Predicate<ZipEntry> isDockerComposeConfig = e -> isConfigFile.test(e, "docker-compose.yml");

        public static Predicate<ZipEntry> isDriver = e ->  e.getName().endsWith(".jar") &&
                                                           !e.getName().contains("__MACOSX");

        public static  Predicate<ZipEntry> isBenchFlowConfigFile = isBenchflowBenchmarkConfig
                                                                   .or(isBenchflowComposeConfig)
                                                                   .or(isDockerComposeConfig);

        public static Function<ZipEntry, String> getEntryFileName = e -> e.getName().replaceFirst(".*/([^/?]+).*", "$1");

    }

}
