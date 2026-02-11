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


    public void start() throws IOException {
        System.out.println("Server started on port " + port);
        running = true;
        serverSocket = new ServerSocket(port);

        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket s = serverSocket.accept();

                    ServerSocketConnection client = new ServerSocketConnection(s);

                    client.addSubscriber(event -> {
                        String line = String.valueOf(event.message());
                        System.out.println("[" + s.getRemoteSocketAddress() + "] " + line);
                        client.sendMessage("Server received: " + line);
                    });

                    client.startReceiving();

                    connections.add(client);
                    System.out.println("New connection from " + s.getRemoteSocketAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        acceptThread.start();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    public void stop() throws IOException {
        System.out.println("Server stopped");
        running = false;
        acceptThread.interrupt();
        serverSocket.close();
    }

}




