package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Enemy;

import java.io.Serializable;

public record UpdateEnemyResponse(Enemy enemy) implements Serializable {

    @Override
    public String toString() {
        return "UpdateEnemyResponse{" +
                "enemy_username=" + enemy.getUsername() +
                '}';
    }

}
