package htl.steyr.uno.GameTableClasses;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Stack;

public class CardStack {

    private final Stack<Card> pile = new Stack<>();
    private final StackPane visual = new StackPane();

    public CardStack() {
        visual.setPrefSize(120, 180);
    }

    public StackPane getVisual() {
        return visual;
    }

    public Card getTopCard() {
        if (pile.isEmpty()) return null;
        return pile.peek();
    }

    public void addCard(Card card) {

        pile.push(card);

        // Visual leeren (nur oberste Karte anzeigen)
        visual.getChildren().clear();

        // Bildpfad erzeugen
        String path = "../Uno_Cards/" + card.getCardColour() + "/" + card.getCardColour() + card.getCardValue() + ".png";

        ImageView iv = new ImageView(
                new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(path)))
        );

        iv.setFitWidth(100);
        iv.setFitHeight(150);
        iv.setPreserveRatio(true);

        visual.getChildren().add(iv);

        // kleine Pop-Animation
        ScaleTransition st = new ScaleTransition(Duration.millis(200), visual);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }
}