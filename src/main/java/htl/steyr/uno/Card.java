package htl.steyr.uno;

public class Card {
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

    public Card(int value, String colour) {

        if (colour == null) {
            throw new IllegalArgumentException("Colour cannot be null.");
        }

        colour = colour.toLowerCase();

        //check if CardValue is correct
        if (value < 0 || value > 14) {
            throw new IllegalArgumentException("Invalid card value: " + value);
        }

        //  check if colour is correct
        if (!colour.equals("yellow") &&
                !colour.equals("green") &&
                !colour.equals("blue") &&
                !colour.equals("red") &&
                !colour.equals("black")) {

            throw new IllegalArgumentException("Invalid card colour: " + colour);
        }

        // check Black Cards. Colour.equals & value == 13 or Colour.equals & value == 14 has to be true
        //in order for it to count
        if (colour.equals("black") != (value == 13 || value == 14)) {
            throw new IllegalArgumentException("Black cards must be 13 or 14, and 13/14 must be black.");
        }

        this.CardValue = value;
        this.CardColour = colour;

    }


    public int getCardValue() {
        return this.CardValue;
    }

    public String getCardColour() {
        return this.CardColour;
    }
}
