package cloud.benchflow.experimentsmanager.configurations;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientConfiguration;
//import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExperimentsManagerConfiguration extends Configuration {

    @Valid
    @NotNull
    private FabanConfiguration fabanConfiguration = new FabanConfiguration();

    @Valid
    @NotNull
    private DriversMakerConfiguration driversMakerConfiguration = new DriversMakerConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private BenchFlowEnvConfiguration benchFlowEnvConfiguration = new BenchFlowEnvConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DbConfiguration dbConfiguration = new DbConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private MinioConfiguration minioConfiguration = new MinioConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @JsonProperty("drivers-maker")
    public DriversMakerConfiguration getDriversMakerConfiguration() {
        return driversMakerConfiguration;
    }

    @JsonProperty("drivers-maker")
    public void setDriversMakerConfiguration(DriversMakerConfiguration driversMakerConfiguration) {
        this.driversMakerConfiguration = driversMakerConfiguration;
    }

    @JsonProperty("faban")
	public FabanConfiguration getFabanConfiguration() {
		return fabanConfiguration;
	}

    @JsonProperty("faban")
	public void setFabanConfiguration(FabanConfiguration fabanConfiguration) {
		this.fabanConfiguration = fabanConfiguration;
	}

    @JsonProperty("database")
    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }

    @JsonProperty("database")
    public void setDbConfiguration(DbConfiguration dbConfiguration) {
        this.dbConfiguration = dbConfiguration;
    }

    @JsonProperty("benchflow.env")
    public BenchFlowEnvConfiguration getBenchFlowEnvConfiguration() {
        return benchFlowEnvConfiguration;
    }

    @JsonProperty("benchflow.env")
    public void setBenchFlowEnvConfiguration(BenchFlowEnvConfiguration benchFlowEnvConfiguration) {
        this.benchFlowEnvConfiguration = benchFlowEnvConfiguration;
    }

    @JsonProperty("minio")
    public MinioConfiguration getMinioConfiguration() {
        return minioConfiguration;
    }

    @JsonProperty("minio")
    public void setMinioConfiguration(MinioConfiguration minioConfiguration) {
        this.minioConfiguration = minioConfiguration;
    }
}