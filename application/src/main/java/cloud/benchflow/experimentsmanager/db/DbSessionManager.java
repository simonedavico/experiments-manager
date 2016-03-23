package cloud.benchflow.experimentsmanager.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 07/03/16.
 */
public class DbSessionManager {

    private SessionFactory sessionFactory;

    public DbSessionManager(String url, int port, String dbName, String username) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.configure(new File("./application/src/main/resources/hibernate.cfg.xml"))
                .applySetting("hibernate.connection.url", "jdbc:mysql://" + url + ":" +
                        port + "/" + dbName + "?createDatabaseIfNotExist=true")
                .applySetting("hibernate.connection.username", username)
                .applySetting("hibernate.connection.password", "");

        final StandardServiceRegistry registry = builder.build();
        try {

            this.sessionFactory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();

            checkDatabaseSchema();

        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Encountered a problem connecting to database " + dbName, e);
        }
    }

    /***
     * Checks existence of expected tables, and creates them if they
     * don't exist
     */
    private void checkDatabaseSchema() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        final String createExperimentsTable =
                "create table EXPERIMENTS " +
                "(USERNAME varchar(255) not null, " +
                "BENCHMARK_NAME varchar(255) not null, " +
                "EXP_NUMBER bigint not null, " +
                "PERFORMED_ON timestamp,  " +
                "primary key(USERNAME, BENCHMARK_NAME, EXP_NUMBER));";

        final String createTrialsTable =
                "create table TRIALS " +
                "(USERNAME varchar(255) not null, " +
                "BENCHMARK_NAME varchar(255) not null, " +
                "EXP_NUMBER bigint not null, " +
                "TRIAL_NUMBER integer not null, " +
                "FABAN_RUN_ID varchar(255), " +
                "PERFORMED_ON timestamp, " +
                "primary key (USERNAME, BENCHMARK_NAME, EXP_NUMBER, TRIAL_NUMBER))";

        //check for existence of tables EXPERIMENTS and TRIALS
        final String checkExists = "show tables like :tableName";
        List results = session.createSQLQuery(checkExists).setParameter("tableName", "EXPERIMENTS").list();

        if(results.size() == 0) {
            session.createSQLQuery(createExperimentsTable).executeUpdate();
        }

        results = session.createSQLQuery(checkExists).setParameter("tableName", "TRIALS").list();
        if(results.size() == 0) {
            session.createSQLQuery(createTrialsTable).executeUpdate();
        }

        session.getTransaction().commit();
        session.close();
    }

    public DbSession getSession() {
        return new DbSession(this.sessionFactory);
    }

}
