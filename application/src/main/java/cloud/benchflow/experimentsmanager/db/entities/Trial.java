package cloud.benchflow.experimentsmanager.db.entities;

import javax.persistence.*;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
@Entity
@Table(name = "TRIALS")
public class Trial {

    @Column(name = "FABAN_RUN_ID")
    private String fabanRunId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="EXPERIMENT_ID")
    private Experiment experiment;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRIAL_ID")
    private int trialId;

    //Hibernate wants this to be at least package visible
    Trial() {}

    public Trial(String fabanRunId) {
        this.fabanRunId = fabanRunId;
    }

    public String getFabanRunId() {
        return fabanRunId;
    }

    public void setFabanRunId(String fabanRunId) {
        this.fabanRunId = fabanRunId;
    }

    public int getTrialId() {
        return trialId;
    }

    public void setTrialId(int trialId) {
        this.trialId = trialId;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

}
