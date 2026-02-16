package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LobbyInfoRequest implements Serializable {

    private Integer lobbyId;
    private final List<User> users = new ArrayList<>();

    @Override
    public String toString() {
        return "LobbyInfoRequest{" +
                "lobbyId=" + lobbyId +
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
}



