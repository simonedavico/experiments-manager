package cloud.benchflow.experimentsmanager.utils;

import io.minio.errors.ClientException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public class MockMinioHandler implements MinioHandler {


    @Override
    public void storeBenchmark(String benchmarkID, byte[] benchmark) throws IOException, ClientException {

    }

    @Override
    public void storeConfig(String benchmarkID, byte[] config) throws IOException, ClientException {

    }

    @Override
    public InputStream getConfig(String benchmarkId) throws IOException, ClientException {
        return null;
    }

}
