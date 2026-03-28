package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public record CreateLobbyRequest(User user) implements Serializable {

    @Override
    public String toString() {
        return "CreateLobbyRequest{" +
                "username='" + user().getUsername() +
                "'}";
    }
}