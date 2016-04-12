package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 06/04/16.
 */
public class NoSuchTrialIdException extends WebApplicationException {

    public NoSuchTrialIdException(String trialId) {
        super("Trial Id " + trialId + " does not exist.");
    }

}
