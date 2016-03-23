package cloud.benchflow.experimentsmanager.db;

import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public class DbSession implements AutoCloseable {

    private Session session;

    public DbSession(SessionFactory sessionFactory) {
        this.session = sessionFactory.openSession();
        this.session.beginTransaction();
    }

    /***
     *
     * @param e the experiment to be persisted
     */
    public void saveExperiment(Experiment e) {
//        session.beginTransaction();
        session.save(e);
        session.flush(); //forces INSERT
//        session.refresh(e); //should update e with the experiment number generated in the DB
//        session.getTransaction().commit();
    }

    /***
     *
     */
    public String getFabanRunId(String username, String benchmarkName, long experimentNumber, int trialNumber) {
//        session.beginTransaction();

        String query = "select t.fabanRunId from Trial t where " +
                       "t.experiment.username = :uname and " +
                       "t.experiment.benchmarkName = :bname and " +
                       "t.experiment.experimentNumber = :expNum and " +
                       "t.trialNumber = :trialNum";

        String fabanRunId = (String) session.createQuery(query)
                                      .setParameter("uname", username)
                                      .setParameter("bname", benchmarkName)
                                      .setParameter("expNum", experimentNumber)
                                      .setParameter("trialNum", trialNumber)
                                      .uniqueResult();

//        session.getTransaction().commit();
        return fabanRunId;
    }

    public void rollback() {
        this.session.getTransaction().rollback();
    }

    public void updateTrials(Set<Trial> trials) {
//        session.beginTransaction();
        for(Trial t: trials) {
            session.update(t);
        }
        session.flush();
//        session.getTransaction().commit();
    }

    public Session getSession() {
        return this.session;
    }

    @Override
    public void close() {
        this.session.getTransaction().commit();
        this.session.close();
    }
}
