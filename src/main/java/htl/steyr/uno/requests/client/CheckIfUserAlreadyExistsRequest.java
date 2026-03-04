package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class CheckIfUserAlreadyExistsRequest implements Serializable {

    private String username;

    public CheckIfUserAlreadyExistsRequest(String username) {
        setUsername(username);
    }

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + getUsername() +
                "'}";
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String userName) {
        this.username = username;
    }




}
