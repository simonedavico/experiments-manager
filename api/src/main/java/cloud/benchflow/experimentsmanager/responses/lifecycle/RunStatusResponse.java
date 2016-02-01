package cloud.benchflow.experimentsmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/12/15.
 */
public class RunStatusResponse {

    @JsonProperty("runStatus")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RunStatusResponse(String status) {
        this.status = status;
    }

}
