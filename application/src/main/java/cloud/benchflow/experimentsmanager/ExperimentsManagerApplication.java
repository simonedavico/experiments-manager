package cloud.benchflow.experimentsmanager;

import cloud.benchflow.experimentsmanager.resources.faban.DeployBenchmarkResource;
import cloud.benchflow.experimentsmanager.resources.faban.StatusBenchmarkResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;;

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
                    Environment environment) {

        final DeployBenchmarkResource db =
                new DeployBenchmarkResource(
                        configuration.getMinioConfiguration().getAccessKey(),
                        configuration.getMinioConfiguration().getSecretKey(),
                        configuration.getMinioConfiguration().getAddress()
                );
    	
        final StatusBenchmarkResource sb = new StatusBenchmarkResource();

        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(db);
        environment.jersey().register(sb);
    }

}
