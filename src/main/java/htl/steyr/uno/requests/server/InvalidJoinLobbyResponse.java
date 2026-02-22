package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class InvalidJoinLobbyResponse implements Serializable {

    private User user;

    public InvalidJoinLobbyResponse(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "InvalidJoinLobbyResponse{" +
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
