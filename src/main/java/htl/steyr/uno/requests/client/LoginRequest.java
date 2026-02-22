package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class LoginRequest implements Serializable {

    private String username;
    private String password;


    /**
     * Create a new LoginRequest with the given username and password.
     *
     * @param username
     * @param password
     */
    public LoginRequest(String username, String password) {
        setUsername(username);
        setPassword(password);
    }


    @Override
    public String toString() {
        return "LoginRequest{" + "username='" + username + "'}";
    }


    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


}




