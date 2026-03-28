package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record JoinLobbyRequest(int lobbyId) implements Serializable {

    @Override
    public String toString() {
        return "JoinLobbyRequest{" +
                "lobbyId=" + lobbyId() +
                '}';
    }
}