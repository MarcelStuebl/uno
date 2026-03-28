package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Enemy;

import java.io.Serializable;
import java.util.ArrayList;

public record StartGameResponse(ArrayList<Enemy> enemies) implements Serializable {

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "enemy_username='" + enemies().getFirst().getUsername() +
                "'}";
    }
}