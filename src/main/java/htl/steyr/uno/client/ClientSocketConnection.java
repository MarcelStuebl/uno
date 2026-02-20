package htl.steyr.uno.client;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.server.LobbyInfoRequest;

import java.io.*;
import java.net.Socket;

public class ClientSocketConnection implements Closeable {

    private final Client client;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receiveThread;
    private volatile boolean running;
    private User user;
    private LobbyInfoRequest lobby;

    public ClientSocketConnection(String host, int port, Client client) throws IOException {
        this.socket = new Socket(host, port);
        this.client = client;

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startReceiving() {
        running = true;
        receiveThread = new Thread(() -> {
            try {
                while (running) {

                    Object obj = in.readObject();

                    if (obj instanceof User user) {
                        gotUser(user);
                    } else if (obj instanceof LobbyInfoRequest lobbyInfoRequest) {
                        gotLobby(lobbyInfoRequest);
                    }




                }
            } catch (Exception e) {
                if (running) System.out.println("Receive error: " + e.getMessage());
            }
        });
        receiveThread.start();
    }

    private void gotUser(User user) {
        this.user = user;

        if (user.getUsername() != null) {
            System.out.println(user);
            System.out.println("Login successful.");
        } else {
            System.out.println("Login failed.");
            client.logIn();
        }
    }

    private void gotLobby(LobbyInfoRequest lobby) {
        this.lobby = lobby;

        if (lobby.getUsers() != null) {
            System.out.println(lobby);
        } else {
            System.out.println("Lobby operation failed.");
            client.joinOrCreateLobby();
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        try { socket.close(); } catch (Exception ignored) {}
    }

    public User getUser() {
        return user;
    }


}
