package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.requests.server.GameTurnResponse;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Objects;

public class CardStack {
    private Card topCard;
    private StackPane visual;
    private GameTable gameTable;

    public CardStack(GameTable gameTable) {
        this.visual = new StackPane();
        this.visual.setPrefSize(137, 192);
        this.visual.setMaxSize(137, 192);
        this.visual.setStyle("-fx-background-color: transparent;" +
                "-fx-border-color: green;" +
                "-fx-border-width: 6;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;");
        this.gameTable = gameTable;
    }

    public StackPane getVisual() {
        return visual;
    }




    public void layCard(Card c, Button handButton, Player p) {
        if (c == null) return;

        if (!p.isCurrentTurn()) {
            shakeHandButton(handButton);
            return;
        }

        GameTurnResponse turnResponse = getGameTable().getGameTurnResponse();
        Integer currentPenalty = turnResponse != null ? turnResponse.drawPenaltyValue() : 0;
        String currentColor = turnResponse != null ? turnResponse.currentColor() : null;

        // Wenn drawPenaltyValue > 0, darf nur +2 oder +4 gespielt werden!
        if (currentPenalty != null && currentPenalty > 0) {
            boolean isAllowedCard = c.getCardValue() == 12 || c.getCardValue() == 14;
            if (!isAllowedCard) {
                shakeHandButton(handButton);
                return;
            }

            // +2 auf +2: erlaubt
            // +4 auf +4: erlaubt
            // +4 auf +2: erlaubt
            // +2 auf +4: NICHT erlaubt
            Card top = getTopCard();
            if (top != null && top.getCardValue() == 14 && c.getCardValue() == 12) {
                // Versuchen, +2 auf +4 zu legen - NICHT erlaubt!
                shakeHandButton(handButton);
                return;
            }
        }

        Card top = getTopCard();

        // if no card, just lay card
        if (top == null) {
            // Wenn es eine black card ist, Farbe auswählen
            if (c.getCardColour().equals("black")) {
                showColorSelection(c, p);
                return;
            }
            addToStack(c);
            getGameTable().getGameLogic().playCard(c, 0);
            return;
        }

        // Wenn drawPenaltyValue == 0 und die oberste Karte ist eine Penalty-Karte,
        // dann hat der vorherige Spieler die Karten abgehoben. 
        // Wenn currentColor gesetzt ist, muss trotzdem die aktuelle Farbe beachtet werden
        if (top.getCardValue() >= 12 && currentPenalty != null && currentPenalty == 0 && (currentColor == null || currentColor.isBlank())) {
            if (c.getCardColour().equals("black")) {
                showColorSelection(c, p);
                return;
            }
            addToStack(c);
            getGameTable().getGameLogic().playCard(c, 0);
            return;
        }

        if (c.getCardColour().equals("black")) {
            // Wenn currentColor gesetzt ist, darf der nächste Spieler nur +2 oder +4 spielen
            // Ein Farbwechsel ist erlaubt, wenn die Penalty schon beglichen wurde
            if (currentColor != null && !currentColor.isBlank() && c.getCardValue() == 13 && currentPenalty != null && currentPenalty > 0) {
                shakeHandButton(handButton);
                return;
            }
            showColorSelection(c, p);
            return;
        }

        String effectiveTopColor = top.getCardColour();
        if (currentColor != null && !currentColor.isBlank()) {
            effectiveTopColor = currentColor;
        } else if (top.getCardColour().equals("black") && top.getChosenColour() != null && !top.getChosenColour().isBlank()) {
            effectiveTopColor = top.getChosenColour();
        }

        // check if colour or value matches
        boolean colorMatch = c.getCardColour().equals(effectiveTopColor);
        boolean valueMatch = c.getCardValue() == top.getCardValue();

        if (!colorMatch && !valueMatch) {
            shakeHandButton(handButton);
            return;
        }

        // card valid, lay card
        addToStack(c);

        p.removeCardFromHand(c);
        
        // Wenn der Spieler keine Karten mehr hat, setze ihn auf passiv
        if (p.getHand().isEmpty()) {
            p.setPassive(true);
        }
        
        getGameTable().updatePlayerHandUI();

        Integer drawPenaltyForThisCard = 0;
        if (c.getCardValue() == 12) {
            drawPenaltyForThisCard = (currentPenalty != null ? currentPenalty : 0) + 2;
        }

        getGameTable().getGameLogic().playCard(c, drawPenaltyForThisCard);
    }


