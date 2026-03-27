package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.User;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Player implements Serializable {
    private String username;
    private boolean isCurrentTurn;
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private Integer playerIndex;
    private boolean isReady = false;
    private boolean isPassive;


    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies, Integer playerIndex, boolean isPassive) {
        setUsername(username);
        setCurrentTurn(isCurrentTurn);
        getHand().addAll(hand);
        setEnemies(enemies);
        setPlayerIndex(playerIndex);
        setPassive(isPassive);
        sortHand();
    }

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies, Integer playerIndex){
        this(username, isCurrentTurn, hand, enemies, playerIndex, false);
    }

    public Player(User user) {
        this(user.getUsername(), false, new ArrayList<>(), new ArrayList<>(), -1, false);
    }



    private void sortHand() {
        this.hand.sort((c1, c2) -> {

            // Farben-Reihenfolge
            int colorCompare = getColorOrder(c1.getCardColour())
                    - getColorOrder(c2.getCardColour());

            if (colorCompare != 0) return colorCompare;

            // Dann einfach nach Wert sortieren
            return c1.getCardValue() - c2.getCardValue();
        });
    }

    private int getColorOrder(String color) {
        return switch (color) {
            case "red" -> 0;
            case "green" -> 1;
            case "yellow" -> 2;
            case "blue" -> 3;
            case "black" -> 4;
            default -> 5;
        };
    }


    public void displayPlayerHand(StackPane root, Player player, CardStack middleCardStack) {
        HBox handBox = new HBox();
        handBox.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
        handBox.setSpacing(-100);

        for (Card c : player.getHand()) {
            String path = "../Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
            ImageView iv = new ImageView(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
            iv.setFitWidth(137);
            iv.setFitHeight(192);
            iv.setPreserveRatio(true);
            UiStyleUtil.applyRoundedCardClip(iv, 137, 192, 18);

            StackPane cardPane = new StackPane(iv);
            cardPane.setPrefSize(137, 192);
            cardPane.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-border-color: green;" +
                            "-fx-border-width: 6;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;"
            );

            Button cardBtn = new Button();
            cardBtn.setGraphic(cardPane);
            cardBtn.setPadding(javafx.geometry.Insets.EMPTY);
            cardBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            cardBtn.setOnAction(e -> {
                middleCardStack.layCard(c, cardBtn, player);
                if (middleCardStack.getTopCard() == c) {
                    player.getHand().remove(c);
                    handBox.getChildren().remove(cardBtn);
                }
            });

            ScaleTransition stEnter = new ScaleTransition(Duration.millis(200), cardPane);
            ScaleTransition stExit = new ScaleTransition(Duration.millis(200), cardPane);

            DropShadow shadow = new DropShadow();
            shadow.setRadius(15);
            shadow.setOffsetX(0);
            shadow.setOffsetY(0);
            shadow.setColor(Color.BLACK);

            cardBtn.setOnMouseEntered(e -> {
                stExit.stop();
                stEnter.setToX(1.1);
                stEnter.setToY(1.1);
                stEnter.playFromStart();
                cardPane.setEffect(shadow);
            });

            cardBtn.setOnMouseExited(e -> {
                stEnter.stop();
                stExit.setToX(1.0);
                stExit.setToY(1.0);
                stExit.playFromStart();
                cardPane.setEffect(null);
            });

            handBox.getChildren().add(cardBtn);
        }

        StackPane.setAlignment(handBox, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(handBox, new javafx.geometry.Insets(40));
        root.getChildren().add(handBox);
    }



    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", isCurrentTurn=" + isCurrentTurn +
                ", hand=" + hand +
                ", enemies=" + enemies +
                ", playerIndex=" + playerIndex +
                ", isReady=" + isReady +
                ", isPassive=" + isPassive +
                '}';
    }


    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex = playerIndex;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isPassive() {
        return isPassive;
    }
    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }
    public void setCurrentTurn(boolean currentTurn) {
        this.isCurrentTurn = currentTurn;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }
    public void setEnemies(ArrayList<Enemy> enemies) {
        this.enemies.clear();
        this.enemies.addAll(enemies);
    }

    public Enemy getEnemyByUsername(String username) {
        for (Enemy e : this.enemies) {
            if (e.getUsername().equals(username)) {
                return e;
            }
        }
        return null;
    }

    public ArrayList<Card> getHand() {
        return this.hand;
    }

    public void addCardToHand(Card card) {
        this.hand.add(card);
        sortHand();
    }
    public void removeCardFromHand(Card card) {
        this.hand.remove(card);
    }

}