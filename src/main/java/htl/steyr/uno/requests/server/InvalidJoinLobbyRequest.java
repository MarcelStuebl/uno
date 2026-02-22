package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class InvalidJoinLobbyRequest implements Serializable {

    private User user;

    public InvalidJoinLobbyRequest(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "InvalidJoinLobbyRequest{" +
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
