package cloud.benchflow.experimentsmanager.modules;

import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;
import org.apache.http.client.HttpClient;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 10/01/16.
 */
public class ManagedHttpClientModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public HttpClient providesHttpClient(ExperimentsManagerConfiguration config, Environment environment) {
        return new HttpClientBuilder(environment).using(config.getHttpClientConfiguration()).build(environment.getName());
    }
}
