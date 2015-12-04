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

    public void storeBenchmark(String benchmarkID, byte[] benchmark) throws IOException, ClientException {

        //String bucket = benchmarkBucket.replace("x", benchmarkID);

        //mc.makeBucket(bucket);

        //TODO: check that the size is specified in the right way
        mc.putObject(benchmarkBucket, benchmarkID,
                     ContentType.APPLICATION_OCTET_STREAM.toString(),
                     benchmark.length, new ByteArrayInputStream(benchmark));
    }


}
