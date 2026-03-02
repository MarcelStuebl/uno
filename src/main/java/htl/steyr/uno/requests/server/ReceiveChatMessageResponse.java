package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class ReceiveChatMessageResponse implements Serializable {

    private String message;
    private User user;

    public ReceiveChatMessageResponse(String message, User user) {
        this.message = message;
        this.user = user;
    }

    @Override
    public String toString() {
        return "ResiveChatMessageResponse{" +
                "username='" + user.getUsername() + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
