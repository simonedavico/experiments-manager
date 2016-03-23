package cloud.benchflow.experimentsmanager.utils.minio.v1;

import cloud.benchflow.experimentsmanager.utils.minio.v2.BenchFlowMinioClientException;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.http.entity.ContentType;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/12/15.
 *
 */
public class MinioHandlerImpl implements MinioHandler {

    private static final String benchmarkBucket = "benchmarks";
    private MinioClient mc;

    public MinioHandlerImpl(final String address, final String accessKey, final String secretKey)
            throws MalformedURLException, InvalidPortException, InvalidEndpointException {
        mc = new MinioClient(address, accessKey, secretKey);
    }

    private void storeObject(String id, byte[] object) {
        try {
            mc.putObject(benchmarkBucket, id,
                    new ByteArrayInputStream(object),
                            object.length, ContentType.APPLICATION_OCTET_STREAM.toString());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException |
                 XmlPullParserException | IOException e) {
            throw new BenchFlowMinioClientException(e.getMessage(), e);
        }
    }

    public void storeBenchmark(String benchmarkID, byte[] benchmark) {
        storeObject(benchmarkID + "/benchmark.zip", benchmark);
    }

    public void storeConfig(String benchmarkID, String configName, byte[] config) {
        storeObject(benchmarkID + "/config/" + configName, config);
    }

    public InputStream getConfig(String benchmarkId) {
        try {
            return mc.getObject(benchmarkBucket, benchmarkId + "/benchflow-benchmark.yml");
        } catch (MinioException | NoSuchAlgorithmException | IOException | InvalidKeyException | XmlPullParserException e) {
            throw new BenchFlowMinioClientException(e.getMessage(), e);
        }
    }

}
