package cloud.benchflow.experimentsmanager.db.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
@Entity
@Table(name = "EXPERIMENTS")
public class Experiment {

    @OneToMany(mappedBy = "experiment", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Set<Trial> trials = new HashSet<>();

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXPERIMENT_ID")
    private long experimentId;

    @Column(name = "BENCHMARK_NAME")
    private String benchmarkName;

    //Hibernate wants this to be at least package visible
    Experiment() {}

    public Experiment(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public Set<Trial> getTrials() { return trials; }
    public void setTrials(Set<Trial> trials) { this.trials = trials; }

    public long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(long experimentId) {
        this.experimentId = experimentId;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public void addTrial(Trial trial) {
        trial.setExperiment(this);
        this.trials.add(trial);
    }

}
