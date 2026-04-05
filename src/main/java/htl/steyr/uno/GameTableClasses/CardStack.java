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
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Objects;

public class CardStack {
    private Card topCard;
    private StackPane visual;
    private GameTable gameTable;
    private boolean isEmpty = false;

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
                shakeHandButton(handButton);
                return;
            }
        }

        Card top = getTopCard();

        // Keine Karte auf dem Stapel — erste Karte legen
        if (top == null) {
            if (c.getCardColour().equals("black")) {
                showColorSelection(c, p);
                return;
            }
            addToStack(c);
            p.removeCardFromHand(c);
            if (p.getHand().isEmpty()) {
                p.setPassive(true);
            }
            getGameTable().updatePlayerHandUI();
            if (p.getHand().size() == 1) {
                getGameTable().startUnoCountdown();
            }
            getGameTable().getGameLogic().playCard(c, 0);
            return;
        }

        // Wenn drawPenaltyValue == 0 und die oberste Karte ist eine Penalty-Karte,
        // dann hat der vorherige Spieler die Karten bereits abgehoben.
        // FIX: startUnoCountdown() war in diesem Branch vergessen worden.
        if (top.getCardValue() >= 12 && currentPenalty != null && currentPenalty == 0 && (currentColor == null || currentColor.isBlank())) {
            if (c.getCardColour().equals("black")) {
                showColorSelection(c, p);
                return;
            }
            addToStack(c);
            p.removeCardFromHand(c);
            if (p.getHand().isEmpty()) {
                p.setPassive(true);
            }
            getGameTable().updatePlayerHandUI();
            if (p.getHand().size() == 1) {
                getGameTable().startUnoCountdown(); // FIX: war hier vergessen
            }
            getGameTable().getGameLogic().playCard(c, 0);
            return;
        }

        if (c.getCardColour().equals("black")) {
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

        boolean colorMatch = c.getCardColour().equals(effectiveTopColor);
        boolean valueMatch = c.getCardValue() == top.getCardValue();

        if (!colorMatch && !valueMatch) {
            shakeHandButton(handButton);
            return;
        }

        // Karte ist gültig — legen
        addToStack(c);
        p.removeCardFromHand(c);

        if (p.getHand().isEmpty()) {
            p.setPassive(true);
        }

        getGameTable().updatePlayerHandUI();

        Integer drawPenaltyForThisCard = 0;
        if (c.getCardValue() == 12) {
            drawPenaltyForThisCard = (currentPenalty != null ? currentPenalty : 0) + 2;
        }

        if (p.getHand().size() == 1) {
            getGameTable().startUnoCountdown();
        }

        getGameTable().getGameLogic().playCard(c, drawPenaltyForThisCard);
    }


    private void showColorSelection(Card card, Player player) {
        Platform.runLater(() -> {

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(10,10,10,0.8);");

            VBox container = new VBox(30);
            container.setAlignment(Pos.CENTER);

            Text title = new Text("FARBE WÄHLEN");
            title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
            title.setFill(Color.WHITE);

            HBox cardsBox = new HBox(25);
            cardsBox.setAlignment(Pos.CENTER);

            String[] colorList = {"red", "yellow", "green", "blue"};
            String[] colorNames = {"ROT", "GELB", "GRÜN", "BLAU"};
            String[] colorCodes = {"#c62828", "#f9a825", "#2e7d32", "#1565c0"};

            for (int i = 0; i < colorList.length; i++) {
                String selectedColor = colorList[i];
                String name = colorNames[i];
                String color = colorCodes[i];

                StackPane cardTile = new StackPane();
                cardTile.setPrefSize(120, 160);
                cardTile.setStyle("""
                        -fx-background-radius: 15;
                        -fx-border-radius: 15;
                        -fx-border-color: white;
                        -fx-border-width: 2;
                        """ + "-fx-background-color: " + color + ";");

                Text label = new Text(name);
                label.setFill(Color.WHITE);
                label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                cardTile.getChildren().add(label);

                cardTile.setOnMouseEntered(e -> {
                    cardTile.setScaleX(1.1);
                    cardTile.setScaleY(1.1);
                    cardTile.setOpacity(0.9);
                });
                cardTile.setOnMouseExited(e -> {
                    cardTile.setScaleX(1);
                    cardTile.setScaleY(1);
                    cardTile.setOpacity(1);
                });

                cardTile.setOnMouseClicked(e -> {
                    card.setChosenColour(selectedColor);

                    int drawPenaltyForThisCard = 0;
                    Integer currentPenalty = getGameTable().getGameTurnResponse() != null ?
                            getGameTable().getGameTurnResponse().drawPenaltyValue() : 0;

                    if (card.getCardValue() == 13) {
                        drawPenaltyForThisCard = 0;
                    } else if (card.getCardValue() == 14) {
                        drawPenaltyForThisCard = (currentPenalty != null ? currentPenalty : 0) + 4;
                    }

                    player.removeCardFromHand(card);
                    addToStack(card);
                    if (player.getHand().size() == 1) {
                        getGameTable().startUnoCountdown();
                    }
                    getGameTable().getGameLogic().playCard(card, drawPenaltyForThisCard);
                    getGameTable().updatePlayerHandUI();

                    gameTable.getRoot().getChildren().remove(overlay);
                });

                cardsBox.getChildren().add(cardTile);
            }

            container.getChildren().addAll(title, cardsBox);
            overlay.getChildren().add(container);
            gameTable.getRoot().getChildren().add(overlay);
        });
    }

    public void addToStack(Card c) {
        this.topCard = c;

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

        ScaleTransition st = new ScaleTransition(Duration.millis(200), visual);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

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