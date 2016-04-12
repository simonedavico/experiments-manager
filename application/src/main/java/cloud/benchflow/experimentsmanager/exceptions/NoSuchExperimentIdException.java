package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 09/04/16.
 */
public class NoSuchExperimentIdException extends WebApplicationException {

    public NoSuchExperimentIdException(String experimentId) {
        super("Experiment Id " + experimentId + " does not exist.");
    }

}
