package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardDeck {

    private final ArrayList<Card> stack = new ArrayList<>();
    private final ArrayList<Card> discardPile = new ArrayList<>();



    public CardDeck() {
        generateDeck();
        shuffle();
    }


    /**
     * Generates a standard UNO deck with 108 cards, including number cards, action cards, and wild cards.
     */
    private void generateDeck() {
        stack.clear();

        String[] colours = {"yellow", "green", "blue", "red"};

        for (String colour : colours) {
            // 1× Karte „0" pro Farbe
            addCard(0, colour);

            // 2× Karten 1–9 pro Farbe
            for (int value = 1; value <= 9; value++) {
                addCard(value, colour);
                addCard(value, colour);
            }

            // 2× Aussetzen pro Farbe
            addCard(10, colour);
            addCard(10, colour);

            // 2× Richtungswechsel pro Farbe
            addCard(11, colour);
            addCard(11, colour);

            // 2× +2 pro Farbe
            addCard(12, colour);
            addCard(12, colour);
        }

        // 4× Farbwahl
        for (int i = 0; i < 4; i++) {
            addCard(13, "black");
        }

        // 4× +4 Farbwahl
        for (int i = 0; i < 4; i++) {
            addCard(14, "black");
        }
    }



    private void shuffle() {
        Collections.shuffle(stack);
    }

    Card getCardFromStack() {
        if (stack.isEmpty()) {
            refill();
        }
        if (stack.isEmpty()) {
            return null;
        }
        return stack.removeLast();
    }

    void returnCardToDiscordPile(Card card) {
        discardPile.add(card);
        if (stack.isEmpty()) {
            refill();
        }
    }

    void refill() {
        stack.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    private void addCard(int value, String colour) {
        stack.add(new Card(value, colour));
    }

    boolean isStackEmpty() {
        return stack.isEmpty();
    }

    Card getTopDiscardCard() {
        if (discardPile.isEmpty()) {
            return null;
        }
        return discardPile.get(discardPile.size() - 1);
    }
}