package cloud.benchflow.experimentsmanager.utils;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 28/07/16.
 */
public class ExperimentConfiguration {

    private String expConfig;
    private String deploymentDescriptor;


    public String getExpConfig() {
        return expConfig;
    }

    public void setExpConfig(String expConfig) {
        this.expConfig = expConfig;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public void setDeploymentDescriptor(String deploymentDescriptor) {
        this.deploymentDescriptor = deploymentDescriptor;
    }
}
