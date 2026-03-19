package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;

public class WithdrawalStack{

    public static Card drawCard() throws InvalidCardException {
        //here will be the logic for getting 1 singular card from the Server

        Card card = new Card(4,"blue"); //card that will be given be the server
        return card;
    }


}
