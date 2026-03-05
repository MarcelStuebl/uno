package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class CheckIfUserAlreadyExistsRequest implements Serializable {

    private String username;
    private String email;

    public CheckIfUserAlreadyExistsRequest(String username, String email) {
        setUsername(username);
        setEmail(email);
    }

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + getUsername() +
                "', email='" + getEmail() +
                "'}";
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }




}
