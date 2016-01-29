package cloud.benchflow.experimentsmanager.utils;

import io.minio.errors.ClientException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public interface MinioHandler {

    void storeBenchmark(String benchmarkID, byte[] benchmark) throws IOException, ClientException;

    void storeConfig(String benchmarkID, byte[] config) throws IOException, ClientException;

    InputStream getConfig(String benchmarkId) throws IOException, ClientException;

}
