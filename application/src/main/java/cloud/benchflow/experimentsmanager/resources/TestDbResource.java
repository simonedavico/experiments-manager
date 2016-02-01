package cloud.benchflow.experimentsmanager.resources;

import cloud.benchflow.experimentsmanager.db.DbUtils;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.hibernate.Session;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
@Path("/test/db")
public class TestDbResource {

    private final DbUtils db;

    @Inject
    public TestDbResource(@Named("db") DbUtils db) {
       this.db = db;
   }

   @GET
   public String testDb() {

       Session session = db.getSession().openSession();
       session.beginTransaction();

       Trial trial = new Trial("fooBenchmark.1B");
       Experiment exp = new Experiment("fooBenchmark");
       exp.addTrial(trial);
       session.save(exp);


//       String sql = "show tables like :tableName";
//       List results = session.createSQLQuery(sql).setParameter("tableName", "EXPERIMENTS").list();
//
//       if(results.size() == 0) {
//           session.createSQLQuery("create table EXPERIMENTS " +
//                                  "(EXPERIMENT_ID bigint not null auto_increment, " +
//                                  "BENCHMARK_NAME varchar(255), primary key (EXPERIMENT_ID))").executeUpdate();
//       }
//
//       results = session.createSQLQuery(sql).setParameter("tableName", "TRIALS").list();
//       if(results.size() == 0) {
//           session.createSQLQuery("create table TRIALS " +
//                                  "(TRIAL_ID integer not null auto_increment, " +
//                                  "FABAN_RUN_ID varchar(255), " +
//                                  "EXPERIMENT_ID bigint, primary key (TRIAL_ID))").executeUpdate();
//       }

       session.getTransaction().commit();

//       int requestId = 4;
//       List<Trial> trials = (List<Trial>)session.createQuery("from Trial where trialId = :requestId")
//                            .setParameter("requestId", requestId)
//                            .list();
//       trials.forEach(trial ->
//               System.out.println(trial.getExperiment().getExperimentId() + " " + trial.getTrialId() + " " + trial.getFabanRunId()));

//       String fabanRunId = (String) session.createQuery("select fabanRunId from Trial where trialId = :requestId")
//                                           .setParameter("requestId", requestId)
//                                           .uniqueResult();


//       session.getTransaction().commit();
       session.close();

       return "FINISHED";

   }

}
