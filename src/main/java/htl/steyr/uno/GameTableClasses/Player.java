package htl.steyr.uno.GameTableClasses;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Objects;

public class Player {
    private final String username;
    private final boolean isCurrentTurn;
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies) {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.hand.addAll(hand);
        this.enemies.addAll(enemies);
        sortHand();
    }


    public String getUsername() {
        return this.username;
    }

    public ArrayList<Card> getPlayerHand() {
        return this.hand;
    }

    public void setPlayerHand(ArrayList<Card> arr) {
        this.hand.clear();
        this.hand.addAll(arr);
    }
    public void addCardToHand(Card card) {
        this.hand.add(card);
    }
    public void removeCardFromHand(Card card) {
        this.hand.remove(card);
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }

    public Enemy getEnemy(int index) {
        return this.enemies.get(index);
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
        switch (color) {
            case "red": return 0;
            case "green": return 1;
            case "yellow": return 2;
            case "blue": return 3;
            case "black": return 4;
            default: return 5;
        }
    }

    public void testPrintHand() {
        System.out.println("Hand von " + username + ":");

        for (Card card : hand) {
            System.out.println(
                    card.getCardColour() + " " + card.getCardValue()
            );
        }

        System.out.println("----------------------");
    }


    public void showPlayerHand(StackPane root, Player player) {

        HBox handBox = new HBox();
        handBox.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
        handBox.setSpacing(-80); // overlap cards

        for (Card c : player.getPlayerHand()) {

            Button cardBtn = new Button();

            // picture of the card
            String path = "../testMHORETH/yellowCards/" + c.getCardColour() + c.getCardValue() + ".png";
            ImageView iv = new ImageView(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
            iv.setFitWidth(100);
            iv.setFitHeight(150);
            iv.setPreserveRatio(true);

            cardBtn.setGraphic(iv);
            cardBtn.setPadding(javafx.geometry.Insets.EMPTY);
            cardBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            // click event TEST
            cardBtn.setOnAction(e -> System.out.println("Karte gespielt: " + c.getCardColour() + " " + c.getCardValue()));

            // Scale transitions for hover
            ScaleTransition stEnter = new ScaleTransition(Duration.millis(200), cardBtn);
            ScaleTransition stExit = new ScaleTransition(Duration.millis(200), cardBtn);

            // DropShadow effect
            DropShadow shadow = new DropShadow();
            shadow.setRadius(15);
            shadow.setOffsetX(0);
            shadow.setOffsetY(0);
            shadow.setColor(Color.BLACK);

            cardBtn.setOnMouseEntered(e -> {
                stExit.stop();
                stEnter.setToX(1.1); // 20% larger
                stEnter.setToY(1.1);
                stEnter.playFromStart();

                // Apply shadow
                cardBtn.setEffect(shadow);
            });

            cardBtn.setOnMouseExited(e -> {
                stEnter.stop();
                stExit.setToX(1.0); // back to normal size
                stExit.setToY(1.0);
                stExit.playFromStart();

                // Remove shadow
                cardBtn.setEffect(null);
            });

            handBox.getChildren().add(cardBtn);
        }

        StackPane.setAlignment(handBox, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(handBox, new javafx.geometry.Insets(40));

        root.getChildren().add(handBox);
    }

}
