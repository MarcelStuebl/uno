package htl.steyr.uno.GameTableClasses;

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


    public Enemy(String username, boolean isCurrentTurn, int cardCount, Integer playerIndex, boolean isPassive, byte[] imageBytes) {
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










    //needs immediate fixing :3 (please)
    public void displayEnemyHand(StackPane root, Enemy enemy, int position) {

        String cardBackside = "../Uno_Cards/backside.png";
        double scale = 0.5;
        double cardWidth = 137 * scale;
        double cardHeight = 192 * scale;
        final double overlap = 20; // constant for the amount of overlap on the cards

        Runnable drawCards = () -> {
            Pane handPane = new Pane();
            handPane.setMouseTransparent(true);

            int count = enemy.getHandSize();

            switch(position) {
                case 2: // oben
                    double totalWidth = count * (cardWidth - overlap) + overlap;
                    double startX = (root.getWidth() - totalWidth) / 2;

                    for (int i = 0; i < count; i++) {
                        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(cardBackside))));
                        iv.setFitWidth(cardWidth);
                        iv.setFitHeight(cardHeight);
                        iv.setPreserveRatio(true);
                        UiStyleUtil.applyRoundedCardClip(iv, cardWidth, cardHeight, 14);
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
                        UiStyleUtil.applyRoundedCardClip(iv, cardWidth, cardHeight, 14);
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
                        UiStyleUtil.applyRoundedCardClip(iv, cardWidth, cardHeight, 14);
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




