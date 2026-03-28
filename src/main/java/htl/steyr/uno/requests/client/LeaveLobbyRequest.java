package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public record LeaveLobbyRequest(User user) implements Serializable {

    @Override
    public String toString() {
        return "LeftLobbyRequest{" +
                "username='" + user().getUsername() +
                "'}";
    }
}