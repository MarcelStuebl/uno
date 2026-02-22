package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class LobbyJoinRefusedResponse implements Serializable {

    private User user;
    private LobbyInfoResponse lobbyInfo;


    /**
     * Create a new LobbyJoinRefusedResponse for the given user and lobby info.
     * The user is the one who attempted to join the lobby, and the lobby info contains details about the lobby they tried to join.
     * This response is sent by the server to inform the client that their attempt to join the lobby was refused, along with information about the lobby.
     *
     * @param user
     * @param lobbyInfo
     */
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
