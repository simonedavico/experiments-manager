package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/12/15.
 */
public class NoDriversException extends WebApplicationException {

    /**
     * Create a HTTP 400 (Bad Request) exception.
     */
    public NoDriversException() {
        super(Status.BAD_REQUEST);
    }

    /**
     * Create a HTTP 400 (Bad Request) exception.
     * @param message the String that is the exception message of the 500 response.
     */
    public NoDriversException(String message) {
//        super(Response.status(Status.INTERNAL_SERVER_ERROR).
//                entity(new JSONExceptionMessageContainer(message)).type(MediaType.APPLICATION_JSON).build());
        //super(Status.BAD_REQUEST,message);
        super(message, Status.BAD_REQUEST);
    }

}
