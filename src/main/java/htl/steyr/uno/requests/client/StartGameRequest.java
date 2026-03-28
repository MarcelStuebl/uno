package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public record StartGameRequest(User user) implements Serializable {

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "user='" + user().getUsername() +
                "'}";
    }
}