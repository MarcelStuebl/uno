package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public class CreateLobbyRequest implements Serializable {

    private User user;

    public CreateLobbyRequest(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "CreateLobbyRequest{" +
                "username='" + getUser().getUsername() +
                "'}";
    }


    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }



}
