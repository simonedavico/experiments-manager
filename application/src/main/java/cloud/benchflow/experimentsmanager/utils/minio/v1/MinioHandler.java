package cloud.benchflow.experimentsmanager.utils.minio.v1;

import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public interface MinioHandler {

    void storeBenchmark(String benchmarkID, byte[] benchmark);

    void storeConfig(String benchmarkID, String configName, byte[] config);

    InputStream getConfig(String benchmarkId);

}
