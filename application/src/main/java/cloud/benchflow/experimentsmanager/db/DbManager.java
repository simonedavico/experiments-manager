package cloud.benchflow.experimentsmanager.db;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 07/03/16.
 */
public class DbManager {

    private SessionFactory sessionFactory;

    public DbManager(String url, String dbName, String username, String password) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        System.out.println("jdbc:mysql://" + url + "/" + dbName + "?createDatabaseIfNotExist=true");
        builder.configure(new File("./application/src/main/resources/hibernate.cfg.xml"))
                .applySetting("hibernate.connection.url", "jdbc:mysql://" + url +
                              "/" + dbName + "?createDatabaseIfNotExist=true")
                .applySetting("hibernate.connection.username", username)
                .applySetting("hibernate.connection.password", password);

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
    private void checkDatabaseSchema() throws IOException {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        Path createExperimentsTableQuery =
                Paths.get("./application/src/main/resources/db/create-experiments-table.sql");
        final String createExperimentsTable =
                FileUtils.readFileToString(createExperimentsTableQuery.toFile(), Charsets.UTF_8);

        Path createTrialsTableQuery =
                Paths.get("./application/src/main/resources/db/create-trials-table.sql");
        final String createTrialsTable =
                FileUtils.readFileToString(createTrialsTableQuery.toFile(), Charsets.UTF_8);

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

    public ExperimentsDAO getExperimentsDAO() {
        return new ExperimentsDAO(this.sessionFactory);
    }

}
