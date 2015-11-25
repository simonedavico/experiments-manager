package cloud.benchflow.experimentsmanager;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import cloud.benchflow.experimentsmanager.configurations.HelloWorldConfiguration;
import cloud.benchflow.experimentsmanager.health.TemplateHealthCheck;
import cloud.benchflow.experimentsmanager.resources.HelloWorldResource;;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
        
    	final HelloWorldResource resource = new HelloWorldResource(
            configuration.getTemplate(),
            configuration.getDefaultName()
        );
    	
    	final TemplateHealthCheck healthCheck =
    	        new TemplateHealthCheck(configuration.getTemplate());
    	
    	environment.healthChecks().register("template", healthCheck);    	
        environment.jersey().register(resource);
    }

}
