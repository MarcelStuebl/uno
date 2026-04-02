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

    }


    /**
     * Handles the logic for when a card is added to the players hand.
     * This method should update the game state and UI to reflect the new card.
     *
     * @param msg The CardAddResponse message containing information about the card that was added.
     */
    public void cardAddResponse(CardAddResponse msg) {
        Platform.runLater(() -> {
            getGameTable().getPlayer().addCardToHand(msg.card());
            getGameTable().updatePlayerHandUI();
        });
    }


    /**
     * Handles the logic for when a player plays a card.
     * This method should send a CardPlayedRequest to the server with the details of the card being played.
     *
     * @param card The card that the player is attempting to play.
     */
    void playCard(Card card, Integer drawPenaltyValue) {
        CardPlayedRequest msg = new CardPlayedRequest(card, drawPenaltyValue, getGameTable().getPlayer());
        getGameTable().getClient().getConn().sendMessage(msg);
    }


    /**
     * Handles the logic for when a player requests to draw a card from the central stack.
     * This method should send a RequestCardRequest to the server with the number of cards the player wants to draw.
     *
     * @param amount The number of cards the player is requesting to draw.
     */
    void requestCard(int amount) {
        if (!getGameTable().getPlayer().isCurrentTurn()) {
            return;
        }
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
     * Handles the logic, when an Enemy's information is updated (e.g., after playing a card or drawing a card).
     * Especially when the Enemy is updated to passive.
     * This method should update the enemy's information in the game state and UI to reflect the changes.
     *
     * @param msg
     */
    public void updateEnemyResponse(UpdateEnemyResponse msg) {
        if (getGameTable() == null || getGameTable().getPlayer() == null) {
            return;
        }
        
        for (Enemy e : getGameTable().getPlayer().getEnemies()) {
            if (e.getUsername().equals(msg.enemy().getUsername())) {
                Enemy updatedEnemy = msg.enemy();
                e.setEnemy(updatedEnemy);

                Platform.runLater(() -> {
                    for (EnemyDisplayController ctrl : getGameTable().getEnemyControllers()) {
                        if (ctrl != null && updatedEnemy.getUsername() != null && ctrl.getUsername() != null &&
                            updatedEnemy.getUsername().equals(ctrl.getUsername())) {
                            ctrl.setCardCount(updatedEnemy.getHandSize());
                            break;
                        }
                    }
                });

                break;
            }
        }
    }



    public void gameTurnResponse(GameTurnResponse msg) {
        getGameTable().setGameTurnResponse(msg);


        if (msg.enemyIndex() == null) {
            if (msg.card() != null) {
                // Initiale Karte am Spielstart
                Card initialCard = msg.card();
                if (initialCard.getCardColour().equals("black") && msg.currentColor() != null && !msg.currentColor().isBlank()) {
                    initialCard.setChosenColour(msg.currentColor());
                }
                Platform.runLater(() -> {
                    getGameTable().getCardStack().addToStack(initialCard);
                });
            }
            
            getGameTable().getPlayer().setCurrentTurn(getGameTable().getPlayer().getPlayerIndex().equals(msg.nextPlayerIndex()));
        } else {
            Card currentTopCard = getGameTable().getCardStack().getTopCard();
            Card playedCard = msg.card();

            if (playedCard.getCardColour().equals("black") && msg.currentColor() != null && !msg.currentColor().isBlank()) {
                playedCard.setChosenColour(msg.currentColor());
            }

            if (!isSameCard(currentTopCard, playedCard)) {
                Platform.runLater(() -> {
                    getGameTable().getCardStack().addToStack(playedCard);
                });
            } else {
                if (playedCard.getCardColour().equals("black") && playedCard.getChosenColour() != null && !playedCard.getChosenColour().isBlank()) {
                    if (currentTopCard.getChosenColour() == null || currentTopCard.getChosenColour().isBlank()) {
                        currentTopCard.setChosenColour(playedCard.getChosenColour());
                        Platform.runLater(() -> {
                            getGameTable().getCardStack().addToStack(currentTopCard);
                        });
                    }
                }
            }
            
            if (getGameTable().getPlayer().getPlayerIndex().equals(msg.enemyIndex())) {
                getGameTable().getPlayer().getHand().removeIf(card -> card.equals(msg.card()));
                getGameTable().getPlayer().setCurrentTurn(false);
            } else {
                for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                    if (e.getPlayerIndex().equals(msg.enemyIndex())) {
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

    /**
     * Vergleicht zwei Karten nach Farbe und Wert.
     * Verwendet dies, um zu unterscheiden, ob eine neue Karte gelegt wurde oder nur abgehoben wurde.
     * Für schwarze Karten: ignoriert die chosenColour beim Vergleich, da sie später vom Server gesetzt wird.
     *
     * @param c1 Erste Karte
     * @param c2 Zweite Karte
     * @return true wenn beide Karten gleich sind (Farbe + Wert), false sonst
     */
    private boolean isSameCard(Card c1, Card c2) {
        if (c1 == null || c2 == null) return false;
        return c1.getCardColour().equals(c2.getCardColour()) && c1.getCardValue() == c2.getCardValue();
    }




    GameTable getGameTable() {
        return gameTable;
    }
    void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }



}