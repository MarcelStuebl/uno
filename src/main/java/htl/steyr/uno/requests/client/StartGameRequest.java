package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public class StartGameRequest implements Serializable {

    private User user;

    public StartGameRequest(User user) {
        setUser(user);
    }

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "user=" + user +
                '}';
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }


}
