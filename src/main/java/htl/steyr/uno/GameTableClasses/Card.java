package htl.steyr.uno.GameTableClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Card implements Serializable {
    //CardValue 0 bis 9 stands for the Number on the Card
    /**
     * Special Cards
     * 10. Skip Player
     * 11. Change Direction
     * 12. Draw 2 Cards
     * 13. choose Colour
     * 14. Draw 4 and Choose Colour
     */
    private final int CardValue;
    private final String CardColour; //Colors: yellow, green, blue, red
    @Setter private String chosenColour;
    private final String cardId;

    public Card(int value, String colour) {
        this(value, colour, null, UUID.randomUUID().toString());
    }

    @JsonCreator
    public Card(@JsonProperty("cardValue") int value,
                @JsonProperty("cardColour") String colour,
                @JsonProperty("chosenColour") String chosenColour,
                @JsonProperty("cardId") String cardId) {
        colour = colour.toLowerCase();

        this.CardValue = value;
        this.CardColour = colour;
        this.chosenColour = chosenColour;
        this.cardId = cardId == null ? UUID.randomUUID().toString() : cardId;
    }

    @Override
    public String toString() {
        return getCardColour() + getCardValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Card card = (Card) obj;
        return this.cardId.equals(card.cardId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cardId);
    }

}