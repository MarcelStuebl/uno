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
    public void start() throws IOException {
        System.out.println("Server started on port " + port);
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
    }


    public void removeConnection(ServerSocketConnection connection) {
        connections.remove(connection);
        for (Lobby lobby : lobbies) {
            if (lobby.getConnections().contains(connection)) {
                lobby.playerLeft(connection);
                if (lobby.getConnections().isEmpty()) {
                    lobbies.remove(lobby);
                    System.out.println("Lobby " + lobby.getLobbyId() + " removed");
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
    public void setLogMessage(Object message) {
        System.out.println(message);
    }


    /**
     * Stops the server by setting the running flag to false, interrupting the accept thread, and closing the server socket.
     * This method is used to gracefully shut down the server and release any resources associated with it.
     * @throws IOException If an I/O error occurs when closing the server socket.
     */
    public void stop() throws IOException {
        System.out.println("Server stopped");
        running = false;
        acceptThread.interrupt();
        serverSocket.close();
    }


    public List<Lobby> getLobbies() {
        return lobbies;
    }

}




