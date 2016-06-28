package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/03/16.
 */
public class DriverGenerationException extends WebApplicationException {

    public DriverGenerationException(String message) {
        super(message, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public DriverGenerationException(String message, int status) {
        super(message, status);
    }

    public DriverGenerationException(String message, Throwable cause) {
        super(message, cause, Response.Status.INTERNAL_SERVER_ERROR);
    }


}
