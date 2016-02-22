package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 11/12/15.
 */
public class BenchmarkRunException extends WebApplicationException {

    public BenchmarkRunException(String message, Throwable cause) {
        super(message, cause);
    }

}
