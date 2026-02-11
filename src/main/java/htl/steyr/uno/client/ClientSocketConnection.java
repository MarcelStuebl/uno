package htl.steyr.uno.client;

import htl.steyr.uno.User;

import java.io.*;
import java.net.Socket;

public class ClientSocketConnection implements Closeable {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receiveThread;
    private volatile boolean running;

    public ClientSocketConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

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
                        System.out.println(user);
                    }
                }
            } catch (Exception e) {
                if (running) System.out.println("Receive error: " + e.getMessage());
            }
        });
        receiveThread.start();
    }

    @Override
    public void close() throws IOException {
        running = false;
        try { socket.close(); } catch (Exception ignored) {}
    }
}
