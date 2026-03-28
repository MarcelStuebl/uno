package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public record LobbyNotFoundResponse(User user) implements Serializable {

    @Override
    public String toString() {
        return "LobbyNotFoundResponse{" +
                "username='" + user().getUsername() +
                "'}";
    }
}