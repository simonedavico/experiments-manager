package cloud.benchflow.experimentsmanager.utils;

import cloud.benchflow.experimentsmanager.exceptions.BenchmarkDeployException;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
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
                                        .addBinaryBody("benchflow-config", benchflowConfig,
                                                        ContentType.create("application/octet-stream"),
                                                        "benchflow.config.yml")
                                        //.addBinaryBody("benchflow-config", benchflowConfig)
                                        .build();
        post.setEntity(multipartEntity);
        try {
            HttpResponse response = http.execute(post);
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new BenchmarkDeployException();
        }

    }

}
