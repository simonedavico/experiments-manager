package cloud.benchflow.experimentsmanager;

import cloud.benchflow.experimentsmanager.resources.faban.DeployBenchmarkResource;
import cloud.benchflow.experimentsmanager.resources.faban.RunBenchmarkResource;
import cloud.benchflow.experimentsmanager.resources.faban.StatusBenchmarkResource;
import cloud.benchflow.experimentsmanager.utils.MinioHandler;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.MultiPartFeature;;import java.net.MalformedURLException;

public class ExperimentsManagerApplication extends Application<ExperimentsManagerConfiguration> {
    public static void main(String[] args) throws Exception {
        new ExperimentsManagerApplication().run(args);
    }

    @Override
    public String getName() {
        return "experiments-manager";
    }

    @Override
    public void initialize(Bootstrap<ExperimentsManagerConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(ExperimentsManagerConfiguration configuration,
                    Environment environment) throws MalformedURLException, ClientException {

        final MinioHandler mh = new MinioHandler(configuration.getMinioConfiguration().getAddress(),
                                                 configuration.getMinioConfiguration().getAccessKey(),
                                                 configuration.getMinioConfiguration().getSecretKey());

        final DeployBenchmarkResource db = new DeployBenchmarkResource(mh);
        final RunBenchmarkResource rb = new RunBenchmarkResource(mh);
        final StatusBenchmarkResource sb = new StatusBenchmarkResource();

        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(db);
        environment.jersey().register(sb);
    }

}
