package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.UiStyleUtil;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
    ScaleTransition st;
    private Card topCard;
    private StackPane visual;

    public CardStack() {
        this.visual = new StackPane();
        this.visual.setPrefSize(137, 192);
        this.visual.setMaxSize(137, 192);
        this.visual.setStyle("-fx-background-color: transparent;" +
                "-fx-border-color: green;" +
                "-fx-border-width: 6;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;");
    }

    public StackPane getVisual() {
        return visual;
    }

    //player lays a card on the stack
    //stack will update, display a small animation and check if it can be layed down or not
    //logic for laying down cards will happen in the stack
    public void addToStack(Card c) {
        if (c == null) return;

        // save card
        this.topCard = c;

        // update visually
        visual.getChildren().clear();

        String path = "/htl/steyr/uno/Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
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

    public Card getTopCard() {
        return topCard;
    }


    public void layCard(Card c, Button handButton, Player p) {
        if (c == null) return;

        Card top = getTopCard();

        // if no card, just lay card
        if (top == null) {
            addToStack(c);
            return;
        }

        // always lay black card
        if (c.getCardColour().equals("black")) {
            addToStack(c);
            return;
        }


        // check if colour of value matches
        boolean colorMatch = c.getCardColour().equals(top.getCardColour());
        boolean valueMatch = c.getCardValue() == top.getCardValue();

        if ((colorMatch || valueMatch) || p.isCurrentTurn()) {
            // card valid, lay card
            addToStack(c);

            // special card testPrint
            switch (c.getCardValue()) {
                case 10 -> System.out.println("Skip");
                case 11 -> System.out.println("Reverse");
                case 12 -> System.out.println("+2");
                case 13 -> System.out.println("pick Colour");
                case 14 -> System.out.println("+4 and pick Colour");
            }

        } else {
            shakeHandButton(handButton);
        }
    }

   // makes the button that is currently clicked shake if it is layable
    private void shakeHandButton(Button cardBtn) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), cardBtn);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.playFromStart();
    }

}