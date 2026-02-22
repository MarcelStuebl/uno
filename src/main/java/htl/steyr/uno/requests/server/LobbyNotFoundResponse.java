package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class LobbyNotFoundResponse implements Serializable {

    private User user;

    public LobbyNotFoundResponse(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "LobbyNotFoundResponse{" +
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
