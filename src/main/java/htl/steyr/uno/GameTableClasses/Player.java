package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidHandException;
import javafx.animation.ScaleTransition;
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

    
    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies) throws InvalidHandException {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.hand.addAll(hand);
        if(hand.size() != 7){
        throw new InvalidHandException("too many or too little Cards");
        }
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




    public void displayPlayerHand(StackPane root, Player player, CardStack middleCardStack) {
        HBox handBox = new HBox();
        handBox.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
        handBox.setSpacing(-100);

        for (Card c : player.getPlayerHand()) {
            String path = "../Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
            ImageView iv = new ImageView(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
            iv.setFitWidth(137);
            iv.setFitHeight(192);
            iv.setPreserveRatio(true);

            StackPane cardPane = new StackPane(iv);
            cardPane.setPrefSize(137, 192);
            cardPane.setStyle(
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
                middleCardStack.layCard(c, cardBtn);
                if (middleCardStack.getTopCard() == c) {
                    player.getPlayerHand().remove(c);
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






}