package cloud.benchflow.experimentsmanager.db.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/02/16.
 */
@Entity
@Table(name = "TRIALS")
@IdClass(Trial.TrialId.class)
public class Trial {

    Trial() {}

    public Trial(int trialNumber) {
        this.trialNumber = trialNumber;
        this.performedOn = LocalDateTime.now();
    }

    @Embeddable
    public static class TrialId implements Serializable {

        private Experiment experiment;
        private int trialNumber;

        public Experiment getExperiment() {
            return experiment;
        }

        public void setExperiment(Experiment experiment) {
            this.experiment = experiment;
        }

        public int getTrialNumber() {
            return trialNumber;
        }

        public void setTrialNumber(int trialNumber) {
            this.trialNumber = trialNumber;
        }
    }

    @Id
    @Column(name = "TRIAL_NUMBER")
    private int trialNumber;

    @Id
    @JoinColumns({
            @JoinColumn(name = "USERNAME", referencedColumnName = "USERNAME"),
            @JoinColumn(name = "BENCHMARK_NAME", referencedColumnName = "BENCHMARK_NAME"),
            @JoinColumn(name = "EXP_NUMBER", referencedColumnName = "EXP_NUMBER")
    })
    @ManyToOne
    private Experiment experiment;


    @Column(name = "FABAN_RUN_ID")
    private String fabanRunId;

    @Column(name = "PERFORMED_ON")
    private LocalDateTime performedOn;

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public int getTrialNumber() {
        return trialNumber;
    }

    public void setTrialNumber(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    public String getFabanRunId() {
        return fabanRunId;
    }

    public void setFabanRunId(String fabanRunId) {
        this.fabanRunId = fabanRunId;
    }

    public LocalDateTime getPerformedOn() {
        return performedOn;
    }

    public void setPerformedOn(LocalDateTime performedOn) {
        this.performedOn = performedOn;
    }

}
