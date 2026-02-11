package htl.steyr.uno;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Player {
    private String username;
    private boolean isCurrentTurn;
    ArrayList<Card> hand = new ArrayList();




    public ArrayList<Card> getPlayerHand(){
        return this.hand;
    }

    public String getUsername(){
        return this.username;
    }

    public void setPlayerHand(ArrayList<Card> arr){
            this.hand.clear();
            this.hand.addAll(arr);

    }


}
