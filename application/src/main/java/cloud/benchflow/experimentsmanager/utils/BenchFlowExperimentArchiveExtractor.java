package cloud.benchflow.experimentsmanager.utils;

import cloud.benchflow.minio.BenchFlowMinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.IOUtils;

import java.io.InputStream;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 28/07/16.
 */
public class BenchFlowExperimentArchiveExtractor {

    private BenchFlowMinioClient minio;
    private String minioTestId;
    private static Logger logger = LoggerFactory.getLogger(BenchFlowExperimentArchiveExtractor.class.getName());

    public BenchFlowExperimentArchiveExtractor(BenchFlowMinioClient minio, String minioTestId) {
        this.minio = minio;
        this.minioTestId = minioTestId;
    }

    public ExperimentConfiguration extract(InputStream archive) {

        try {

            ExperimentConfiguration config = new ExperimentConfiguration();

            ZipUtil.iterate(archive, (in, entry) -> {

                if(BenchmarkArchiveUtils.isDeploymentDescriptor.test(entry)) {

                    String deploymentDescriptor = IOUtils.toString(in, "UTF-8");
                    config.setDeploymentDescriptor(deploymentDescriptor);
                    minio.saveOriginalDeploymentDescriptor(minioTestId, deploymentDescriptor);
                    logger.debug("Saved experiment configuration");

                } else if(BenchmarkArchiveUtils.isExpConfig.test(entry)) {

                    String expConfig = IOUtils.toString(in, "UTF-8");
                    config.setExpConfig(expConfig);
                    minio.saveOriginalTestConfiguration(minioTestId, expConfig);
                    logger.debug("Saved deployment descriptor");

                } else if (BenchmarkArchiveUtils.isModel.test(entry)) {

                    String modelName = BenchmarkArchiveUtils.getEntryFileName.apply(entry);
                    minio.saveModel(minioTestId, modelName, in);
                    logger.debug("Saved model " + modelName);

                }

            });

            return config;

        } catch (Exception e) {

            cleanUp(minioTestId);
            throw new ArchiveExtractionException("Couldn't extract archive for test " + minioTestId, e);

        }

    }

    private void cleanUp(final String testId) {
        minio.removeModels(testId);
        //TODO: add
        //minio.removeOriginalBenchFlowBenchmark(benchmarkId);
        //minio.removeOriginalDeploymentDescriptor(benchmarkId);
    }


    private static class BenchmarkArchiveUtils {

        private static BiPredicate<ZipEntry, String> isExpConfigEntry = (e, s) -> e.getName().endsWith(s);
        public static Function<ZipEntry, String> getEntryFileName = e -> e.getName().replaceFirst(".*/([^/?]+).*", "$1");

        public static Predicate<ZipEntry> isExpConfig = e -> isExpConfigEntry.test(e, "benchflow-test.yml");
        public static Predicate<ZipEntry> isDeploymentDescriptor = e -> isExpConfigEntry.test(e, "docker-compose.yml");
//        public static Predicate<ZipEntry> isModel = e -> e.getName().contains("models") &&
//                !(getEntryFileName.apply(e).endsWith("models/"));
        public static Predicate<ZipEntry> isModel = e -> e.getName().contains("models") &&
                !(e.getName().endsWith("models/"));

    }


}
