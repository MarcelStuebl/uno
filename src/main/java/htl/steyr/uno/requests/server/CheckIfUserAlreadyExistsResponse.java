package htl.steyr.uno.requests.server;

import java.io.Serializable;

public class CheckIfUserAlreadyExistsResponse implements Serializable {

    private String username;
    private String email;
    private boolean userAlreadyExists = true;
    private boolean emailAlreadyExists = true;

    public CheckIfUserAlreadyExistsResponse(String username, String email, boolean userAlreadyExists, boolean emailAlreadyExists) {
        setUsername(username);
        setUserAlreadyExists(userAlreadyExists);
        setEmail(email);
        setEmailAlreadyExists(emailAlreadyExists);
    }

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + getUsername() +
                '}';
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String userName) {
        this.username = username;
    }

    public boolean isUserAlreadyExists() {
        return userAlreadyExists;
    }
    public void setUserAlreadyExists(boolean userAlreadyExists) {
        this.userAlreadyExists = userAlreadyExists;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailAlreadyExists() {
        return emailAlreadyExists;
    }
    public void setEmailAlreadyExists(boolean emailAlreadyExists) {
        this.emailAlreadyExists = emailAlreadyExists;
    }

}
