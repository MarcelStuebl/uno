package htl.steyr.uno.GameTableClasses.controller;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.GameTableClasses.Player;
import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.client.Client;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PlayerHandController {
    private static final int MAX_CARDS_PER_ROW = 30;

    private final GameTable gameTable;
    private final Client client;
    private VBox handVBox;
    private HBox ownProfileDock;

    public PlayerHandController(GameTable gameTable, Client client) {
        this.gameTable = gameTable;
        this.client = client;
    }

    public void open() {
        handVBox = new VBox();
        handVBox.setAlignment(Pos.BOTTOM_CENTER);
        handVBox.setSpacing(-200);
        handVBox.setMouseTransparent(false);
        StackPane.setAlignment(handVBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handVBox, new Insets(40));
        gameTable.getRoot().getChildren().add(handVBox);

        ownProfileDock = createOwnProfileDock();
        gameTable.getRoot().getChildren().add(ownProfileDock);
        updatePlayerHandUI();
    }

    public void updatePlayerHandUI() {
        if (handVBox == null) {
            return;
        }

        handVBox.getChildren().clear();
        List<Card> cards = new ArrayList<>(gameTable.getPlayer().getHand());
        int index = 0;
        int rowNumber = 0;

        while (index < cards.size()) {
            HBox row = new HBox();
            row.setAlignment(Pos.BOTTOM_CENTER);
            row.setPickOnBounds(false);

            int cardsInThisRow = Math.min(cards.size() - index, MAX_CARDS_PER_ROW);
            row.setSpacing(calculateSpacing(cardsInThisRow));
            for (int i = 0; i < cardsInThisRow; i++) {
                Button cardButton = createCardButton(cards.get(index++));
                cardButton.setTranslateY(40 * rowNumber);
                row.getChildren().add(cardButton);
            }

            row.setTranslateY(10 * rowNumber++);
            handVBox.getChildren().add(row);
        }

        handVBox.toFront();
        if (ownProfileDock != null) {
            ownProfileDock.toFront();
        }
    }

    public VBox getHandNode() {
        return handVBox;
    }

    public HBox getOwnProfileDock() {
        return ownProfileDock;
    }

    private HBox createOwnProfileDock() {
        HBox emojiBar = new HBox(4);
        emojiBar.setAlignment(Pos.CENTER);
        String[][] reactions = {
                {"🖕", "Mittelfinger"},
                {"❤️", "Herz"},
                {"😠", "Wütend"},
                {"😭", "Weinen"}
        };
        for (String[] reaction : reactions) {
            Button reactionButton = new Button();
            reactionButton.setPadding(Insets.EMPTY);
            reactionButton.getStyleClass().add("game-reaction-button");
            reactionButton.setMinSize(48, 48);
            reactionButton.setPrefSize(48, 48);
            reactionButton.setMaxSize(48, 48);
            reactionButton.setAlignment(Pos.CENTER);
            reactionButton.setAccessibleText(reaction[1]);
            reactionButton.setFocusTraversable(false);
            reactionButton.setOnAction(event -> client.sendEmojiReaction(reaction[0]));

            ImageView emojiImage = createReactionImageView(reaction[0], 36);
            emojiImage.setMouseTransparent(true);
            StackPane reactionButtonStack = new StackPane(reactionButton, emojiImage);
            reactionButtonStack.setAlignment(Pos.CENTER);
            reactionButtonStack.setMinSize(48, 48);
            reactionButtonStack.setPrefSize(48, 48);
            reactionButtonStack.setMaxSize(48, 48);
            emojiBar.getChildren().add(reactionButtonStack);
        }

        HBox dock = new HBox(emojiBar);
        dock.setAlignment(Pos.CENTER_LEFT);
        dock.setPickOnBounds(false);
        dock.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(dock, Pos.BOTTOM_LEFT);
        StackPane.setMargin(dock, new Insets(0, 0, 24, 20));
        return dock;
    }

    public static ImageView createReactionImageView(String emoji, double size) {
        String imagePath = switch (emoji) {
            case "🖕" -> "/htl/steyr/uno/emojis/middlefinger.png";
            case "❤️" -> "/htl/steyr/uno/emojis/heart.png";
            case "😠" -> "/htl/steyr/uno/emojis/angry.png";
            case "😭" -> "/htl/steyr/uno/emojis/crying.png";
            default -> throw new IllegalArgumentException("Unsupported emoji reaction: " + emoji);
        };

        Image image = new Image(Objects.requireNonNull(PlayerHandController.class.getResourceAsStream(imagePath)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        switch (emoji) {
            case "🖕" -> imageView.setTranslateX(size * 11 / 160);
            case "😠" -> imageView.setTranslateY(size / 160);
            case "😭" -> imageView.setTranslateY(-size * 1.5 / 96);
            default -> {
            }
        }
        return imageView;
    }

    private double calculateSpacing(int cardCount) {
        double minSpacing = -140;
        double maxSpacing = -50;
        if (cardCount <= 5) {
            return maxSpacing;
        }
        if (cardCount >= MAX_CARDS_PER_ROW) {
            return minSpacing;
        }
        return maxSpacing - ((cardCount - 5) * (Math.abs(maxSpacing - minSpacing) / (MAX_CARDS_PER_ROW - 5)));
    }

    private Button createCardButton(Card card) {
        Image cardImage = loadCardImage(card);
        ImageView imageView = cardImage == null ? new ImageView() : new ImageView(cardImage);
        imageView.setFitWidth(137);
        imageView.setFitHeight(192);
        imageView.setPreserveRatio(true);
        UiStyleUtil.applyRoundedCardClip(imageView, 137, 192, 18);

        StackPane cardPane = new StackPane(imageView);
        cardPane.setPrefSize(137, 192);
        cardPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        Button cardButton = new Button();
        cardButton.setGraphic(cardPane);
        cardButton.setPadding(Insets.EMPTY);
        cardButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        cardButton.setOnAction(event -> gameTable.getCardStack().layCard(card, cardButton, gameTable.getPlayer()));

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), cardPane);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), cardPane);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.BLACK);

        cardButton.setOnMouseEntered(event -> {
            scaleOut.stop();
            scaleIn.setToX(1.1);
            scaleIn.setToY(1.1);
            scaleIn.playFromStart();
            cardPane.setEffect(shadow);
        });
        cardButton.setOnMouseExited(event -> {
            scaleIn.stop();
            scaleOut.setToX(1);
            scaleOut.setToY(1);
            scaleOut.playFromStart();
            cardPane.setEffect(null);
        });
        return cardButton;
    }

    private Image loadCardImage(Card card) {
        String path = "/htl/steyr/uno/Uno_Cards/" + card.getCardColour() + "/" + card.getCardColour() + card.getCardValue() + ".png";
        Image image = readImage(path);
        if (image != null) {
            return image;
        }
        System.err.println("Warnung: Kartenbild nicht gefunden: " + path);
        String fallbackPath = "/htl/steyr/uno/Uno_Cards/backside.png";
        Image fallbackImage = readImage(fallbackPath);
        if (fallbackImage == null) {
            System.err.println("Warnung: Kartenrueckseite konnte nicht geladen werden: " + fallbackPath);
        }
        return fallbackImage;
    }

    private Image readImage(String path) {
        try (InputStream imageStream = getClass().getResourceAsStream(path)) {
            return imageStream == null ? null : new Image(imageStream);
        } catch (IOException e) {
            System.err.println("Warnung: Bild konnte nicht geladen werden: " + path);
            return null;
        }
    }
}
