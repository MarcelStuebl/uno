package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameOverResponse implements Serializable {
    
    private ArrayList<Player> players;
    private final Set<String> leftPlayers = new HashSet<>();
    
    public GameOverResponse(ArrayList<Player> players, ArrayList<String> leftPlayers) {
        setPlayers(players);
        if (leftPlayers != null) {
            this.leftPlayers.addAll(leftPlayers);
        }
    }


    @Override
    public String toString() {
        return "AddCardResponse{" +
                "player.size='" + getPlayers().size() +
                "'}";
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean isLeftPlayer(String username) {
        return username != null && leftPlayers.contains(username);
    }

    public Set<String> getLeftPlayers() {
        return new HashSet<>(leftPlayers);
    }
    
    public Player getPlayerByPosition(Integer position) {
        return players.get(position);
    }
    
    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }
    
    public void addPlayer(Player player) {
        this.players.add(player);
    }
    
    
    
}
