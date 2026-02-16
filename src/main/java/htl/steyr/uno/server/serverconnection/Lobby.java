package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.requests.server.LobbyInfoRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lobby {

    private final Server server;
    private Integer lobbyId;
    private final LobbyInfoRequest lobbyInfoRequest = new LobbyInfoRequest();
    private final List<ServerSocketConnection> connections = Collections.synchronizedList(new ArrayList<>());

    public Lobby(Server server) {
        this.server = server;
        lobbyId = (int) (Math.random() * 100000);
        while (server.getLobbies().stream().anyMatch(lobby -> Objects.equals(lobby.getLobbyId(), lobbyId))) {
            lobbyId = (int) (Math.random() * 100000);
        }
        System.out.println("Created lobby with ID: " + lobbyId);
        lobbyInfoRequest.setLobbyId(lobbyId);
        server.getLobbies().add(this);
    }

    public void updateJoined() {
        LobbyInfoRequest msg = new LobbyInfoRequest();
        msg.setLobbyId(lobbyId);

        synchronized (connections) {
            for (var c : connections) msg.addUser(c.getUser());
            for (var c : connections) c.sendMessage(msg);
        }
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

    public LobbyInfoRequest getLobbyInfoRequest() {
        return lobbyInfoRequest;
    }

}




