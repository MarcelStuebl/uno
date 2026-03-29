package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardDeck {

    private final Random random = new Random();
    private final int MAX_CARDS = 108;
    private final ArrayList<Card> stack = new ArrayList<>();
    private final ArrayList<Card> discardPile = new ArrayList<>();



    public CardDeck() {
        generateDeck();
    }

    private void generateDeck() {
        stack.clear();
        while (stack.size() < MAX_CARDS) {
            stack.add(generateCard());
        }
    }

    public void shuffle() {
        Collections.shuffle(stack);
    }

    public Card getCardFromStack() {
        if (stack.isEmpty()) {
            refill();
        }
        if (stack.isEmpty()) {
            return null;
        }
        return stack.removeLast();
    }

    public void returnCardToDiscordPile(Card card) {
        discardPile.add(card);
        if (stack.isEmpty()) {
            refill();
        }
    }






    private Card generateCard() {
        int value = random.nextInt(15);
        String colour;

        if (value == 13 || value == 14) {
            colour = "black";
        } else {
            String[] colors = {"yellow", "green", "blue", "red"};
            colour = colors[random.nextInt(colors.length)];
        }

        try {
            return new Card(value, colour);
        } catch (InvalidCardException e) {
            throw new RuntimeException("Error generating valid card", e);
        }
    }

    private void refill() {
        stack.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}