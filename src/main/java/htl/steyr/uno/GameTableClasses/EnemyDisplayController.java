package htl.steyr.uno.GameTableClasses;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class EnemyDisplayController {

    @FXML private Label usernameLabel;
    @FXML private ImageView profileImageView;
    @FXML private Pane cardPane;

    private static final double CARD_W = 28.0;
    private static final double CARD_H = 44.0;
    private static final double PANE_MAX_W = 150.0;

    private static final String CARD_IMAGE_PATH = "/htl/steyr/uno/Uno_Cards/backside.png";

    private static Image cachedCardImage = null;

    private int currentCardCount = 0;

    @FXML
    private void initialize() {
        loadCardImageIfNeeded();
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    public String getUsername() {
        return usernameLabel.getText();
    }

    public void setProfileImage(Image image) {
        profileImageView.setImage(image);
    }


    public void setCardCount(int count) {
        currentCardCount = Math.max(0, count);
        cardPane.getChildren().clear();
        if (currentCardCount == 0) return;

        double offset = (currentCardCount == 1) ? 0 : Math.min(CARD_W, (PANE_MAX_W - CARD_W) / (currentCardCount - 1));

        for (int i = 0; i < currentCardCount; i++) {
            ImageView card = new ImageView();
            card.setFitWidth(CARD_W);
            card.setFitHeight(CARD_H);
            card.setPreserveRatio(false);
            card.setSmooth(true);
            card.getStyleClass().add("uno-card-back");

            if (cachedCardImage != null) {
                card.setImage(cachedCardImage);
            }

            card.setLayoutX(i * offset);
            card.setLayoutY(0);
            cardPane.getChildren().add(card);
        }
    }


    private static void loadCardImageIfNeeded() {
        if (cachedCardImage != null) return;
        try {
            var stream = EnemyDisplayController.class.getResourceAsStream(CARD_IMAGE_PATH);
            if (stream != null) {
                cachedCardImage = new Image(stream, (int)(CARD_W * 4), (int)(CARD_H * 4), true, true);
            } else {
                System.err.println("backside.png nicht gefunden: " + CARD_IMAGE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden von backside.png: " + e.getMessage());
        }
    }
}