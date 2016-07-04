package cloud.benchflow.experimentsmanager.resources.lifecycle;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.DeployStatusResponse;
import cloud.benchflow.experimentsmanager.utils.minio.BenchFlowMinioClient;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.IOUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/03/16.
 */
@Path("/deploy")
public class DeployBenchmarkResource {

    private BenchFlowMinioClient minio;
    private Logger logger;

    @Inject
    public DeployBenchmarkResource(@Named("minio") BenchFlowMinioClient minio) {
        this.minio = minio;
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/vnd.experiments-manager.v2+json")
    public DeployStatusResponse deploy(@FormDataParam("benchmark") InputStream benchmarkArchive,
                                       @FormDataParam("benchmark") FormDataContentDisposition details) throws IOException {

        String user = "BenchFlow";
        byte[] archive = IOUtils.toByteArray(benchmarkArchive);
        byte[] unpacked = ZipUtil.unpackEntry(new ByteArrayInputStream(archive), "benchflow-benchmark.yml");

        String bb = new String(unpacked, "UTF-8");
        Map<String, Object> parsedBenchFlowBenchmark = (Map) new Yaml().load(bb);
        final String benchmarkName = (String) parsedBenchFlowBenchmark.get("benchmark_name");

        if(benchmarkName == null) {
            throw new BenchmarkDeployException("The submitted configuration doesn't include " +
                    "mandatory field benchmark_name");
        }

        final String minioBenchmarkId = user + "/" + benchmarkName;

        try {
            minio.saveOriginalBenchFlowBenchmark(minioBenchmarkId, bb);

            logger.debug("Saved benchmark configuration");

            ZipUtil.iterate(new ByteArrayInputStream(archive), (in, entry) -> {

                if(BenchmarkArchiveUtils.isDeploymentDescriptor.test(entry)) {

                    String content = IOUtils.toString(in, "UTF-8");
                    minio.saveOriginalDeploymentDescriptor(minioBenchmarkId, content);

                    logger.debug("Saved deployment descriptor");

                } else if (BenchmarkArchiveUtils.isModel.test(entry)) {

                    String modelName = BenchmarkArchiveUtils.getEntryFileName.apply(entry);
                    minio.saveModel(minioBenchmarkId, modelName, in);
                    logger.debug("Saved model " + modelName);

                } else if (BenchmarkArchiveUtils.isBenchmarkSources.test(entry)) {

                    minio.saveBenchmarkSources(in, minioBenchmarkId);
                    logger.debug("Saved sources");

                }
            });

            return new DeployStatusResponse(benchmarkName);
        }

        catch (Exception e) {
            cleanUp(minioBenchmarkId);
            throw new BenchmarkDeployException(e.getMessage(), e);
        }
    }


    private void cleanUp(final String benchmarkId) {
        minio.removeModels(benchmarkId);
        minio.removeOriginalBenchFlowBenchmark(benchmarkId);
        minio.removeOriginalDeploymentDescriptor(benchmarkId);
        minio.removeSources(benchmarkId);
    }

    private static class BenchmarkArchiveUtils {

        private static BiPredicate<ZipEntry, String> isBenchmarkEntry = (e, s) -> e.getName().endsWith(s);
        public static Function<ZipEntry, String> getEntryFileName = e -> e.getName().replaceFirst(".*/([^/?]+).*", "$1");

        public static Predicate<ZipEntry> isBenchmarkConfig = e -> isBenchmarkEntry.test(e, "benchflow-benchmark.yml");
        public static Predicate<ZipEntry> isDeploymentDescriptor = e -> isBenchmarkEntry.test(e, "docker-compose.yml");
        public static Predicate<ZipEntry> isModel = e -> e.getName().contains("models") &&
                                                         !(getEntryFileName.apply(e).endsWith("models/"));
        public static Predicate<ZipEntry> isBenchmarkSources = e -> isBenchmarkEntry.test(e, "sources.zip");

    }

}
