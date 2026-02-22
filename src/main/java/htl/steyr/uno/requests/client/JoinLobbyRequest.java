package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class JoinLobbyRequest implements Serializable {

    private int lobbyId;


    /**
     * Create a new JoinLobbyRequest for the given lobby ID.
     *
     * @param lobbyId the ID of the lobby to join
     */
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
