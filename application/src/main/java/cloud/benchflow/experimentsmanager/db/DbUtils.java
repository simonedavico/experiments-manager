package cloud.benchflow.experimentsmanager.db;

import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.util.Map;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public class DbUtils {

    private SessionFactory sessionFactory;

    public DbUtils(String url, int port, String dbName, String username) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.configure(new File("./application/src/main/resources/hibernate.cfg.xml"))
               .applySetting("hibernate.connection.url", "jdbc:mysql://" + url + ":" + port + "/" + dbName)
               .applySetting("hibernate.connection.username", username)
               .applySetting("hibernate.connection.password", "");

        final StandardServiceRegistry registry = builder.build();
        try {

            this.sessionFactory = new MetadataSources(registry)
                                      .buildMetadata()
                                      .buildSessionFactory();
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Encountered a problem connecting to database " + dbName, e);
        }
    }

    /***
     *
     * @param e the experiment to be persisted
     */
    public void saveExperiment(Experiment e) {
        Session s = this.sessionFactory.openSession();
        s.beginTransaction();
        s.save(e);
        s.getTransaction().commit();
        s.close();
    }

    /***
     *
     * @param trialId the id of the trial
     * @return the faban runId corresponding to the given trial
     */
    public String getFabanRunId(int trialId) {
        Session s = this.sessionFactory.openSession();
        s.beginTransaction();

        String query = "select fabanRunId from Trial where trialId = :requestId";
        String fabanRunId = (String) s.createQuery(query)
                                      .setParameter("requestId", trialId)
                                      .uniqueResult();

        s.getTransaction().commit();
        s.close();
        return fabanRunId;
    }

    //TODO: remove this
    public SessionFactory getSession() {
        return this.sessionFactory;
    }

}
