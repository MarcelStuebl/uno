package htl.steyr.uno.server.serverconnection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UNO WebSocket server. The port is intentionally unchanged so existing
 * deployments only need to replace the server artifact.
 */
public class Server extends WebSocketServer {
    public static final int PORT = 59362;

    private final List<ServerSocketConnection> connections =
            Collections.synchronizedList(new ArrayList<>());
    private final List<Lobby> lobbies =
            Collections.synchronizedList(new ArrayList<>());

    public Server() {
        super(new InetSocketAddress(PORT));
    }

    public static void main(String[] args) {
        new Server().start();
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {
        ServerSocketConnection client = new ServerSocketConnection(connection, this);
        connections.add(client);
        client.startReceiving();
        System.out.println("[" + connection.getRemoteSocketAddress() + "] New WebSocket connection");
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        findConnection(connection).ifPresent(client -> client.onMessage(message));
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        findConnection(connection).ifPresent(ServerSocketConnection::close);
        System.out.println("[" + connection.getRemoteSocketAddress() + "] WebSocket closed: " + reason);
    }

    @Override
    public void onError(WebSocket connection, Exception exception) {
        System.out.println("WebSocket error"
                + (connection == null ? "" : " (" + connection.getRemoteSocketAddress() + ")")
                + ": " + exception.getMessage());
        if (connection != null) {
            findConnection(connection).ifPresent(client -> client.closeWithReason("WebSocket error"));
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started on WebSocket port " + PORT);
        new MailSender().sendServerStartetNotification();
    }

    private java.util.Optional<ServerSocketConnection> findConnection(WebSocket connection) {
        synchronized (connections) {
            return connections.stream()
                    .filter(client -> client.getWebSocket() == connection)
                    .findFirst();
        }
    }

    void removeConnection(ServerSocketConnection connection) {
        if (connection == null) {
            return;
        }
        if (connections.remove(connection)) {
            System.out.println("[" + connection + "] Connection removed");
        }
        leaveLobby(connection);
    }

    void leaveLobby(ServerSocketConnection connection) {
        if (connection == null) {
            return;
        }
        for (Lobby lobby : new ArrayList<>(lobbies)) {
            if (lobby.getConnections().contains(connection)) {
                System.out.println("Removing connection from lobby " + lobby.getLobbyId());
                lobby.playerLeft(connection);
                if (lobby.getConnections().isEmpty()) {
                    lobbies.remove(lobby);
                    System.out.println("Lobby " + lobby.getLobbyId() + " removed (empty)");
                }
                break;
            }
        }
    }

    void sendLogMessage(Object message) {
        System.out.println(message);
    }

    public void shutdown() throws InterruptedException {
        System.out.println("Server stopped");
        stop();
    }

    List<Lobby> getLobbies() {
        return lobbies;
    }

    Lobby getLobbyByConnection(ServerSocketConnection connection) {
        for (Lobby lobby : lobbies) {
            if (lobby.getConnections().contains(connection)) {
                return lobby;
            }
        }
        return null;
    }

    List<ServerSocketConnection> getClientConnections() {
        return connections;
    }
}
