package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.requests.client.CardPlayedRequest;
import htl.steyr.uno.requests.server.CardAddResponse;
import htl.steyr.uno.requests.server.CardPlayedResponse;
import htl.steyr.uno.requests.server.PlayerGetResponse;

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
     * Handles the logic for when a player receives their initial hand of cards.
     * This method should update the player's hand and the UI to reflect the new cards.
     *
     * @param msg The PlayerGetResponse message containing information about the player's hand.
     */
    public void playerGetResponse(PlayerGetResponse msg) {
        gameTable.setPlayer(msg.getPlayer());
        // @TODO: Update the UI to reflect the player's hand of cards
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
     * Handles the logic for when a player plays a card.
     * This method should send a CardPlayedRequest to the server with the details of the card being played.
     *
     * @param card The card that the player is attempting to play.
     */
    public void playCard(Card card) {
        CardPlayedRequest msg = new CardPlayedRequest(card, getGameTable().getPlayer());
        getGameTable().getClient().getConn().sendMessage(msg);
    }


    /**
     * Handles the logic for when a card is played by any player (including the current player).
     * This method should update the game state and UI to reflect the card that was played and any changes to the game state (e.g., next player's turn).
     *
     * @param msg The CardPlayedResponse message containing information about the card that was played and the player who played it.
     */
    public void cardPlayedResponse(CardPlayedResponse msg) {
        Card card = msg.getCard();
        Enemy enemy = msg.getEnemy();

        if (!enemy.getUsername().equals(getGameTable().getPlayer().getUsername())) {
            for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                if (e.getUsername().equals(enemy.getUsername())) {
                    e.decrementCardCount(1);
                    break;
                }
            }
        }

        Integer nextPlayerIndex = msg.getNextPlayerIndex();
        // @TODO: Update the UI to reflect the card that was played and any changes to the game state (e.g., next player's turn)

    }









    public GameTable getGameTable() {
        return gameTable;
    }
    public void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }

}
