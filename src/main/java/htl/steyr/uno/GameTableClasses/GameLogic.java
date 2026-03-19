package htl.steyr.uno.GameTableClasses;

import javafx.stage.Stage;

import java.util.ArrayList;

public class GameLogic {

    public static void endGame(Player player, Stage stage) {
     stage.close();
     //no idea what happens after the game ends but heres gonna be the code for it



    }

    public static void drawFourCards(Player player){
       //logic for drawing four cards (card can only be +4 AND colour chooseable)




        chooseColour();
    }

    public static void blockNextPlayer(Player player){
            //ask server whose turn it currently is. then tell server to block the next player in line
        }

    public static void chooseColour(){
        //logic for picking what Colour the player can / cant use on their next turn
        //should be programmed so that it can be used for drawFourCards aswell

    }


}



