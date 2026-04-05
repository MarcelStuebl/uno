package htl.steyr.uno.requests.client;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * A heartbeat request sent by the client to keep the connection alive.
 * This prevents socket timeout when the client is idle (e.g., waiting in a lobby).
 * 
 * The server should respond with a HeartbeatPongResponse to confirm the connection is still active.
 */
public record HeartbeatPingRequest(Timestamp timestamp) implements Serializable {

    @Override
    public String toString() {
        return "HeartbeatPingRequest{}";
    }
}

