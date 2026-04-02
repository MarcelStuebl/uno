package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.*;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {

    private Lobby lobby;
    private ArrayList<Player> players = new ArrayList<>();
    private Integer currentPlayerIndex = 0;
    private boolean directionClockwise = true;
    private final CardDeck cardDeck = new CardDeck();
    private Integer lastStackInfo = 0;
    private Card currentCard;
    private Integer drawPenaltyValue = 0;
    private String currentColor = null;  // Aktuelle Spielfarbe (von schwarzen Karten)


    GameLogic(Lobby lobby) {
        setLobby(lobby);
    }
    
    
    /**
     * Game Setup Methods for initializing the game state and preparing the game for play.
     * <p>
     * This includes methods for creating the game, starting the game, and generating the initial hands for each player.
     * The GameLogic class is responsible for setting up the game state based on the players in the lobby and ensuring that each player receives their initial hand of cards.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */
    void createGame(List<User> users) {
        players.clear();
        currentPlayerIndex = 0;
        directionClockwise = true;
        drawPenaltyValue = 0;

        while (getCurrentCard() == null || getCurrentCard().getCardValue() >= 10) {
            setCurrentCard(getCardDeck().getCardFromStack());
        }
        getCardDeck().refill();

        int playerIndex = 0;
        for (User user : users) {
            if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
                continue;
            }

            ArrayList<Card> hand = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(getCardDeck().getCardFromStack());
            }

            players.add(new Player(user.getUsername(), false, hand, new ArrayList<>(), playerIndex, user.getProfileImageData()));
            playerIndex++;
        }
    }


    /**
     * Handles the logic for when a player indicates that they are ready in the game lobby.
     * This method should update the player's ready status and check if all players are ready to start the game.
     *
     * @param msg The ReadyInGameTableRequest message containing information about the player who is ready.
     */
    void readyInGameTable(ReadyInGameTableRequest msg) {
        for (Player player : players) {
            if (player.getUsername().equals(msg.player().getUsername())) {
                player.setReady(true);
                checkReadyInGameTable();
                break;
            }
        }
    }


    /**
     * Checks if all players in the game lobby are ready to start the game.
     * If all players are ready, this method will call the startGame() method to begin the game.
     * If any player is not ready, this method will simply return without starting the game.
     */
    private void checkReadyInGameTable() {
        for (Player player : players) {
            if (!player.isReady()) {
                return;
            }
        }
        startGame();
    }


    /**
     * Starts the game by sending the initial game state to all players in the lobby.
     * This method should send a PlayerGetResponse to each player with their initial hand of cards and any relevant game information.
     */
    private void startGame(){
        if (lobby == null || lobby.getConnections() == null || players.isEmpty()) {
            return;
        }
        
        for (Player player : players) {
            if (player == null || player.getUsername() == null) {
                continue;
            }
            
            for (ServerSocketConnection c : lobby.getConnections()) {
                if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                    continue;
                }
                if (c.getUser().getUsername().equals(player.getUsername())) {
                    player.getEnemies().clear();
                    
                    for (Player enemy : players) {
                        if (!enemy.getUsername().equals(c.getUser().getUsername())) {
                            Enemy newEnemy = new Enemy(enemy);
                            player.getEnemies().add(newEnemy);
                        }
                    }
                    PlayerGetResponse msg = new  PlayerGetResponse(player);
                    c.sendMessage(msg);

                    GameTurnResponse gtr = new GameTurnResponse(null, getCurrentCard(), 0, getCurrentPlayer(), isDirectionClockwise(), currentColor);
                    c.sendMessage(gtr);
                    break;
                }
            }
        }
    }





    /**
     * Logic Methods for handling game mechanics.
     * <p>
     * This includes methods for adding cards to players, handling card plays, and updating the game state accordingly.
     * The GameLogic class is responsible for ensuring that the game rules are followed and that the game state is updated correctly based on player actions.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */




    void cardPlayed(CardPlayedRequest msg) {
        Card card = msg.card();
        Player msgPlayer = msg.player();
        Integer receivedDrawPenaltyValue = msg.drawPenaltyValue();

        Player player = null;
        for (Player p : players) {
            if (p.getUsername().equals(msgPlayer.getUsername())) {
                player = p;
                break;
            }
        }
        
        if (player == null) {
            return;
        }

        player.removeCardFromHand(card);
        getCardDeck().returnCardToDiscordPile(card);

        boolean isSkip = card.getCardValue() == 10;
        boolean isReverse = card.getCardValue() == 11;

        if (isSkip) {
            // Skip Player: Übernächsten Spieler überspringen (2x wechseln)
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        } else if (isReverse) {
            directionClockwise = !directionClockwise;
        } else if (card.getCardValue() == 12) {
            drawPenaltyValue = (receivedDrawPenaltyValue != null && receivedDrawPenaltyValue > 0) ? receivedDrawPenaltyValue : 2;
        } else if (card.getCardValue() == 13) {
            drawPenaltyValue = 0;
        } else if (card.getCardValue() == 14) {
            drawPenaltyValue = (receivedDrawPenaltyValue != null && receivedDrawPenaltyValue > 0) ? receivedDrawPenaltyValue : 4;
        }

        if (card.getCardColour().equals("black") && card.getChosenColour() != null && !card.getChosenColour().isBlank()) {
            currentColor = card.getChosenColour();
        } else if (!card.getCardColour().equals("black")) {
            currentColor = null;
        }

        if (!isReverse) {
            if (!isSkip) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }

            // überspringe passive Spieler
            while (players.get(currentPlayerIndex).isPassive()) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }
        } else {
            int activePlayerCount = 0;
            for (Player p : players) {
                if (!p.isPassive()) {
                    activePlayerCount++;
                }
            }
            
            if (activePlayerCount == 2) {
                if (players.get(currentPlayerIndex).isPassive()) {
                    currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                    while (players.get(currentPlayerIndex).isPassive()) {
                        currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                    }
                }
            } else {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                
                // überspringe passive Spieler
                while (players.get(currentPlayerIndex).isPassive()) {
                    currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                }
            }
        }

        checkForWinner(player);

        Card cardForResponse = card;
        if (card.getCardColour().equals("black")) {
            cardForResponse = new Card(card.getCardValue(), card.getCardColour());
            String colorToSet = (card.getChosenColour() != null && !card.getChosenColour().isBlank()) 
                ? card.getChosenColour() 
                : currentColor;
            if (colorToSet != null && !colorToSet.isBlank()) {
                cardForResponse.setChosenColour(colorToSet);
            }
        }

        GameTurnResponse response = new GameTurnResponse(player.getPlayerIndex(), cardForResponse, drawPenaltyValue, currentPlayerIndex, directionClockwise, currentColor);
        for (ServerSocketConnection c : lobby.getConnections()) {
            c.sendMessage(response);
        }

        for (Player p : players) {
            for (Player lobbyPlayer : players) {
                for (Enemy e : lobbyPlayer.getEnemies()) {
                    if (e.getUsername().equals(p.getUsername())) {
                        e.setEnemy(new Enemy(p));
                        break;
                    }
                }
            }
            
            Enemy updatedEnemy = new Enemy(p);
            for (ServerSocketConnection c : lobby.getConnections()) {
                c.sendMessage(new UpdateEnemyResponse(updatedEnemy));
            }
        }

        updateEnemy(new Enemy(player));
    }



    private void updateEnemy(Enemy enemy) {
        if (enemy == null || enemy.getUsername() == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c != null && c.getUser() != null && c.getUser().getUsername() != null) {
                c.sendMessage(new UpdateEnemyResponse(enemy));
            }
        }
    }



    private void addCardsToPlayer(Player player, Card card) {
        checkForEmptyStack();

        if (player == null || player.getUsername() == null || card == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        player.addCardToHand(card);
        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                continue;
            }
            if (c.getUser().getUsername().equals(player.getUsername())) {
                CardAddResponse msg = new CardAddResponse(card);
                c.sendMessage(msg);
                break;
            }
        }

        updateEnemy(new Enemy(player));
    }



    void requestCard(RequestCardRequest msg) {
        Player requestingPlayer = msg.player();

        Player player = null;
        for (Player p : players) {
            if (p.getUsername().equals(requestingPlayer.getUsername())) {
                player = p;
                break;
            }
        }
        
        if (player == null) {
            return;
        }
        
        int amount = msg.amount();

        for (int i = 0; i < amount; i++) {
            Card card = getCardDeck().getCardFromStack();
            addCardsToPlayer(player, card);
        }
        drawPenaltyValue = 0;

        // Wechsle zum nächsten Spieler nach dem Abheben
        currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        
        // überspringe passive Spieler
        while (players.get(currentPlayerIndex).isPassive()) {
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        }

        // Sende die aktuelle oberste Karte
        Card cardToSend = getCardDeck().getTopDiscardCard();
        if (cardToSend == null) {
            cardToSend = getCurrentCard();
        }

        // Nur wenn die oberste Karte NICHT schwarz ist, currentColor zurücksetzen
        if (cardToSend != null && !cardToSend.getCardColour().equals("black")) {
            currentColor = null;
        }

        // Sende null statt einer Karte, um zu signalisieren, dass es kein neuer Spielzug ist
        GameTurnResponse response = new GameTurnResponse(null, null, 0, currentPlayerIndex, directionClockwise, currentColor);
        for (ServerSocketConnection c : lobby.getConnections()) {
            c.sendMessage(response);
        }


        for (Player p : players) {
            for (Player lobbyPlayer : players) {
                for (Enemy e : lobbyPlayer.getEnemies()) {
                    if (e.getUsername().equals(p.getUsername())) {
                        e.setEnemy(new Enemy(p));
                        break;
                    }
                }
            }
            
            Enemy updatedEnemy = new Enemy(p);
            for (ServerSocketConnection c : lobby.getConnections()) {
                c.sendMessage(new UpdateEnemyResponse(updatedEnemy));
            }
        }

        updateEnemy(new Enemy(player));
    }


    private void checkForWinner(Player player) {
        if (player.getHand().isEmpty()) {
            player.setPassive(true);
            updateEnemy(new Enemy(player));
        }
    }


    private void checkForEmptyStack() {
        if (getCardDeck().isStackEmpty() && lastStackInfo == 0) {
            getLobby().sendInfoToAll(new StackInfoResponse(1));
            lastStackInfo = 1;
        } else if (!getCardDeck().isStackEmpty() && lastStackInfo == 1) {
            getLobby().sendInfoToAll(new StackInfoResponse(0));
            lastStackInfo = 0;
        }
    }




    /**
     * End of Logic Methods.
     */



    ArrayList<Enemy> getPlayersAsEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        for (Player player : players) {
            enemies.add(new Enemy(player));
        }
        return enemies;
    }

    Lobby getLobby() {
        return lobby;
    }
    void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    ArrayList<Player> getPlayers() {
        return players;
    }
    Player getPlayer(int i) {
        return getPlayers().get(i);
    }
    void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    Integer getCurrentPlayer() {
        return currentPlayerIndex;
    }
    void setCurrentPlayer(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    boolean isDirectionClockwise() {
        return directionClockwise;
    }
    void setDirectionClockwise(boolean directionClockwise) {
        this.directionClockwise = directionClockwise;
    }

    private CardDeck getCardDeck() {
        return cardDeck;
    }

    private Card getCurrentCard() {
        return currentCard;
    }
    private void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }

    Integer getDrawPenaltyValue() {
        return drawPenaltyValue;
    }
    void setDrawPenaltyValue(Integer drawPenaltyValue) {
        this.drawPenaltyValue = drawPenaltyValue;
    }
}
