package cloud.benchflow.experimentsmanager.configurations;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 26/11/15.
 */
public class FabanConfiguration {

    private String user;

    private String password;

    @NotEmpty
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
