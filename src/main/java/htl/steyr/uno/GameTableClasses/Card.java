package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;

import java.io.Serializable;

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
    //the colour black stands for choose-colour cards
    private final String CardColour;

    private String chosenColour;

    public Card(int value, String colour) {

        if (colour == null) {
           //throw new InvalidCardException("Colour cannot be null");
        }

        colour = colour.toLowerCase();

        //check if CardValue is correct
        if (value < 0 || value > 14) {
            //throw new InvalidCardException("Value must be between 0 and 14");
        }

        //  check if colour is correct
        if (!colour.equals("yellow") &&
                !colour.equals("green") &&
                !colour.equals("blue") &&
                !colour.equals("red") &&
                !colour.equals("black")) {

           //throw new InvalidCardException("Invalid colour");
        }

        // check Black Cards. Colour.equals & value == 13 or Colour.equals & value == 14 has to be true
        //in order for it to count

        if (colour.equals("black") != (value == 13 || value == 14)) {
           //throw new InvalidCardException("black cards must have value 13 or 14");
        }

        this.CardValue = value;
        this.CardColour = colour;

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
        
        // Vergleiche CardValue und CardColour
        if (this.CardValue != card.CardValue) return false;
        if (!this.CardColour.equals(card.CardColour)) return false;
        
        // Vergleiche chosenColour (kann null sein)
        if (this.chosenColour == null) {
            return card.chosenColour == null;
        }
        return this.chosenColour.equals(card.chosenColour);
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


}
