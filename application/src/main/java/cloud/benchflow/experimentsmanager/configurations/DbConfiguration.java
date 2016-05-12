package cloud.benchflow.experimentsmanager.configurations;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 29/01/16.
 */
public class DbConfiguration {

    private String address;
    private String user;
    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String dbName) {
        this.name = dbName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
