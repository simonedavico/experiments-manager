package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/12/15.
 */
public class ExperimentDeployException extends WebApplicationException {

    public ExperimentDeployException(String message, Response.Status status) {
        super(message, status);
    }

    public ExperimentDeployException(String message) {
        this(message, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public ExperimentDeployException() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public ExperimentDeployException(String message, Throwable cause) {
        super(message, cause, Response.Status.INTERNAL_SERVER_ERROR);
    }

}
