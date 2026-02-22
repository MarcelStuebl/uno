package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class LobbyJoinRefusedResponse implements Serializable {

    private User user;
    private LobbyInfoResponse lobbyInfo;

    public LobbyJoinRefusedResponse(User user, LobbyInfoResponse lobbyInfo) {
        setLobbyInfo(lobbyInfo);
        setUser(user);
    }


    @Override
    public String toString() {
        return "LobbyJoinRefusedResponse{" +
                "username='" + getUser().getUsername() +
                "'}";
    }


    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public LobbyInfoResponse getLobbyInfo() {
        return lobbyInfo;
    }
    public void setLobbyInfo(LobbyInfoResponse lobbyInfo) {
        this.lobbyInfo = lobbyInfo;
    }



}
