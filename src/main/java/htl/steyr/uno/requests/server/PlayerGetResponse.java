package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record PlayerGetResponse(Player player) implements Serializable {

    @Override
    public String toString() {
        return "PlayerGetResponse{" +
                "player_username='" + player().getUsername() +
                "'}";
    }
}