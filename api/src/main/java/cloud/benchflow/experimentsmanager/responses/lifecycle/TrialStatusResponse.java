package cloud.benchflow.experimentsmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 09/04/16.
 */
public class TrialStatusResponse {

    public TrialStatusResponse(String trialId, String status) {
        this.trialId = trialId;
        this.status = status;
    }

    @JsonProperty("trialId")
    private String trialId;

    @JsonProperty("status")
    private String status;

    public String getTrialId() {
        return trialId;
    }

    public void setTrialId(String trialId) {
        this.trialId = trialId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
