package cloud.benchflow.experimentsmanager.db;

import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
        session.setFlushMode(FlushMode.ALWAYS);
        this.session.beginTransaction();
    }

    public void saveExperiment(Experiment e) {
        session.save(e);
//        session.flush(); //forces INSERT
//        session.refresh(e); //should update e with the experiment number generated in the DB
    }

    public String getFabanRunId(String username, String benchmarkName, long experimentNumber, int trialNumber) {

        String query = "select t.fabanRunId from Trial t where " +
                       "t.experiment.username = :uname and " +
                       "t.experiment.benchmarkName = :bname and " +
                       "t.experiment.experimentNumber = :expNum and " +
                       "t.trialNumber = :trialNum";

        return (String) session.createQuery(query)
                               .setParameter("uname", username)
                               .setParameter("bname", benchmarkName)
                               .setParameter("expNum", experimentNumber)
                               .setParameter("trialNum", trialNumber)
                               .uniqueResult();
    }

    public void rollback() {
        session.getTransaction().rollback();
    }

    public void update(Object row) {
        session.update(row);
    }

    public Trial get(String userId, String benchmarkName, long experimentNumber, int trialNumber) {
        String query = "select t from Trial t where " +
                       "t.experiment.username = :uname and " +
                       "t.experiment.benchmarkName = :bname and " +
                       "t.experiment.experimentNumber = :expNum and " +
                       "t.trialNumber = :trialNum";

        return (Trial) session.createQuery(query)
                              .setParameter("uname", userId)
                              .setParameter("bname", benchmarkName)
                              .setParameter("expNum", experimentNumber)
                              .setParameter("trialNum", trialNumber)
                              .uniqueResult();
    }

    public Experiment get(String userId, String benchmarkName, long experimentNumber) {

        String query = "select e from Experiment e where " +
                              "e.username = :uname and " +
                              "e.benchmarkName = :bname and " +
                              "e.experimentNumber = :expNum";

        return (Experiment) session.createQuery(query)
                                   .setParameter("uname", userId)
                                   .setParameter("bname", benchmarkName)
                                   .setParameter("expNum", experimentNumber)
                                   .uniqueResult();
    }

    public void updateTrials(Set<Trial> trials) {
//        session.beginTransaction();
        for(Trial t: trials) {
            session.update(t);
        }
//        session.flush();
//        session.getTransaction().commit();
    }

    public Session getSession() {
        return this.session;
    }

    @Override
    public void close() {
        session.getTransaction().commit();
        session.close();
    }
}
