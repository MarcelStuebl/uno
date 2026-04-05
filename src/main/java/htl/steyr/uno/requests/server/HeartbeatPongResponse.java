package htl.steyr.uno.requests.server;

import java.io.Serializable;

/**
 * A heartbeat response sent by the server in response to a HeartbeatPingRequest.
 * This confirms that the connection is still active and prevents socket timeout.
 */
public record HeartbeatPongResponse() implements Serializable {

    @Override
    public String toString() {
        return "HeartbeatPongResponse{}";
    }
}

