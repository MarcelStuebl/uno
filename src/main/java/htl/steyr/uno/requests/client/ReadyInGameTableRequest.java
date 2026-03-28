package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record ReadyInGameTableRequest(Player player) implements Serializable {

    @Override
    public String toString() {
        return "ReadyInGameTableRequest{" +
                "player=" + player().getUsername() +
                '}';
    }
}