package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

public class StartGameResponse {


    private LobbyInfoResponse lobbyInfoResponse;

    public StartGameResponse(LobbyInfoResponse lobbyInfoResponse) {
        setLobbyInfoResponse(lobbyInfoResponse);
    }

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "user=" + getLobbyInfoResponse().getUsers().getFirst().getUsername() +
                '}';
    }

    public LobbyInfoResponse getLobbyInfoResponse() {
        return lobbyInfoResponse;
    }
    public void setLobbyInfoResponse(LobbyInfoResponse lobbyInfoResponse) {
        this.lobbyInfoResponse = lobbyInfoResponse;
    }


}
