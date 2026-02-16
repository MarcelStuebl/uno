package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class JoinLobbyRequest implements Serializable {

    private int lobbyId;

    public JoinLobbyRequest(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    @Override
    public String toString() {
        return "JoinLobbyRequest{" +
                "lobbyId=" + lobbyId +
                '}';
    }

    public int getLobbyId() {
        return lobbyId;
    }


}
