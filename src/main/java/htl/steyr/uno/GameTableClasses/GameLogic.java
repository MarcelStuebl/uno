package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.requests.server.CardAddResponse;
import htl.steyr.uno.requests.server.CardRemoveResponse;

public class GameLogic {

    private GameTable gameTable;

    /**
     * The GameLogic class is responsible for handling the core game mechanics and rules.
     * It processes incoming messages related to card additions and removals, and updates the game state accordingly.
     * This class interacts with the GameTable to reflect changes in the game state visually and logically.
     */
    public GameLogic(GameTable gameTable) {
        setGameTable(gameTable);
    }


    /**
     * Handles the logic for when a card is added to the central stack.
     * This method should update the game state and UI to reflect the new card on the stack.
     *
     * @param msg The CardAddResponse message containing information about the card that was added.
     */
    public void cardAddResponse(CardAddResponse msg) {
        // @TODO: Implement logic for when a card is added to the central stack
    }


    /**
     * Handles the logic for when a card is removed from the central stack.
     * This method should update the game state and UI to reflect the removed card from the stack.
     *
     * @param msg The CardRemoveResponse message containing information about the card that was removed.
     */
    public void cardRemoveResponse(CardRemoveResponse msg) {
        // @TODO: Implement logic for when a card is removed from the central stack
    }








    public GameTable getGameTable() {
        return gameTable;
    }
    public void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }

}
