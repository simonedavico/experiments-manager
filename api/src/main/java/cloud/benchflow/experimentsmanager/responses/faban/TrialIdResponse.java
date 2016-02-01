package cloud.benchflow.experimentsmanager.responses.faban;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 11/12/15.
 */
public class TrialIdResponse {

    @JsonProperty("trialId")
    private String id;

    public TrialIdResponse(String id) {
        this.id = id;
    }

    public String getId() { return this.id; }

}
