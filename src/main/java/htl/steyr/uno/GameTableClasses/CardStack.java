package htl.steyr.uno.GameTableClasses;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Zentraler Karten-Stack für das UNO-Spiel.
 * Speichert die oberste Karte und zeigt sie visuell an.
 * Nur gültige Karten werden auf den Stack gelegt.
 * Ungültige Karten wackeln auf dem Button der Hand.
 */
public class CardStack {

    private Card topCard;
    private final StackPane visual;

    public CardStack() {
        this.visual = new StackPane();
        this.visual.setPrefSize(120, 180); // Standardgröße für Karten
    }

    public StackPane getVisual() {
        return visual;
    }

    /**
     * Legt eine Karte auf den Stack.
     * Speichert die Karte intern und zeigt sie im StackPane an.
     * Wird nur bei gültigen Zügen aufgerufen.
     */
    public void addToStack(Card c) {
        if (c == null) return;

        // Karte speichern
        this.topCard = c;

        // visuelles Update
        visual.getChildren().clear();

        String path = "../Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
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

    public Card getTopCard() {
        return topCard;
    }


    public void layCard(Card c, Button handButton) {
        if (c == null) return;

        Card top = getTopCard();

        // always lay black card
        if (c.getCardColour().equals("black")) {
            addToStack(c);
            System.out.println("used black card");
            return;
        }

        // if no card, just lay card
        if (top == null) {
            addToStack(c);
            return;
        }

        // Prüfen: Farbe oder Wert matchen
        boolean colorMatch = c.getCardColour().equals(top.getCardColour());
        boolean valueMatch = c.getCardValue() == top.getCardValue();

        if (colorMatch || valueMatch) {
            // card valid, lay card
            addToStack(c);

            // special card testPrint
            switch (c.getCardValue()) {
                case 10 -> System.out.println("Skip");
                case 11 -> System.out.println("Reverse");
                case 12 -> System.out.println("+2");
                case 13 -> System.out.println("Farbwahl");
                case 14 -> System.out.println("+4");
            }

        } else {
            shakeHandButton(handButton);
        }
    }

    /**
     * Macht den Button in der Hand wackeln bei ungültigem Zug
     * @param cardBtn Button der Karte
     */
    private void shakeHandButton(Button cardBtn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), cardBtn);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.1); st.setToY(1.1);
        st.setAutoReverse(true);
        st.setCycleCount(4); // 2x hin und her wackeln
        st.play();
    }

}