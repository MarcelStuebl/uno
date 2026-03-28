package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;
import java.util.List;

public record LobbyInfoResponse(Integer lobbyId, Integer status, List<User> users) implements Serializable {

    @Override
    public String toString() {
        return "LobbyInfoResponse{" +
                "lobbyId=" + lobbyId +
                ", status=" + status +
                ", users=" + users +
                '}';
    }
}