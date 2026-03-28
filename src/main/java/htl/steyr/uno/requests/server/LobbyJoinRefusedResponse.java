package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public record LobbyJoinRefusedResponse(User user, LobbyInfoResponse lobbyInfo) implements Serializable {

    @Override
    public String toString() {
        return "LobbyJoinRefusedResponse{" +
                "username='" + user().getUsername() +
                "'}";
    }
}