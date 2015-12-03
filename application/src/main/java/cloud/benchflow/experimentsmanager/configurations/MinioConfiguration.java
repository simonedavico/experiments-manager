package cloud.benchflow.experimentsmanager.configurations;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 26/11/15.
 */
public class MinioConfiguration {

    @NotEmpty
    private String secretKey;

    @NotEmpty
    private String accessKey;

    @NotEmpty
    private String address;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
