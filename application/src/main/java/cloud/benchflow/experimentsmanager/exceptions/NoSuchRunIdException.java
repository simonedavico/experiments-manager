package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/12/15.
 */
public class NoSuchRunIdException extends WebApplicationException {

    /**
     * Create a HTTP 404 (Not Found) exception.
     */
    public NoSuchRunIdException() {
        super(Response.Status.NOT_FOUND);
    }

    /**
     * Create a HTTP 404 (Not Found) exception.
     * @param message the String that is the exception message of the 404 response.
     */
    public NoSuchRunIdException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }

}
