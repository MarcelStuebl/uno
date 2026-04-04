package htl.steyr.uno.GameTableClasses;


import java.io.Serializable;
import java.util.UUID;

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
    //Colours: yellow, green, blue, red
    private final String CardColour;
    private String chosenColour;
    private final String cardId;

    public Card(int value, String colour) {
        colour = colour.toLowerCase();

        this.CardValue = value;
        this.CardColour = colour;
        this.cardId = UUID.randomUUID().toString();
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

        if (this.CardValue != card.CardValue) return false;
        return this.CardColour.equals(card.CardColour);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(CardValue, CardColour);
    }


    public int getCardValue() {
        return this.CardValue;
    }

    public String getCardColour() {
        return this.CardColour;
    }

    public String getChosenColour() {
        return this.chosenColour;
    }
    public void setChosenColour(String chosenColour) {
        this.chosenColour = chosenColour;
    }
    
    public String getCardId() {
        return this.cardId;
    }


}
