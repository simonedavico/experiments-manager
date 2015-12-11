package cloud.benchflow.experimentsmanager.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 09/12/15.
 */
public class UndeployableDriverException extends WebApplicationException {

   public UndeployableDriverException(String message) {
       super(message);
   }

}
