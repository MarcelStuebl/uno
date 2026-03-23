package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class StartGameResponse implements Serializable {


    private LobbyInfoResponse lobbyInfoResponse;

    public StartGameResponse(LobbyInfoResponse lobbyInfoResponse) {
        setLobbyInfoResponse(lobbyInfoResponse);
    }

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "user='" + getLobbyInfoResponse().getUsers().getFirst().getUsername() +
                "'}";
    }

    public LobbyInfoResponse getLobbyInfoResponse() {
        return lobbyInfoResponse;
    }
    public void setLobbyInfoResponse(LobbyInfoResponse lobbyInfoResponse) {
        this.lobbyInfoResponse = lobbyInfoResponse;
    }


}
