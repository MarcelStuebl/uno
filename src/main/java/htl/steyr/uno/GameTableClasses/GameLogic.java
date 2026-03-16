package htl.steyr.uno.GameTableClasses;

import javafx.stage.Stage;

import java.util.ArrayList;

public class GameLogic {

    public void endGame(Player player, Stage stage) {
        int counter = 0;
        ArrayList<Enemy> enemies = player.getEnemies();
        if (player.getPlayerHand().size() == 0) {
            counter++;
        }
        for (Enemy enemy : enemies) {
            if (enemy.getCardCount() == 0) {
            }
            stage.close();
        }
    }

    public void drawFourCards(Player player){
       //logic for drawing four cards (card can only be
        chooseColour();
    }

    public void blockNextPlayer(Player player){
        for(Enemy enemy : player.getEnemies()){
            //ask server whose turn it currently is. then tell seerver to block the next player in line

        }
    }

    public void chooseColour(){
        //logic for picking what Colour the player can / cant use on their next turn
    }



}



