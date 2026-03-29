package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import javafx.application.Platform;

public class GameLogic {

    private GameTable gameTable;

    /**
     * The GameLogic class is responsible for handling the core game mechanics and rules.
     * It processes incoming messages related to card additions and removals, and updates the game state accordingly.
     * This class interacts with the GameTable to reflect changes in the game state visually and logically.
     */
    GameLogic(GameTable gameTable) {
        setGameTable(gameTable);
    }


    void sendReadyToStart() {
        getGameTable().getClient().getConn().sendMessage(new ReadyInGameTableRequest(getGameTable().getPlayer()));
    }

    /**
     * Handles the logic for when a player receives their initial hand of cards.
     * This method should update the player's hand and the UI to reflect the new cards.
     *
     * @param msg The PlayerGetResponse message containing information about the player's hand.
     */
    public void playerGetResponse(PlayerGetResponse msg) {
        getGameTable().setPlayer(msg.player());
        Platform.runLater(() -> {
            getGameTable().setEnemies();
            getGameTable().open();
        });


        /*
        @TODO: Update the UI to reflect the player's hand of cards.
            After that, the Game is ready to start and the player can start playing cards from their hand if it's his turn.
            !!!!!NOT BEFORE!!!!!
         */
    }


    /**
     * Handles the logic for when a card is added to the players hand.
     * This method should update the game state and UI to reflect the new card.
     *
     * @param msg The CardAddResponse message containing information about the card that was added.
     */
    public void cardAddResponse(CardAddResponse msg) {
        // @TODO: Implement logic for when a card is added to the players hand (e.g., after drawing a card from the central stack).
    }


    /**
     * Handles the logic for when a player plays a card.
     * This method should send a CardPlayedRequest to the server with the details of the card being played.
     *
     * @param card The card that the player is attempting to play.
     */
    void playCard(Card card) {
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
        Enemy enemy = msg.enemy();

        if (!enemy.getUsername().equals(getGameTable().getPlayer().getUsername())) {
            for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                if (e.getUsername().equals(enemy.getUsername())) {
                    getGameTable().getPlayer().getEnemyByUsername(enemy.getUsername()).setEnemy(enemy);
                    break;
                }
            }
        } else {
            getGameTable().getPlayer().setPassive(enemy.isPassive());
        }

        Card card = msg.card();
        Integer nextPlayerIndex = msg.nextPlayerIndex();
        // @TODO: Update the UI to reflect the card that was played and any changes to the game state (e.g., next player's turn)
    }


    /**
     * Handles the logic for when a player requests to draw a card from the central stack.
     * This method should send a RequestCardRequest to the server with the number of cards the player wants to draw.
     *
     * @param amount The number of cards the player is requesting to draw.
     */
    void requestCard(int amount) {
        getGameTable().getClient().getConn().sendMessage(new RequestCardRequest(gameTable.getPlayer(), amount));
    }


    /**
     * Handels the logic for when the player receives a response to their request to draw a card from the central stack.
     * This method should update the player's hand and the UI to reflect the new cards, or show a message if the stack is empty.
     *
     * @param msg
     */
    public void stackInfoResponse(StackInfoResponse msg) {
        if (msg.statusCode() == 0) {
            // @TODO: Stack is not empty, the player can draw cards from the stack
        } else if (msg.statusCode() == 1) {
            // @TODO: Stack is empty, stop the player from drawing cards and show a message that the stack is empty
        }
    }


    /**
     * Handels the logic, when an Enemy's information is updated (e.g., after playing a card or drawing a card).
     * Especially when the Enemy is updated to passive.
     * This method should update the enemy's information in the game state and UI to reflect the changes.
     *
     * @param msg
     */
    public void updateEnemyResponse(UpdateEnemyResponse msg) {
        for (Enemy e : getGameTable().getPlayer().getEnemies()) {
            if (e.getUsername().equals(msg.enemy().getUsername())) {
                getGameTable().getPlayer().getEnemyByUsername(msg.enemy().getUsername()).setEnemy(msg.enemy());
                break;
            }
        }
    }



    public void gameTurnResponse(GameTurnResponse msg) {
        if (msg.enemyIndex() == null) {
            Card initialCard = msg.card();
            // @TODO: Show initial Card on the table.
        } else {
            // @TODO: Check this logic
            if (getGameTable().getPlayer().getPlayerIndex().equals(msg.enemyIndex())) {
                getGameTable().getPlayer().getHand().removeIf(card -> card.equals(msg.card()));
                getGameTable().getPlayer().setCurrentTurn(false);
            } else {
                for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                    if (e.getPlayerIndex().equals(msg.enemyIndex())) {
                        e.setHandSize(e.getHandSize() - 1);
                        e.setCurrentTurn(false);
                        break;
                    }
                }
            }

            if (getGameTable().getPlayer().getPlayerIndex().equals(msg.nextPlayerIndex())) {
                gameTable.getPlayer().setCurrentTurn(true);
            } else {
                for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                    if (e.getPlayerIndex().equals(msg.nextPlayerIndex())) {
                        e.setCurrentTurn(true);
                        break;
                    }
                }
            }
        }
    }








    GameTable getGameTable() {
        return gameTable;
    }
    void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }

}
