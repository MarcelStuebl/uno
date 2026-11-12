package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public record UserRequestsJoinLobbyResponse(User user) implements Serializable {

    @Override
    public String toString() {
        return "UserJoinLobbyRequestResponse{" +
                "user=" + user +
                '}';
    }
}
