package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class CardDeckTest {
    @Test
    void refillKeepsTopDiscardCardOnDiscardPile() {
        CardDeck deck = new CardDeck();
        Card previousTop = new Card(3, "red");
        Card currentTop = new Card(5, "blue");
        deck.returnCardToDiscordPile(previousTop);
        deck.returnCardToDiscordPile(currentTop);

        deck.refill();

        assertSame(currentTop, deck.getTopDiscardCard());
    }
}
