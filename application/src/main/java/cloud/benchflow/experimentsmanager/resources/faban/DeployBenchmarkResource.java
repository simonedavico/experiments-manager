package cloud.benchflow.experimentsmanager.resources.faban;

import com.codahale.metrics.annotation.Timed;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/11/15.
 */
@Path("/faban/deploy")
public class DeployBenchmarkResource {

    @POST
    @Path("/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String deployBenchmark(@PathParam("name") String name,
                                  @FormDataParam("file") InputStream benchmarkInputStream,
                                  @FormDataParam("file") FormDataContentDisposition benchmarkDetail) {


        Logger logger = LoggerFactory.getLogger("DeployBenchmarkResourceLogger");
        logger.debug("ciao");
        logger.debug(name);
        logger.debug(benchmarkDetail.getFileName());



        return name;
    }


}
