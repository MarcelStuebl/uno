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




    public static Pane createEnemyHand(Enemy enemy, double angleRad) {

        Pane pane = new Pane();

        String cardBackside = "/Uno_Cards/backside.png";

        double scale = 0.5;
        double cardWidth = 137 * scale;
        double cardHeight = 192 * scale;
        double overlap = 20;

        int count = enemy.getCardCount();
        double totalWidth = cardWidth + (count - 1) * overlap;

        for (int i = 0; i < count; i++) {

            ImageView iv = new ImageView(new Image(
                    Objects.requireNonNull(Enemy.class.getResourceAsStream(cardBackside))
            ));

            iv.setFitWidth(cardWidth);
            iv.setFitHeight(cardHeight);

            double x = i * overlap - totalWidth / 2;
            iv.setLayoutX(x);

            // 👉 Karten zeigen zur Tischmitte
            double angleDeg = Math.toDegrees(angleRad);
            iv.setRotate(angleDeg + 90);

            pane.getChildren().add(iv);
        }

        return pane;
    }



}