    private void showColorSelection(Card card, Player player) {
        Platform.runLater(() -> {
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            overlay.setPrefSize(gameTable.getRoot().getWidth(), gameTable.getRoot().getHeight());

            VBox contentBox = new VBox();
            contentBox.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #00ff00; -fx-border-width: 3; -fx-padding: 30; -fx-background-radius: 15;");
            contentBox.setSpacing(20);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setMaxWidth(500);

            Text title = new Text("Waehle eine Farbe:");
            title.setFont(new Font(28));
            title.setFill(Color.WHITE);
            contentBox.getChildren().add(title);

            HBox buttonBox = new HBox();
            buttonBox.setSpacing(15);
            buttonBox.setAlignment(Pos.CENTER);

            String[] colorList = {"red", "yellow", "green", "blue"};
            String[] colorNames = {"ROT", "GELB", "GREEN", "BLAU"};
            String[] colorCodes = {"#ff0000", "#ffff00", "#00ff00", "#0000ff"};

            for (int i = 0; i < colorList.length; i++) {
                String selectedColor = colorList[i];
                String btnText = colorNames[i];
                String btnColor = colorCodes[i];

                Button colorBtn = new Button(btnText);
                colorBtn.setPrefSize(100, 100);
                colorBtn.setStyle("-fx-font-size: 16; -fx-padding: 20; -fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 10; -fx-background-radius: 10;");

                colorBtn.setOnAction(e -> {
                    // Speichere die gewählte Farbe in der Karte
                    card.setChosenColour(selectedColor);

                    // Berechne den korrekten drawPenaltyValue
                    int drawPenaltyForThisCard = 0;
                    Integer currentPenalty = getGameTable().getGameTurnResponse() != null ?
                                            getGameTable().getGameTurnResponse().drawPenaltyValue() : 0;

                    if (card.getCardValue() == 13) {
                        // Farbwahl: setzt drawPenalty auf 0
                        drawPenaltyForThisCard = 0;
                    } else if (card.getCardValue() == 14) {
                        // +4: ADDIERT 4 zur bestehenden Penalty!
                        drawPenaltyForThisCard = (currentPenalty != null ? currentPenalty : 0) + 4;
                    }

                    // Entferne die Karte ZUERST von der Hand
                    // Verwende removeCardFromHand um sicherzustellen, dass nur eine Karte entfernt wird
                    player.removeCardFromHand(card);

                    if (player.getHand().isEmpty()) {
                        player.setPassive(true);
                    }

                    addToStack(card);
                    getGameTable().getGameLogic().playCard(card, drawPenaltyForThisCard);
                    getGameTable().updatePlayerHandUI();
                    gameTable.getRoot().getChildren().remove(overlay);
                });

                buttonBox.getChildren().add(colorBtn);
            }

            contentBox.getChildren().add(buttonBox);
            overlay.getChildren().add(contentBox);
            gameTable.getRoot().getChildren().add(overlay);
        });
    }

    public void addToStack(Card c) {
        // save card
        this.topCard = c;

        // update visually
        visual.getChildren().clear();

        String path;

        if (c.getCardColour().equals("black") && c.getChosenColour() != null && !c.getChosenColour().isBlank()) {
            path = "/htl/steyr/uno/Uno_Cards/black/black" + c.getCardValue() + "-" + c.getChosenColour() + ".png";
        } else {
            path = "/htl/steyr/uno/Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
        }

        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        iv.setFitWidth(137);
        iv.setFitHeight(192);
        iv.setPreserveRatio(true);
        UiStyleUtil.applyRoundedCardClip(iv, 137, 192, 18);
        visual.getChildren().add(iv);

        // small pop animation
        ScaleTransition st = new ScaleTransition(Duration.millis(200), visual);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    // makes the button that is currently clicked shake if it is layable
    public void shakeHandButton(Button cardBtn) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), cardBtn);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.playFromStart();
    }


    public Card getTopCard() {
        return topCard;
    }

    private GameTable getGameTable() {
        return gameTable;
    }
}