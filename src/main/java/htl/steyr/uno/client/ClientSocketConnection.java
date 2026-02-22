package htl.steyr.uno.client;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.server.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocketConnection implements Closeable {

    private final Client client;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receiveThread;
    private volatile boolean running;
    private User user;
    private LobbyInfoResponse lobby;

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

                    if (obj instanceof LoginSuccessResponse msg) {
                        logInSuccess(msg);
                    } else if (obj instanceof LoginFailedResponse msg) {
                        logInFailed(msg);
                    } else if (obj instanceof LobbyInfoResponse msg) {
                        gotLobby(msg);
                    } else if (obj instanceof JoinLobbySuccessResponse msg) {
                        joinLobbySuccess(msg);
                    } else if (obj instanceof LobbyNotFoundResponse msg) {
                        lobbyNotFound(msg);
                    } else if (obj instanceof LobbyJoinRefusedResponse msg) {
                        lobbyJoinRefused(msg);
                    } else if (obj instanceof CreateLobbySuccessResponse msg) {
                        createLobbySuccess(msg);
                    } else {
                        System.out.println("Received unknown message: " + obj);
                    }


                }
            } catch (Exception e) {
                if (running) System.out.println("Receive error: " + e.getMessage());
            }
        });
        receiveThread.start();
    }

    private void logInSuccess(LoginSuccessResponse msg) {
        this.user = msg.getUser();

        System.out.println(user);
        System.out.println("Login successful.");


        client.joinOrCreateLobby();
    }

    private void logInFailed(LoginFailedResponse msg) {
        System.out.println("Login failed. Please try again.");
        client.logIn();
    }

    private void gotLobby(LobbyInfoResponse lobby) {
        this.lobby = lobby;

        if (lobby.getUsers() != null) {
            System.out.println(lobby);
        } else {
            System.out.println("Lobby operation failed.");
            client.joinOrCreateLobby();
        }
    }

    private void lobbyNotFound(LobbyNotFoundResponse lobby) {
        System.out.println("Invalid lobby ID. Please try again.");
        client.joinOrCreateLobby();
    }

    private void lobbyJoinRefused(LobbyJoinRefusedResponse msg) {
        if (msg.getLobbyInfo().getStatus() == 1) {
            System.out.println("Lobby is full. Please try again.");
        } else if (msg.getLobbyInfo().getStatus() == 2) {
            System.out.println("Game already started. Please try again.");
        } else {
            System.out.println("Unknown error. Please try again.");
        }
        client.joinOrCreateLobby();
    }

    private void createLobbySuccess(CreateLobbySuccessResponse msg) {
        System.out.println("Lobby created successfully.");
    }

    private void joinLobbySuccess(JoinLobbySuccessResponse msg) {
        System.out.println("Joined lobby successfully.");
    }

    @Override
    public void close() throws IOException {
        running = false;
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }

    public User getUser() {
        return user;
    }


}
