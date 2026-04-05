package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import htl.steyr.uno.server.database.DatabaseUser;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
     * @param card             The card that the player is attempting to play.
     * @param drawPenaltyValue The current draw penalty value.
     */
    void playCard(Card card, Integer drawPenaltyValue) {
        String chosenColor = null;
        if (card.getCardColour().equals("black") && card.getChosenColour() != null && !card.getChosenColour().isBlank()) {
            chosenColor = card.getChosenColour();
        }
        CardPlayedRequest msg = new CardPlayedRequest(card, drawPenaltyValue, getGameTable().getPlayer(), chosenColor);
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
     * Sends a message to the server when the player says UNO in time.
     */
    public void sayUno() {
        getGameTable().getClient().getConn().sendMessage(new SayUnoRequest(getGameTable().getPlayer()));
    }

    /**
     * Called when the player forgets to say UNO within the 3-second timeout.
     *
     * NOTE: Nothing is sent to the server here. The server already runs its own
     * 3-second timeout (checkUnoTimeout) and applies the 2-card penalty automatically.
     * Sending any request here would cause a double-penalty or corrupt the server's
     * UNO state — which was the root cause of the original bug where the player
     * received 2 penalty cards even after correctly saying UNO on a later turn.
     */
    public void forgotToSayUno() {
        // Intentionally empty — the server handles the penalty via its own timeout.
    }

    /**
     * Handles the logic when the player receives a response to their request to draw a card from the central stack.
     * This method should update the player's hand and the UI to reflect the new cards, or show a message if the stack is empty.
     *
     * @param msg The StackInfoResponse message containing the stack status
     */
    public void withDrawStackInfoResponse(StackInfoResponse msg) {
        if (msg.statusCode() == 0) {
            getGameTable().restoreDrawStackImage();
        } else if (msg.statusCode() == 1) {
            getGameTable().showEmptyDrawStack();
        }
    }


    /**
     * Handles the logic when an Enemy's information is updated (e.g., after playing a card or drawing a card).
     * Especially when the Enemy is updated to passive.
     * This method should update the enemy's information in the game state and UI to reflect the changes.
     *
     * @param msg The UpdateEnemyResponse containing the updated enemy data.
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
                            ctrl.setPassive(updatedEnemy.isPassive());
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
                Card initialCard = msg.card();
                if (initialCard.getCardColour().equals("black") && msg.currentColor() != null && !msg.currentColor().isBlank()) {
                    initialCard.setChosenColour(msg.currentColor());
                }
                Platform.runLater(() -> {
                    getGameTable().getCardStack().addToStack(initialCard);
                    getGameTable().refreshEnemyTurnHighlight(msg.nextPlayerIndex());
                });
            } else {
                Platform.runLater(() -> getGameTable().refreshEnemyTurnHighlight(msg.nextPlayerIndex()));
            }

            if (getGameTable().getPlayer() != null) {
                boolean myTurn = getGameTable().getPlayer().getPlayerIndex().equals(msg.nextPlayerIndex());
                getGameTable().getPlayer().setCurrentTurn(myTurn);
                getGameTable().setCurrentTurneLabel(myTurn);
            }
        } else {
            Card playedCard = msg.card();

            if (playedCard.getCardColour().equals("black") && msg.currentColor() != null && !msg.currentColor().isBlank()) {
                if (playedCard.getChosenColour() == null || playedCard.getChosenColour().isBlank()) {
                    playedCard.setChosenColour(msg.currentColor());
                }
            }

            Platform.runLater(() -> {
                getGameTable().getCardStack().addToStack(playedCard);
                getGameTable().refreshEnemyTurnHighlight(msg.nextPlayerIndex());
            });

            if (getGameTable().getPlayer().getPlayerIndex().equals(msg.enemyIndex())) {
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
                getGameTable().getPlayer().setCurrentTurn(true);
                getGameTable().setCurrentTurneLabel(true);
            } else {
                getGameTable().setCurrentTurneLabel(false);
                for (Enemy e : getGameTable().getPlayer().getEnemies()) {
                    if (e.getPlayerIndex().equals(msg.nextPlayerIndex())) {
                        e.setCurrentTurn(true);
                        break;
                    }
                }
            }
        }
    }


    public void gameOverResponse(GameOverResponse msg) {
        // Update personal win/loss state
        for (int i = 0; i < msg.getPlayers().size(); i++) {
            Player player = msg.getPlayers().get(i);
            if (player.getUsername().equals(getGameTable().getPlayer().getUsername())) {
                boolean won = (i == 0);
                getGameTable().getClient().getConn().getUser().setGamesWon(
                        getGameTable().getClient().getConn().getUser().getGamesWon() + (won ? 1 : 0));
                getGameTable().getClient().getConn().getUser().setGamesLost(
                        getGameTable().getClient().getConn().getUser().getGamesLost() + (won ? 0 : 1));
            }
        }

        Platform.runLater(() -> {
            if (getGameTable() != null) {
                getGameTable().showGameOverOverlay(msg);
            }
        });
    }

    /**
     * Handles the logic when a UNO notification is received from the server.
     * This method processes the notification about whether a player said UNO or forgot to say UNO.
     *
     * @param msg The UnoNotificationResponse message containing the username and whether they said UNO
     */
    public void unoNotificationResponse(UnoNotificationResponse msg) {
        if (msg == null || msg.username() == null) {
            return;
        }

        Platform.runLater(() -> {
            GameTable gameTable = getGameTable();
            if (gameTable == null) {
                return;
            }

            // Cancel the UNO countdown if it's still active
            gameTable.cancelUnoCountdown();

            String notificationText;
            if (msg.didSayUno()) {
                notificationText = msg.username() + ": UNO!";
            } else {
                notificationText = msg.username() + " forgot to say UNO! +2 cards";
            }

            gameTable.getYourTurnLabel().setText(notificationText);

            // After 2 seconds, restore the turn label to its correct state
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        gameTable.setCurrentTurneLabel(gameTable.getPlayer().isCurrentTurn());
                    })
            );
            timeline.play();
        });
    }

    GameTable getGameTable() {
        return gameTable;
    }

    void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }
}