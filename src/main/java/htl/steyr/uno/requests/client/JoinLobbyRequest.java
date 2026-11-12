package htl.steyr.uno.requests.client;

import java.io.Serializable;

/**
 * joinMethod: 0 = open, 1 = on_request, 2 = only_code
 */
public record JoinLobbyRequest(int lobbyId, int joinMethod) implements Serializable {

    @Override
    public String toString() {
        return "JoinLobbyRequest{" +
                "lobbyId=" + lobbyId() +
                '}';
    }
}