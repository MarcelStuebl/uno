package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public record ReceiveChatMessageResponse(String message, User user) implements Serializable {

    @Override
    public String toString() {
        return "ResiveChatMessageResponse{" +
                "username='" + user().getUsername() + '\'' +
                ", message='" + message() + '\'' +
                '}';
    }
}