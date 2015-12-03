package cloud.benchflow.experimentsmanager.configurations;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExperimentsManagerConfiguration extends Configuration {

    @NotEmpty
    private String template;

    @Valid
    @NotNull
    private MinioConfiguration minioConfiguration = new MinioConfiguration();

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty("minio")
    public MinioConfiguration getMinioConfiguration() {
        return minioConfiguration;
    }

    @JsonProperty("minio")
    public void setMinioConfiguration(MinioConfiguration mc) {
        this.minioConfiguration = mc;
    }

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
}