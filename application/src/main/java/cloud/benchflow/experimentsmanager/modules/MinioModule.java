package cloud.benchflow.experimentsmanager.modules;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import cloud.benchflow.experimentsmanager.configurations.MinioConfiguration;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.minio.errors.ClientException;

import java.net.MalformedURLException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 10/01/16.
 */
public class MinioModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("minio")
    public MinioHandler provideMinio(ExperimentsManagerConfiguration config) throws MalformedURLException, ClientException {
        MinioConfiguration minioConfig = config.getMinioConfiguration();
        return new MinioHandler(minioConfig.getAddress(), minioConfig.getAccessKey(), minioConfig.getSecretKey());
    }

}
