package cloud.benchflow.experimentsmanager.configurations;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExperimentsManagerConfiguration extends Configuration {

    @Valid
    @NotNull
    private MinioConfiguration minioConfiguration = new MinioConfiguration();
    
    @Valid
    @NotNull
    private FabanConfiguration fabanConfiguration = new FabanConfiguration();

    @Valid
    @NotNull
    private DriversMakerConfiguration driversMakerConfiguration = new DriversMakerConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @JsonProperty("minio")
    public MinioConfiguration getMinioConfiguration() {
        return minioConfiguration;
    }

    @JsonProperty("minio")
    public void setMinioConfiguration(MinioConfiguration mc) {
        this.minioConfiguration = mc;
    }

    @JsonProperty("drivers.maker")
    public DriversMakerConfiguration getDriversMakerConfiguration() {
        return driversMakerConfiguration;
    }

    @JsonProperty("drivers.maker")
    public void setDriversMakerConfiguration(DriversMakerConfiguration driversMakerConfiguration) {
        this.driversMakerConfiguration = driversMakerConfiguration;
    }

	/**
	 * @return the fabanConfiguration
	 */
    @JsonProperty("faban")
	public FabanConfiguration getFabanConfiguration() {
		return fabanConfiguration;
	}

	/**
	 * @param fabanConfiguration the fabanConfiguration to set
	 */
    @JsonProperty("faban")
	public void setFabanConfiguration(FabanConfiguration fabanConfiguration) {
		this.fabanConfiguration = fabanConfiguration;
	}

}