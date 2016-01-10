package cloud.benchflow.experimentsmanager;

import cloud.benchflow.experimentsmanager.modules.DriversMakerModule;
import cloud.benchflow.experimentsmanager.modules.FabanModule;
import cloud.benchflow.experimentsmanager.modules.MinioModule;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import io.minio.errors.ClientException;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import ru.vyarus.dropwizard.guice.GuiceBundle;;import java.net.MalformedURLException;

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
        bootstrap.addBundle(new TemplateConfigBundle());

        GuiceBundle<ExperimentsManagerConfiguration> guiceBundle =
                GuiceBundle.<ExperimentsManagerConfiguration>builder()
                .enableAutoConfig("cloud.benchflow.experimentsmanager")
                .modules(new FabanModule(), new MinioModule(), new DriversMakerModule())
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(ExperimentsManagerConfiguration configuration,
                    Environment environment) throws MalformedURLException, ClientException {
        environment.jersey().register(MultiPartFeature.class);
    }

}
