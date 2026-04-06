package htl.steyr.uno.server.serverconnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private final int port = 59362;
    private Thread acceptThread;
    private boolean running;
    private final List<ServerSocketConnection> connections = Collections.synchronizedList(new ArrayList<>());
    private final List<Lobby> lobbies = Collections.synchronizedList(new ArrayList<>());


    /**
     * The main method serves as the entry point for the server application.
     * It creates an instance of the Server class and calls the start method to initialize and run the server.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }


    /**
     * Starts the server by creating a ServerSocket on the specified port and listening for incoming connections.
     * When a new connection is accepted, a new ServerSocketConnection is created for the client and the client is added to the list of active connections.
     * The server runs in a separate thread to allow for concurrent handling of multiple clients.
     * @throws IOException
     */
    private void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);

        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket s = serverSocket.accept();

                    ServerSocketConnection client = new ServerSocketConnection(s, this);
                    client.startReceiving();

                    connections.add(client);
                    System.out.println("[" + s.getRemoteSocketAddress() + "] New connection");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        acceptThread.start();

        System.out.println("Server started on port " + port);
        MailSender ms = new MailSender();
        ms.sendServerStartetNotification();
    }


    /**
     * Removes a connection from the server and ensures all associated resources are cleaned up.
     * This method is called when a client disconnects or when a connection error is detected.
     * 
     * The method performs the following operations:
     * 1. Checks if the connection is null (defensive programming)
     * 2. Removes the connection from the server's active connections list
     * 3. Ensures the connection is removed from any active lobby it might be in
     * 4. Logs the removal for debugging and monitoring purposes
     * 
     * This is a critical cleanup method that ensures:
     * - No zombie connections remain in the server's connection list
     * - Players are properly removed from lobbies when they disconnect
     * - Game state remains consistent
     * - Other players are notified of the disconnection
     * 
     * @param connection the ServerSocketConnection to remove from the server
     */
    void removeConnection(ServerSocketConnection connection) {
        if (connection == null) {
            return;
        }
        
        boolean removed = connections.remove(connection);
        if (removed) {
            System.out.println("[" + connection + "] Connection removed");
        }
        
        leaveLobby(connection);
    }

    /**
     * Removes a connection from its active lobby and cleans up empty lobbies.
     * This method searches for the lobby that the connection is part of and removes the connection from it.
     * 
     * The method performs the following:
     * 1. Iterates through all active lobbies on the server (using a copy to avoid ConcurrentModificationException)
     * 2. Checks if the connection is part of any lobby
     * 3. Removes the connection from the lobby by calling playerLeft()
     * 4. If the lobby becomes empty (no players and no players in game), removes the lobby entirely
     * 5. Logs the removal for debugging purposes
     * 
     * This ensures that:
     * - Players are properly notified when someone leaves their lobby
     * - Empty lobbies are cleaned up to free server resources
     * - Game state remains consistent
     * - No orphaned lobbies accumulate over time
     * 
     * @param serverSocketConnection the ServerSocketConnection to remove from its lobby
     */
    void leaveLobby(ServerSocketConnection serverSocketConnection) {
        if (serverSocketConnection == null) {
            return;
        }
        
        for (Lobby lobby : new ArrayList<>(lobbies)) {
            if (lobby.getConnections().contains(serverSocketConnection)) {
                System.out.println("Removing connection from lobby " + lobby.getLobbyId());
                lobby.playerLeft(serverSocketConnection);

                if (lobby.getConnections().isEmpty()) {
                    lobbies.remove(lobby);
                    System.out.println("Lobby " + lobby.getLobbyId() + " removed (empty)");
                }
                break;
            }
        }
    }


    /**
     * Logs a message to the console with the client's remote socket address as a prefix.
     * This method is used by ServerSocketConnection instances to log messages related to their respective clients.
     * @param message The message to be logged, which can be of any type (e.g., String, Object).
     */
    void sendLogMessage(Object message) {
        System.out.println(message);
    }


    /**
     * Stops the server by setting the running flag to false, interrupting the accept thread, and closing the server socket.
     * This method is used to gracefully shut down the server and release any resources associated with it.
     * @throws IOException If an I/O error occurs when closing the server socket.
     */
    void stop() throws IOException {
        System.out.println("Server stopped");
        running = false;
        acceptThread.interrupt();
        serverSocket.close();
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

    List<ServerSocketConnection> getConnections() {
        return connections;
    }


}




