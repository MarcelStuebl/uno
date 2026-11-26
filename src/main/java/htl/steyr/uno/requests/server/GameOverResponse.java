package htl.steyr.uno.requests.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import htl.steyr.uno.GameTableClasses.Player;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameOverResponse implements Serializable {
    
    @Getter @Setter private ArrayList<Player> players;
    private final Set<String> leftPlayers = new HashSet<>();
    
    @JsonCreator
    public GameOverResponse(@JsonProperty("players") ArrayList<Player> players,
                            @JsonProperty("leftPlayers") ArrayList<String> leftPlayers) {
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

    public boolean isLeftPlayer(String username) {
        return username != null && leftPlayers.contains(username);
    }

    public Set<String> getLeftPlayers() {
        return new HashSet<>(leftPlayers);
    }
    
    public Player getPlayerByPosition(Integer position) {
        return players.get(position);
    }
    
    public void addPlayer(Player player) {
        this.players.add(player);
    }
    
    
    
}
