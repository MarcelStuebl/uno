package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class CreateAccountRequest implements Serializable {

    private String username;
    private String lastName;
    private String firstName;
    private String password;


    /**
     * Create a new CreateAccountRequest with the given parameters.
     *
     * @param username  the username of the new account
     * @param lastName  the last name of the user
     * @param firstName the first name of the user
     * @param password  the password for the new account
     */
    public CreateAccountRequest(String username, String lastName, String firstName, String password) {
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
        setPassword(password);
    }


    @Override
    public String toString() {
        return "CreateAccountRequest{" +
                "username='" + getUsername() +
                "', lastName='" + getLastName() +
                "', firstName='" + getFirstName() +
                "'}";
    }


    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}




