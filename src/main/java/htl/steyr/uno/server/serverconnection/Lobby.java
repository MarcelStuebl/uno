package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Player;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.ReadyInGameTableRequest;
import htl.steyr.uno.requests.client.SendChatMessageRequest;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import htl.steyr.uno.requests.server.StartGameResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Lobby {

    private final Server server;
    private Integer lobbyId;
    private Integer status = 0; // 0 = waiting for players, 1 = full, 2 = in game
    private final List<ServerSocketConnection> connections = Collections.synchronizedList(new ArrayList<>());
    private final GameLogic gameLogic = new GameLogic(this);


    /**
     * Creates a new lobby with a random ID and adds it to the server's list of lobbies.
     * The lobby ID is generated randomly and checked for uniqueness against existing lobbies on the server.
     * If a duplicate ID is generated, the process is repeated until a unique ID is found.
     * The lobby is initialized with a status of 0 (waiting for players) and the lobby information is updated accordingly.
     * The new lobby is then added to the server's list of lobbies.
     *
     * @param server
     */
    public Lobby(Server server) {
        this.server = server;

        do {
            lobbyId = ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (server.getLobbies().stream().anyMatch(lobby -> Objects.equals(lobby.getLobbyId(), lobbyId)));

        System.out.println("Created lobby with ID: " + lobbyId);
        server.getLobbies().add(this);
    }

    public LobbyInfoResponse getLobbyInfoResponse() {
        synchronized (connections) {
            List<User> users = connections.stream()
                    .map(ServerSocketConnection::getUser)
                    .toList();
            return new LobbyInfoResponse(lobbyId, status, users);
        }
    }

    public void updateJoined() {
        checkStatus();
        LobbyInfoResponse response = getLobbyInfoResponse();
        for (var c : connections) c.sendMessage(response);
    }


    public void sendChatMessage(SendChatMessageRequest obj) {
        ReceiveChatMessageResponse response = new ReceiveChatMessageResponse(obj.message(), obj.user());
        synchronized (connections) {
            for (var c : connections) c.sendMessage(response);
        }
    }


    public void playerLeft(ServerSocketConnection connection) {
        connections.remove(connection);
        getGameLogic().getPlayers().removeIf(player -> player.getUsername().equals(connection.getUser().getUsername()));
        updateJoined();
    }


    private void checkStatus() {
        if (connections.size() >= 4) {
            setStatus(1);
        } else {
            setStatus(0);
        }
    }

    public void startGame() {
        setStatus(2);
        LobbyInfoResponse response = getLobbyInfoResponse();
        getGameLogic().createGame(response.users());
        for (ServerSocketConnection c : connections) {
            c.sendMessage(new StartGameResponse(gameLogic.getPlayersAsEnemies()));
        }
        updateJoined();
    }



    @Override
    public String toString() {
        return "Lobby{" +
                "lobbyId=" + lobbyId +
                ", connections=" + connections.size() +
                '}';
    }


    public String getConnectedPlayers() {
        StringBuilder sb = new StringBuilder();
        for (ServerSocketConnection conn : connections) {
            sb.append(conn.getUser().getUsername()).append(", ");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 2);
        return sb.toString();
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

    public List<ServerSocketConnection> getConnections() {
        return connections;
    }
    public void addConnection(ServerSocketConnection connection) {
        connections.add(connection);
    }
    public void removeConnection(ServerSocketConnection connection) {
        connections.remove(connection);
    }

    public Integer getLobbyId() {
        return lobbyId;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}

