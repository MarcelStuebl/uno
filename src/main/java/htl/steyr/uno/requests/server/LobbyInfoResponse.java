package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LobbyInfoResponse implements Serializable {

    private Integer lobbyId;
    private Integer status = 0; // 0 = waiting for players, 1 = full, 2 = in game
    private final List<User> users = new ArrayList<>();

    /**
     * Create a new LobbyInfoResponse with the given lobby ID and status.
     */
    public LobbyInfoResponse() {}


    @Override
    public String toString() {
        return "LobbyInfoResponse{" +
                "lobbyId=" + lobbyId +
                ", status=" + status +
                ", users=" + users +
                '}';
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public void clearUsers() {
        users.clear();
    }

    public void setLobbyId(Integer lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Integer getLobbyId() {
        return lobbyId;
    }

    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean canJoin() {
        return status == 0;
    }


}



