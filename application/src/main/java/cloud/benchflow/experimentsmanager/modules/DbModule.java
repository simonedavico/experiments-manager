package cloud.benchflow.experimentsmanager.modules;

import cloud.benchflow.experimentsmanager.configurations.DbConfiguration;
import cloud.benchflow.experimentsmanager.configurations.ExperimentsManagerConfiguration;
import cloud.benchflow.experimentsmanager.db.DbSessionManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.dropwizard.setup.Environment;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public class DbModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides @Singleton @Named("db")
    public DbSessionManager provideDb(ExperimentsManagerConfiguration config, Environment env) {
        DbConfiguration dbConfig = config.getDbConfiguration();
        DbSessionManager sessionFactory = new DbSessionManager(dbConfig.getHost(), dbConfig.getPort(),
                                                               dbConfig.getName(), dbConfig.getUser());
        return sessionFactory;
    }

}
