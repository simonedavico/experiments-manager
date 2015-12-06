package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 06/12/15.
 */
public class FabanException extends WebApplicationException {

    public FabanException(Throwable cause) {
        super("An unknown error occurred while trying to contact Faban. Please try again.",
               cause, Response.Status.INTERNAL_SERVER_ERROR);
    }

}
