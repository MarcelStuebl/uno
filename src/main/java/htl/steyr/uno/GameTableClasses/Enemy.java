package htl.steyr.uno.GameTableClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Data
public class Enemy implements Serializable {

    private String username;
    private boolean isCurrentTurn;
    private int handSize;
    private Integer playerIndex;
    private boolean isPassive = false;
    @EqualsAndHashCode.Exclude @ToString.Exclude private byte[] imageBytes;


    @JsonCreator
    public Enemy(@JsonProperty("username") String username,
                 @JsonProperty("currentTurn") boolean isCurrentTurn,
                 @JsonProperty("handSize") int cardCount,
                 @JsonProperty("playerIndex") Integer playerIndex,
                 @JsonProperty("passive") boolean isPassive,
                 @JsonProperty("imageBytes") byte[] imageBytes) {
        setUsername(username);
        setCurrentTurn(isCurrentTurn);
        setHandSize(cardCount);
        setPlayerIndex(playerIndex);
        setPassive(isPassive);
        setImageBytes(imageBytes);
    }

    public Enemy(String username, boolean isCurrentTurn, int cardCount, Integer playerIndex, byte[] imageBytes) {
        this(username, isCurrentTurn, cardCount, playerIndex, false, imageBytes);
    }

    public Enemy(Player player) {
        this(player.getUsername(), player.isCurrentTurn(), player.getHand().size(), player.getPlayerIndex(), player.isPassive(), player.getImageBytes());
    }


    public void setEnemy(Enemy enemy) {
        setUsername(enemy.getUsername());
        setCurrentTurn(enemy.isCurrentTurn());
        setHandSize(enemy.getHandSize());
        setPlayerIndex(enemy.getPlayerIndex());
        setPassive(enemy.isPassive());
        setImageBytes(enemy.getImageBytes());
    }

}

