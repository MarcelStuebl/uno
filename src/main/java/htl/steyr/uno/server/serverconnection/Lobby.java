package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.requests.server.LobbyInfoResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lobby {

    private final Server server;
    private Integer lobbyId;
    private Integer status = 0; // 0 = waiting for players, 1 = full, 2 = in game
    private final LobbyInfoResponse lobbyInfoResponse = new LobbyInfoResponse();
    private final List<ServerSocketConnection> connections = Collections.synchronizedList(new ArrayList<>());

    public Lobby(Server server) {
        this.server = server;
        lobbyId = (int) (Math.random() * 100000);
        while (server.getLobbies().stream().anyMatch(lobby -> Objects.equals(lobby.getLobbyId(), lobbyId))) {
            lobbyId = (int) (Math.random() * 100000);
        }
        System.out.println("Created lobby with ID: " + lobbyId);
        lobbyInfoResponse.setLobbyId(lobbyId);
        server.getLobbies().add(this);
    }

    public void updateJoined() {
        LobbyInfoResponse msg = new LobbyInfoResponse();
        msg.setLobbyId(lobbyId);

        synchronized (connections) {
            for (var c : connections) msg.addUser(c.getUser());
            if (getStatus() != 2) checkStatus();
            msg.setStatus(getStatus());
            for (var c : connections) c.sendMessage(msg);
        }
    }

    private void checkStatus() {
        if (connections.size() >= 2) {
            setStatus(1);
        } else {
            setStatus(0);
        }
    }

    public void setStatus(Integer status) {
        this.status = status;
        lobbyInfoResponse.setStatus(status);
    }

    public Integer getStatus() {
        return status;
    }

    public boolean canJoin() {
        return status == 0;
    }



    @Override
    public String toString() {
        return "Lobby{" +
                "lobbyId=" + lobbyId +
                ", connections=" + connections.size() +
                '}';
    }


    public void addConnection(ServerSocketConnection connection) {
        connections.add(connection);
    }

    public void removeConnection(ServerSocketConnection connection) {
        connections.remove(connection);
    }

    public List<ServerSocketConnection> getConnections() {
        return connections;
    }

    public Integer getLobbyId() {
        return lobbyId;
    }

    public String getConnectedPlayers() {
        StringBuilder sb = new StringBuilder();
        for (ServerSocketConnection conn : connections) {
            sb.append(conn.getUser().getUsername()).append(", ");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    public LobbyInfoResponse getLobbyInfoResponse() {
        return lobbyInfoResponse;
    }

}




