package cloud.benchflow.experimentsmanager.db.entities;

import org.hibernate.annotations.*;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/02/16.
 */
@Entity
@Table(name = "EXPERIMENTS")
@IdClass(Experiment.ExperimentId.class)
//@SQLInsert(sql="insert into EXPERIMENTS (PERFORMED_ON, BENCHMARK_NAME, EXP_NUMBER, USERNAME) values ()")
public class Experiment {

    //TODO: maybe only RUNNING and COMPLETED are necessary?
    private enum Status {
        GENERATING, QUEUED, RUNNING, COMPLETED
    }

    Experiment() {}

    public Experiment(String user, String benchmarkName) {
        this.benchmarkName = benchmarkName;
        this.performedOn = LocalDateTime.now();
        this.username = user;
        this.status = Status.GENERATING;
    }

    @Embeddable
    public static class ExperimentId implements Serializable {

        private String username;
        private String benchmarkName;
        private long experimentNumber;

        public String getBenchmarkName() {
            return benchmarkName;
        }

        public void setBenchmarkName(String benchmarkName) {
            this.benchmarkName = benchmarkName;
        }

        public long getExperimentNumber() {
            return experimentNumber;
        }

        public void setExperimentNumber(long experimentNumber) {
            this.experimentNumber = experimentNumber;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @Id
    @Column(name = "USERNAME")
    private String username;

    @Id
    @Column(name = "BENCHMARK_NAME")
    private String benchmarkName;

    @Id
    @Column(name = "EXP_NUMBER")
    @GenericGenerator(name = "expNumberGenerator",
                      strategy = "cloud.benchflow.experimentsmanager.db.generators.ExperimentNumberGenerator")
    @GeneratedValue(generator = "expNumberGenerator")
    private long experimentNumber;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private Set<Trial> trials = new HashSet<>();

    @Column(name = "PERFORMED_ON")
    private LocalDateTime performedOn;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

//    @Formula("concat(USERNAME, '.', BENCHMARK_NAME, '.', EXP_NUMBER)")
//    private String experimentId;

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public long getExperimentNumber() {
        return experimentNumber;
    }

    public void setExperimentNumber(long experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    public void addTrial(Trial trial) {
        trials.add(trial);
        trial.setExperiment(this);
    }

    public Set<Trial> getTrials() {
        return trials;
    }

    public void setTrials(Set<Trial> trials) {
        this.trials = trials;
    }

    public LocalDateTime getPerformedOn() {
        return performedOn;
    }

    public void setPerformedOn(LocalDateTime performedOn) {
        this.performedOn = performedOn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExperimentId() {
        return username + "." + benchmarkName + "." + experimentNumber;
    }

    public String getBenchmarkId() { return username + "." + benchmarkName; }

    public String getStatus() { return status.name(); }

    public void setQueued() { status = Status.QUEUED; }

    public void setRunning() { status = Status.RUNNING; }

    public void setCompleted() { status = Status.COMPLETED; }

    public boolean isQueued() { return status == Status.QUEUED; }

    public boolean isRunning() { return status == Status.RUNNING; }

    public boolean isCompleted() { return status == Status.COMPLETED; }



}
