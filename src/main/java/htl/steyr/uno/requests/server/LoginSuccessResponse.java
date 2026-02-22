package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class LoginSuccessResponse implements Serializable {

    private User user;

    public LoginSuccessResponse(User user) {
        setUser(user);
    }


    @Override
    public String toString() {
        return "LoginSuccessResponse{" +
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
