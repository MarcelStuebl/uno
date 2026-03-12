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

    public void displayEnemyHand(StackPane root, Enemy enemy, int position) {

        String cardBackside = "../Uno_Cards/backside.png";
        double scale = 0.5;
        double cardWidth = 137 * scale;
        double cardHeight = 192 * scale;
        final double overlap = 20; // constant for the amount of overlap on the cards

        Runnable drawCards = () -> {
            Pane handPane = new Pane();
            handPane.setMouseTransparent(true);

            int count = enemy.getCardCount();

            switch(position) {
                case 2: // oben
                    double totalWidth = count * (cardWidth - overlap) + overlap;
                    double startX = (root.getWidth() - totalWidth) / 2;

                    for (int i = 0; i < count; i++) {
                        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(cardBackside))));
                        iv.setFitWidth(cardWidth);
                        iv.setFitHeight(cardHeight);
                        iv.setPreserveRatio(true);
                        iv.setRotate(180);

                        iv.setLayoutX(startX + i * (cardWidth - overlap));
                        iv.setLayoutY(0);

                        handPane.getChildren().add(iv);
                    }
                    StackPane.setAlignment(handPane, Pos.TOP_CENTER);
                    break;

                case 1: // links
                    double totalHeightLeft = count * (cardHeight - overlap) + overlap;
                    double startYLeft = (root.getHeight() - totalHeightLeft) / 2;

                    for (int i = 0; i < count; i++) {
                        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(cardBackside))));
                        iv.setFitWidth(cardWidth);
                        iv.setFitHeight(cardHeight);
                        iv.setPreserveRatio(true);
                        iv.setRotate(90); // rotate

                        iv.setLayoutX(0);
                        iv.setLayoutY(startYLeft + i * (cardHeight - overlap));

                        handPane.getChildren().add(iv);
                    }
                    StackPane.setAlignment(handPane, Pos.CENTER_LEFT);
                    break;

                case 3: // rechts
                    double totalHeightRight = count * (cardHeight - overlap) + overlap;
                    double startYRight = (root.getHeight() - totalHeightRight) / 2;

                    for (int i = 0; i < count; i++) {
                        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(cardBackside))));
                        iv.setFitWidth(cardWidth);
                        iv.setFitHeight(cardHeight);
                        iv.setPreserveRatio(true);
                        iv.setRotate(-90 * (i - count/2.0));

                        iv.setLayoutX(root.getWidth() - cardWidth);
                        iv.setLayoutY(startYRight + i * (cardHeight - overlap));

                        handPane.getChildren().add(iv);
                    }
                    StackPane.setAlignment(handPane, Pos.CENTER_RIGHT);
                    break;
            }

            root.getChildren().add(handPane);

        };
        drawCards.run();
    }

}




