package htl.steyr.uno.GameTableClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import htl.steyr.uno.UiStyleUtil;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.Serializable;
import java.util.Objects;

public class Enemy implements Serializable {

    private String username;
    private boolean isCurrentTurn;
    private int handSize;
    private Integer playerIndex;
    private boolean isPassive = false;
    private byte[] imageBytes;


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


    @Override
    public String toString() {
        return "Enemy{" +
                "username='" + username + '\'' +
                ", isCurrentTurn=" + isCurrentTurn +
                ", handSize=" + handSize +
                ", playerIndex=" + playerIndex +
                ", isPassive=" + isPassive +
                '}';
    }

    public void setEnemy(Enemy enemy) {
        setUsername(enemy.getUsername());
        setCurrentTurn(enemy.isCurrentTurn());
        setHandSize(enemy.getHandSize());
        setPlayerIndex(enemy.getPlayerIndex());
        setPassive(enemy.isPassive());
        setImageBytes(enemy.getImageBytes());
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }
    public void setCurrentTurn(boolean currentTurn) {
        isCurrentTurn = currentTurn;
    }

    public int getHandSize() {
        return handSize;
    }
    public void setHandSize(int handSize) {
        this.handSize = handSize;
    }
    public void incrementCardCount(int count) {
        this.handSize += count;
    }
    public void decrementCardCount(int count) {
        this.handSize -= count;
    }

    public Integer getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex = playerIndex;
    }

    public boolean isPassive() {
        return isPassive;
    }
    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

}



