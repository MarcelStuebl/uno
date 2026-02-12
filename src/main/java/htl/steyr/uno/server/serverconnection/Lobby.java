package htl.steyr.uno.server.serverconnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lobby {

    private final List<ServerSocketConnection> connections = Collections.synchronizedList(new ArrayList<>());




        public void addConnection(ServerSocketConnection connection) {
            connections.add(connection);
        }

        public void removeConnection(ServerSocketConnection connection) {
            connections.remove(connection);
        }

        public List<ServerSocketConnection> getConnections() {
            return connections;
        }


}




