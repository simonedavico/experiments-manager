package cloud.benchflow.experimentsmanager.utils;

import cloud.benchflow.experimentsmanager.exceptions.DriverGenerationException;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

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
                throw new DriverGenerationException("Error in driver generation",
                                                    response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new DriverGenerationException(e.getMessage(), e);
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
