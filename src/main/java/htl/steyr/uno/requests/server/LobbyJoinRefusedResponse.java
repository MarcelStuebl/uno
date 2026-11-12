package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

/**
 * error: 0 = normal, 1 = join refused by lobby owner, 2 = join timed out
 *
 * @param user
 * @param lobbyInfo
 * @param error
 */

public record LobbyJoinRefusedResponse(User user, LobbyInfoResponse lobbyInfo, Integer error) implements Serializable {

    @Override
    public String toString() {
        return "LobbyJoinRefusedResponse{" +
                "username='" + user().getUsername() +
                "'}";
    }
}