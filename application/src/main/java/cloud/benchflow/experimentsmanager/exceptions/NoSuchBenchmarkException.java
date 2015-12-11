package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 11/12/15.
 */
public class NoSuchBenchmarkException extends WebApplicationException {

    public NoSuchBenchmarkException(String benchmarkName) {
        super("The benchmark " + benchmarkName + " couldn't be found.", Response.Status.NOT_FOUND);
    }

}
