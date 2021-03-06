package cloud.benchflow.experimentsmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/12/15.
 */
public class DeployStatusResponse {

    @JsonProperty("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DeployStatusResponse(String status) {
        this.status = status;
    }

}
