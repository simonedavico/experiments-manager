package cloud.benchflow.experimentsmanager.modules.v1;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import cloud.benchflow.experimentsmanager.utils.env.BenchFlowEnv;
import cloud.benchflow.experimentsmanager.utils.minio.v1.MinioHandler;
import cloud.benchflow.experimentsmanager.utils.minio.v1.MinioHandlerImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

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
    @Named("minio.v1")
    @Inject
    public MinioHandler provideMinio(ExperimentsManagerConfiguration config, @Named("bfEnv")BenchFlowEnv benv)
            throws MalformedURLException, InvalidPortException, InvalidEndpointException {
//        String minioIp = benv.<String>getVariable("BENCHFLOW_MINIO_IP");
//        String minioPort = benv.<String>getVariable("BENCHFLOW_MINIO_PORT");
        //String minioAddress = "http://" + minioIp + ":" + minioPort;
        String minioAddress = config.getMinioConfiguration().getAddress();
        String accessKey = benv.<String>getVariable("BENCHFLOW_MINIO_ACCESS_KEY");
        String secretKey = benv.<String>getVariable("BENCHFLOW_MINIO_SECRET_KEY");
        return new MinioHandlerImpl(minioAddress,accessKey,secretKey);
    }

}
