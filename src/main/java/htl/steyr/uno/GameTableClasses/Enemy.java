package htl.steyr.uno.GameTableClasses;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class Enemy {

    private String username;
    private boolean isCurrentTurn;
    private int cardCount;
    private int playerNumber;


    public Enemy(String username, boolean isCurrentTurn, int cardCount, int totalPlayers) {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.cardCount = cardCount;
        this.playerNumber = playerNumber;
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

    public int getCardCount() {
        return cardCount;
    }
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
    public void incrementCardCount(int count) {
        this.cardCount += count;
    }
    public void decrementCardCount(int count) {
        this.cardCount -= count;
    }



}




