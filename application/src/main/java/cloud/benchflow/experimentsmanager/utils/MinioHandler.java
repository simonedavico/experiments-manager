package cloud.benchflow.experimentsmanager.utils;

import io.minio.MinioClient;
import io.minio.errors.ClientException;
import org.apache.http.entity.ContentType;

import java.io.*;
import java.net.MalformedURLException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/12/15.
 *
 * TODO: should we expand this to a more high level library?
 */
public class MinioHandler {

    private static final String benchmarkBucket = "benchmarks";
    private MinioClient mc;

    public MinioHandler(final String address, final String accessKey, final String secretKey) throws MalformedURLException, ClientException {
        mc = new MinioClient(address, accessKey, secretKey);
    }

    private void storeObject(String id, byte[] object) throws IOException, ClientException {
        mc.putObject(benchmarkBucket, id,
                ContentType.APPLICATION_OCTET_STREAM.toString(),
                object.length, new ByteArrayInputStream(object));
    }

    public void storeBenchmark(String benchmarkID, byte[] benchmark) throws IOException, ClientException {
        storeObject(benchmarkID + "/benchmark.zip", benchmark);
    }

    public void storeConfig(String benchmarkID, byte[] config) throws IOException, ClientException {
        storeObject(benchmarkID + "/benchflow-benchmark.yml", config);
    }


}
