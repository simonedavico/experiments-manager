package cloud.benchflow.experimentsmanager.configurations;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExperimentsManagerConfiguration extends Configuration {

    @Valid
    @NotNull
    private MinioConfiguration minioConfiguration = new MinioConfiguration();

    @JsonProperty("minio")
    public MinioConfiguration getMinioConfiguration() {
        return minioConfiguration;
    }

    @JsonProperty("minio")
    public void setMinioConfiguration(MinioConfiguration mc) {
        this.minioConfiguration = mc;
    }

}