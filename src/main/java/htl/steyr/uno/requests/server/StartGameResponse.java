package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Enemy;
import htl.steyr.uno.User;

import java.io.Serializable;
import java.util.ArrayList;

public class StartGameResponse implements Serializable {


    private ArrayList<Enemy> enemies;

    public StartGameResponse(ArrayList<Enemy> enemies) {
        setEnemies(enemies);
    }

    @Override
    public String toString() {
        return "StartGameRequest{" +
                "enemy_username='" + getEnemies().getFirst().getUsername() +
                "'}";
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }
    public void setEnemies(ArrayList<Enemy> enemies) {
        this.enemies = enemies;
    }


}
