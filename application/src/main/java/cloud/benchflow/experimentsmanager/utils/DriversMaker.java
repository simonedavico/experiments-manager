package cloud.benchflow.experimentsmanager.utils;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkGenerationException;
import cloud.benchflow.experimentsmanager.exceptions.BenchmarkRunException;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 02/12/15.
 */
public class DriversMaker {

    private String address;
    private HttpClient http;

    public DriversMaker(String address, HttpClient http) {
        this.address = address;
        this.http = http;
    }

    public InputStream convert(InputStream benchflowConfig) {

        ResponseHandler<InputStream> rh = resp -> resp.getEntity().getContent();

        HttpPost post = new HttpPost(address + "/convert");
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
//                                        .addBinaryBody("benchflow-benchmark", benchflowConfig,
//                                                        ContentType.create("application/octet-stream"),
//                                                        "benchflow.config.yml")
//                                        .addBinaryBody("benchflow-benchmark", benchflowConfig)
                .addBinaryBody("benchflow-benchmark",
                               benchflowConfig,
                               ContentType.create("text/yaml", Consts.UTF_8),
                               "benchflow-benchmark.yml")
                .build();
        post.setEntity(multipartEntity);
        try {
            HttpResponse response = http.execute(post);
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new BenchmarkDeployException();
        }

    }

    public void generateDriver(String benchmarkName, long experimentNumber, int trials) {

        HttpPost post = new HttpPost(address + "/generatedriver");

        MakeDriverRequestBody body = new MakeDriverRequestBody();
        body.setBenchmarkName(benchmarkName);
        body.setExperimentNumber(experimentNumber);
        body.setTrials(trials);

        String json = new Gson().toJson(body);

        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        try {
            HttpResponse response = http.execute(post);
            if(response.getStatusLine().getStatusCode() >= 400) {
                throw new BenchmarkGenerationException("Driver generation ended up returning error code " +
                                                        response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new BenchmarkGenerationException(e.getMessage(), e);
        }

    }

    private static class MakeDriverRequestBody {
        private String benchmarkName;
        private long experimentNumber;
        private int trials;
        MakeDriverRequestBody() {}

        public void setBenchmarkName(String benchmarkName) {
            this.benchmarkName = benchmarkName;
        }

        public void setExperimentNumber(long experimentNumber) {
            this.experimentNumber = experimentNumber;
        }

        public void setTrials(int trials) {
            this.trials = trials;
        }
    }

}
