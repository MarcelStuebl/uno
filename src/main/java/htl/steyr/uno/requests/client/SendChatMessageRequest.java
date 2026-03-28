package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public record SendChatMessageRequest(String message, User user) implements Serializable {

    @Override
    public String toString() {
        return "sendChatMessage{" +
                "username='" + user().getUsername() + '\'' +
                ", message='" + message() + '\'' +
                '}';
    }
}