package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record SayUnoRequest(Player player) implements Serializable {

    @Override
    public String toString() {
        return "SayUnoRequest{" +
                "player=" + player().getUsername() +
                '}';
    }
}

