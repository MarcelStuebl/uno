package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public class LeaveLobbyRequest implements Serializable {

    private User user;


    /**
     * Create a new LeftLobbyRequest for the given user.
     *
     * @param user
     */
    public LeaveLobbyRequest(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "LeftLobbyRequest{" +
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
