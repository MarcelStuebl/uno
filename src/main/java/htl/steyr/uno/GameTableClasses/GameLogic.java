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
        for(int i = 0; i < 4; i++){
            player.addCardToHand(WithdrawalStack.drawCard()); //static method
        }
    }


}



