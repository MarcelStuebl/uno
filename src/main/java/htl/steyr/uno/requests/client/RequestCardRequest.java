package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record RequestCardRequest(Player player, int amount) implements Serializable {

    @Override
    public String toString() {
        return "RequestCardRequest{" +
                "player_username=" + player().getUsername() +
                ", ammount=" + amount() +
                '}';
    }
}