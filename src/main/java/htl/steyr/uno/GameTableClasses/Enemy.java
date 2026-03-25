package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.UiStyleUtil;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Enemy implements Serializable {

    private String username;
    private boolean isCurrentTurn;
    private int handSize;
    private Integer playerIndex;
    private boolean isPassive = false;


    public Enemy(String username, boolean isCurrentTurn, int cardCount, Integer playerIndex, boolean isPassive) {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.handSize = cardCount;
        this.playerIndex = playerIndex;
        this.isPassive = isPassive;
    }

    public Enemy(Player player) {
        this(player.getUsername(), player.isCurrentTurn(), player.getHand().size(), player.getPlayerIndex(), player.isPassive());
    }

    public Enemy(String username, boolean isCurrentTurn, int cardCount, Integer playerIndex) {
        this(username, isCurrentTurn, cardCount, playerIndex, false);
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

    public boolean isPassive() {
        return isPassive;
    }

    public void setEnemy(Enemy enemy) {
        this.username = enemy.getUsername();
        this.isCurrentTurn = enemy.isCurrentTurn();
        this.handSize = enemy.getHandSize();
        this.playerIndex = enemy.getPlayerIndex();
        this.isPassive = enemy.isPassive();
    }

    public static void displayTopEnemyHand(StackPane root, Enemy enemy) {
        HBox handBox = new HBox();
        handBox.setSpacing(-50);
        handBox.setAlignment(Pos.TOP_CENTER);

        enemy.displayHandSize(root, enemy, handBox);

        StackPane.setAlignment(handBox, javafx.geometry.Pos.TOP_CENTER); // position on top or wherever
        StackPane.setMargin(handBox, new javafx.geometry.Insets(20)); // optional spacing
        root.getChildren().add(handBox);
    }

    public static void displaySideEnemy(StackPane root,Enemy enemy, int pos) {
        HBox handBox = new HBox();
        handBox.setSpacing(-50);

        //pos can either be 1 or 2

        enemy.displayHandSize(root, enemy, handBox);

        // is left, 2 is right
        if(pos == 1){
            handBox.setAlignment(Pos.CENTER_LEFT);
            StackPane.setAlignment(handBox, Pos.CENTER_LEFT); // position on top or wherever
        }else if(pos == 2){
            handBox.setAlignment(Pos.CENTER_RIGHT);
            StackPane.setAlignment(handBox, Pos.CENTER_RIGHT);
        }

        StackPane.setMargin(handBox, new javafx.geometry.Insets(20)); // optional spacing
        root.getChildren().add(handBox);

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


    public void displayHandSize(StackPane root, Enemy enemy, HBox handBox) {
        for (int i = 0; i < enemy.getHandSize(); i++) {
            Image backImage = new Image(Objects.requireNonNull(Enemy.class.getResourceAsStream("/Uno_Cards/back.png")));
            ImageView iv = new ImageView(backImage);

            iv.setFitWidth(137);
            iv.setFitHeight(192);
            iv.setPreserveRatio(true);

            handBox.getChildren().add(iv);
        }
    }

}




