package htl.steyr.uno.requests.client;

import htl.steyr.uno.requests.server.UserRequestsJoinLobbyResponse;

import java.io.Serializable;

public record UserRequestsJoinLobbyRequest (UserRequestsJoinLobbyResponse response, Boolean isAccepted) implements Serializable {

    @Override
    public String toString() {
        return "UserRequestsJoinLobbyRequest{" +
                "response=" + response +
                ", isAccepted=" + isAccepted +
                '}';
    }
}
