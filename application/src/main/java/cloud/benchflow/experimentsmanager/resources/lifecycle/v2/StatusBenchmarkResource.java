package cloud.benchflow.experimentsmanager.resources.lifecycle.v2;

import cloud.benchflow.experimentsmanager.db.DbSession;
import cloud.benchflow.experimentsmanager.db.DbSessionManager;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;
import cloud.benchflow.experimentsmanager.db.entities.Trial;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchExperimentIdException;
import cloud.benchflow.experimentsmanager.exceptions.NoSuchTrialIdException;
import cloud.benchflow.experimentsmanager.responses.lifecycle.ExperimentStatusResponse;
import cloud.benchflow.experimentsmanager.responses.lifecycle.TrialStatusResponse;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/04/16.
 */
@Path("status")
public class StatusBenchmarkResource {

   private FabanClient faban;
   private DbSessionManager db;

   @Inject
   public StatusBenchmarkResource(@Named("faban") FabanClient faban,
                                  @Named("db") DbSessionManager db) {
        this.faban = faban;
        this.db = db;
   }


   @GET
   @Path("{benchmarkName}/{experimentNumber}/{trialNumber}")
   @Produces("application/vnd.experiments-manager.v2+json")
   public TrialStatusResponse getTrialStatus(@PathParam("benchmarkName") String benchmarkName,
                                             @PathParam("experimentNumber") long experimentNumber,
                                             @PathParam("trialNumber") int trialNumber) {

       String userId = "BenchFlow";

       try(DbSession session = db.getSession()) {

           Trial trial = session.get(userId, benchmarkName, experimentNumber, trialNumber);

           if(trial == null)
               throw new NoSuchTrialIdException(new Trial(userId,
                                                          benchmarkName,
                                                          experimentNumber,
                                                          trialNumber).getTrialId());
           if(trial.isSubmitted()) {

               String runId = trial.getFabanRunId();
               try {

                   RunStatus.Code status = faban.status(new RunId(runId)).getStatus();
                   if(status == RunStatus.Code.COMPLETED) {
                       trial.setCompleted();
                       session.update(trial);
                   } else if(status == RunStatus.Code.FAILED) {
                       trial.setFailed();
                       session.update(trial);
                   }
                   return new TrialStatusResponse(trial.getTrialId(), status.name());

               } catch(RunIdNotFoundException e) {
                   //this should never happen
                   throw new NoSuchTrialIdException(trial.getTrialId());
               }

           } else {
               return new TrialStatusResponse(trial.getTrialId(), trial.getStatus());
           }
       }
   }

   @GET
   @Path("{benchmarkName}/{experimentNumber}")
   @Produces("application/vnd.experiments-manager.v2+json")
   public ExperimentStatusResponse getExperimentStatus(@PathParam("benchmarkName") String benchmarkName,
                                                       @PathParam("experimentNumber") long experimentNumber) {

       String userId = "BenchFlow";

       try(DbSession session = db.getSession()) {

           Experiment experiment = session.get(userId, benchmarkName, experimentNumber);

           if(experiment == null) {
               Experiment exp = new Experiment(userId, benchmarkName);
               exp.setExperimentNumber(experimentNumber);
               throw new NoSuchExperimentIdException(exp.getExperimentId());
           }

           ExperimentStatusResponse response =
                   new ExperimentStatusResponse(experiment.getExperimentId(),
                                                experiment.getStatus());

           for(Trial t : experiment.getTrials()) {
               response.addTrialStatus(getTrialStatus(benchmarkName, experimentNumber, t.getTrialNumber()));
           }

           if(!experiment.isCompleted() &&
              experiment.getTrials().stream().filter(Trial::isCompleted).count() == experiment.getTrials().size()) {
               experiment.setCompleted();
               session.update(experiment);
           }

           return response;

       }
   }

}
