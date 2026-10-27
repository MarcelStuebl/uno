package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.*;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import htl.steyr.uno.server.database.DatabaseMatchStates;
import htl.steyr.uno.server.database.DatabaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameLogic {

    private static final Set<String> CARD_COLORS = Set.of("red", "green", "yellow", "blue");

    private Lobby lobby;
    private ArrayList<Player> players = new ArrayList<>();
    private Integer currentPlayerIndex = 0;
    private boolean directionClockwise = true;
    private final CardDeck cardDeck = new CardDeck();
    private Integer lastStackInfo = 0;
    private Card currentCard;
    private Integer drawPenaltyValue = 0;
    private String currentColor = null;
    private final ArrayList<String> finishOrder = new ArrayList<>();
    private final LinkedHashSet<String> leftPlayers = new LinkedHashSet<>();
    private boolean gameOverSent = false;

    private String lastPlayerWhoSaidUno = null;
    private long unoTimeoutTimestamp = 0;
    private volatile boolean unoHandled = false;
    private java.util.Set<String> playersSaidUno = new java.util.HashSet<>();

    // FIX: Tracks players who have 1 card but have NOT yet said UNO.
    // If such a player tries to play their last card, they get 2 penalty cards first.
    private final Set<String> pendingUnoPenalty = new HashSet<>();


    GameLogic(Lobby lobby) {
        setLobby(lobby);
    }


    synchronized void createGame(List<User> users) {
        players.clear();
        currentPlayerIndex = 0;
        directionClockwise = true;
        drawPenaltyValue = 0;
        currentColor = null;
        setCurrentCard(null);
        finishOrder.clear();
        leftPlayers.clear();
        gameOverSent = false;
        pendingUnoPenalty.clear();

        while (getCurrentCard() == null || getCurrentCard().getCardValue() >= 10) {
            setCurrentCard(getCardDeck().getCardFromStack());
        }
        getCardDeck().returnCardToDiscordPile(getCurrentCard());

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


    synchronized void readyInGameTable(ReadyInGameTableRequest msg) {
        for (Player player : players) {
            if (player.getUsername().equals(msg.player().getUsername())) {
                player.setReady(true);
                checkReadyInGameTable();
                break;
            }
        }
    }


    private void checkReadyInGameTable() {
        for (Player player : players) {
            if (!player.isReady()) {
                return;
            }
        }
        startGame();
    }


    private void startGame() {
        if (lobby == null || lobby.getConnections() == null || players.isEmpty()) {
            return;
        }

        for (Player player : players) {
            if (player == null || player.getUsername() == null) {
                continue;
            }

            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
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
                    PlayerGetResponse msg = new PlayerGetResponse(player);
                    c.sendMessage(msg);

                    GameTurnResponse gtr = new GameTurnResponse(null, getCurrentCard(), 0, getCurrentPlayer(), isDirectionClockwise(), currentColor);
                    c.sendMessage(gtr);
                    break;
                }
            }
        }
    }


    synchronized void cardPlayed(ServerSocketConnection requester, CardPlayedRequest msg) {
        if (requester == null || requester.getUser() == null || msg == null
                || msg.card() == null || msg.player() == null) {
            System.out.println("Rejected invalid card play request");
            return;
        }

        String username = requester.getUser().getUsername();
        if (username == null || !username.equals(msg.player().getUsername())) {
            System.out.println("Rejected card play request with mismatched player identity");
            return;
        }

        Card card = msg.card();
        Integer receivedDrawPenaltyValue = msg.drawPenaltyValue();

        Player player = null;
        for (Player p : players) {
            if (p.getUsername().equals(username)) {
                player = p;
                break;
            }
        }

        if (player == null || player.isPassive()
                || !java.util.Objects.equals(player.getPlayerIndex(), currentPlayerIndex)
                || !player.getHand().contains(card) || !isLegalCardPlay(card, msg.chosenColor())) {
            System.out.println("Rejected illegal card play request from " + username);
            return;
        }

        // FIX: If this player has a pending UNO penalty (had 1 card and did not say UNO),
        // apply the 2 penalty cards BEFORE allowing the last card to be played.
        // This prevents winning without saying UNO.
        if (pendingUnoPenalty.contains(player.getUsername()) && player.getHand().size() == 1) {
            pendingUnoPenalty.remove(player.getUsername());

            // Cancel the timeout thread's ability to double-penalize
            if (player.getUsername().equals(lastPlayerWhoSaidUno)) {
                lastPlayerWhoSaidUno = null;
                unoTimeoutTimestamp = 0;
            }

            // Give 2 penalty cards
            for (int i = 0; i < 2; i++) {
                Card penaltyCard = getCardDeck().getCardFromStack();
                addCardsToPlayer(player, penaltyCard);
            }

            // Notify all players that this player forgot to say UNO
            UnoNotificationResponse penaltyNotif = new UnoNotificationResponse(player.getUsername(), false);
            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
                c.sendMessage(penaltyNotif);
            }

            // Tell all clients that it is still this player's turn (turn does NOT advance).
            // enemyIndex=null signals "no card was played", nextPlayerIndex stays at the
            // current player so the client re-enables their hand.
            GameTurnResponse keepTurn = new GameTurnResponse(null, null, drawPenaltyValue, currentPlayerIndex, directionClockwise, currentColor);
            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
                c.sendMessage(keepTurn);
            }

            // Update enemy displays so hand size reflects the 2 new cards
            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
                c.sendMessage(new UpdateEnemyResponse(new Enemy(player)));
            }

            return;
        }

        setCurrentCard(card);
        player.removeCardFromHand(card);

        getCardDeck().returnCardToDiscordPile(card);
        checkForEmptyStack();

        boolean isSkip = card.getCardValue() == 10;
        boolean isReverse = card.getCardValue() == 11;

        if (isSkip) {
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            while (players.get(currentPlayerIndex).isPassive()) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            while (players.get(currentPlayerIndex).isPassive()) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }
        } else if (isReverse) {
            // Reverse card was played - change direction
            directionClockwise = !directionClockwise;
            // Note: further handling is done after the card is applied below
        } else if (card.getCardValue() == 12) {
            drawPenaltyValue = (receivedDrawPenaltyValue != null && receivedDrawPenaltyValue > 0) ? receivedDrawPenaltyValue : 2;
        } else if (card.getCardValue() == 13) {
            drawPenaltyValue = 0;
        } else if (card.getCardValue() == 14) {
            drawPenaltyValue = (receivedDrawPenaltyValue != null && receivedDrawPenaltyValue > 0) ? receivedDrawPenaltyValue : 4;
        } else {
            drawPenaltyValue = 0;
        }

        if (card.getCardColour().equals("black") && msg.chosenColor() != null && !msg.chosenColor().isBlank()) {
            currentColor = msg.chosenColor();
        } else if (!card.getCardColour().equals("black")) {
            currentColor = null;
        }

        // FIX: If the player now has exactly 1 card, mark them as needing to say UNO.
        // We do this BEFORE checkForWinner so the penalty set is consistent.
        if (player.getHand().size() == 1) {
            pendingUnoPenalty.add(player.getUsername());
            lastPlayerWhoSaidUno = player.getUsername();
            unoTimeoutTimestamp = System.currentTimeMillis();

            // After 3 seconds, if still pending, apply penalty automatically
            final String pendingUsername = player.getUsername();
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    checkUnoTimeout(pendingUsername);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        checkForWinner(player);
        if (checkAndSendGameOver()) {
            return;
        }

        if (!isReverse) {
            if (!isSkip) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }

            while (players.get(currentPlayerIndex).isPassive()) {
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
            }
        } else {
            // Reverse card logic
            int activePlayerCount = 0;
            for (Player p : players) {
                if (!p.isPassive()) {
                    activePlayerCount++;
                }
            }

            if (activePlayerCount == 2) {
                // With 2 active players, Reverse acts like a Skip
                // Skip the next player (who is the only other active player)
                // So the current player goes again
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                while (players.get(currentPlayerIndex).isPassive()) {
                    currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                }
                // Skip that player too - go to the next one (which brings us back to current due to 2 players)
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                while (players.get(currentPlayerIndex).isPassive()) {
                    currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                }
            } else {
                // With 3+ active players, Reverse changes direction and skips the next player
                // in the new direction - the current player does NOT go again
                currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                while (players.get(currentPlayerIndex).isPassive()) {
                    currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
                }
            }
        }

        while (players.get(currentPlayerIndex).isPassive()) {
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        }

        Card cardForResponse = card;
        if (card.getCardColour().equals("black")) {
            cardForResponse = new Card(card.getCardValue(), card.getCardColour(),
                    msg.chosenColor(), card.getCardId());
            String colorToSet = (msg.chosenColor() != null && !msg.chosenColor().isBlank()) ? msg.chosenColor() : currentColor;
            if (colorToSet != null && !colorToSet.isBlank()) {
                cardForResponse.setChosenColour(colorToSet);
            }
        }

        GameTurnResponse response = new GameTurnResponse(player.getPlayerIndex(), cardForResponse, drawPenaltyValue, currentPlayerIndex, directionClockwise, currentColor);
        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
            c.sendMessage(response);
        }

        for (Player p : new ArrayList<>(players)) {
            for (Player lobbyPlayer : new ArrayList<>(players)) {
                for (Enemy e : new ArrayList<>(lobbyPlayer.getEnemies())) {
                    if (e.getUsername().equals(p.getUsername())) {
                        e.setEnemy(new Enemy(p));
                        break;
                    }
                }
            }

            Enemy updatedEnemy = new Enemy(p);
            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
                c.sendMessage(new UpdateEnemyResponse(updatedEnemy));
            }
        }

        updateEnemy(new Enemy(player));
    }

    private boolean isLegalCardPlay(Card card, String chosenColor) {
        if (card.getCardColour().equals("black")) {
            if (card.getCardValue() != 13 && card.getCardValue() != 14) {
                return false;
            }
            if (chosenColor == null || !CARD_COLORS.contains(chosenColor)) {
                return false;
            }
        } else if (!CARD_COLORS.contains(card.getCardColour())
                || card.getCardValue() < 0 || card.getCardValue() > 12) {
            return false;
        }

        Card topCard = cardDeck.getTopDiscardCard();
        if (topCard == null) {
            topCard = currentCard;
        }
        if (topCard == null) {
            return false;
        }

        if (drawPenaltyValue != null && drawPenaltyValue > 0) {
            return (card.getCardValue() == 12 || card.getCardValue() == 14)
                    && !(topCard.getCardValue() == 14 && card.getCardValue() == 12);
        }

        if (card.getCardColour().equals("black")) {
            return true;
        }

        String effectiveColor;
        if (topCard.getCardColour().equals("black")) {
            effectiveColor = currentColor;
            if (effectiveColor == null || effectiveColor.isBlank()) {
                effectiveColor = topCard.getChosenColour();
            }
        } else {
            effectiveColor = topCard.getCardColour();
        }
        return card.getCardColour().equals(effectiveColor)
                || card.getCardValue() == topCard.getCardValue();
    }


    private void updateEnemy(Enemy enemy) {
        if (enemy == null || enemy.getUsername() == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
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
        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
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


    synchronized void requestCard(ServerSocketConnection requester, RequestCardRequest msg) {
        if (requester == null || requester.getUser() == null || msg == null || msg.player() == null) {
            System.out.println("Rejected invalid card draw request");
            return;
        }

        String username = requester.getUser().getUsername();
        if (username == null || !username.equals(msg.player().getUsername())) {
            System.out.println("Rejected card draw request with mismatched player identity");
            return;
        }

        Player player = null;
        for (Player p : players) {
            if (p.getUsername().equals(username)) {
                player = p;
                break;
            }
        }

        if (player == null) {
            System.out.println("Rejected card draw request from a player not in this game");
            return;
        }

        int expectedAmount = drawPenaltyValue != null && drawPenaltyValue > 0 ? drawPenaltyValue : 1;
        if (player.isPassive()
                || !Objects.equals(player.getPlayerIndex(), currentPlayerIndex)
                || msg.amount() != expectedAmount) {
            System.out.println("Rejected out-of-turn or invalid card draw request from " + username);
            return;
        }

        for (int i = 0; i < expectedAmount; i++) {
            Card card = getCardDeck().getCardFromStack();
            addCardsToPlayer(player, card);
        }
        drawPenaltyValue = 0;

        // Drawing cards clears any pending UNO obligation
        pendingUnoPenalty.remove(player.getUsername());
        if (player.getUsername().equals(lastPlayerWhoSaidUno)) {
            lastPlayerWhoSaidUno = null;
            unoTimeoutTimestamp = 0;
        }

        if (checkAndSendGameOver()) {
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();

        while (players.get(currentPlayerIndex).isPassive()) {
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        }

        Card cardToSend = getCardDeck().getTopDiscardCard();
        if (cardToSend == null) {
            cardToSend = getCurrentCard();
        }

        if (cardToSend != null && !cardToSend.getCardColour().equals("black")) {
            currentColor = null;
        }

        GameTurnResponse response = new GameTurnResponse(null, null, 0, currentPlayerIndex, directionClockwise, currentColor);
        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
            c.sendMessage(response);
        }

        for (Player p : new ArrayList<>(players)) {
            for (Player lobbyPlayer : new ArrayList<>(players)) {
                for (Enemy e : new ArrayList<>(lobbyPlayer.getEnemies())) {
                    if (e.getUsername().equals(p.getUsername())) {
                        e.setEnemy(new Enemy(p));
                        break;
                    }
                }
            }

            Enemy updatedEnemy = new Enemy(p);
            for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
                c.sendMessage(new UpdateEnemyResponse(updatedEnemy));
            }
        }

        updateEnemy(new Enemy(player));
    }


    private void checkForWinner(Player player) {
        if (player.getHand().isEmpty()) {
            player.setPassive(true);
            pendingUnoPenalty.remove(player.getUsername()); // clean up — they finished legitimately
            addToFinishOrder(player.getUsername());
            updateEnemy(new Enemy(player));
        }
    }

    synchronized void handlePlayerLeft(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        for (Player player : players) {
            if (username.equals(player.getUsername())) {
                player.setPassive(true);
                leftPlayers.add(username);
                pendingUnoPenalty.remove(username);
                updateEnemy(new Enemy(player));
                break;
            }
        }

        checkAndSendGameOver();
    }

    private void addToFinishOrder(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (!finishOrder.contains(username)) {
            finishOrder.add(username);
        }
    }

    private int countActivePlayers() {
        int active = 0;
        for (Player player : players) {
            if (player != null && !player.isPassive()) {
                active++;
            }
        }
        return active;
    }

    private boolean checkAndSendGameOver() {
        if (gameOverSent || players.isEmpty()) {
            return gameOverSent;
        }

        int activePlayers = countActivePlayers();
        if (activePlayers > 1) {
            return false;
        }

        if (activePlayers == 1) {
            for (Player player : players) {
                if (player != null && !player.isPassive()) {
                    player.setPassive(true);
                    addToFinishOrder(player.getUsername());
                    updateEnemy(new Enemy(player));
                    break;
                }
            }
        }

        ArrayList<Player> ranking = buildRankingForGameOver();
        GameOverResponse msg = new GameOverResponse(ranking, new ArrayList<>(leftPlayers));
        lobby.sendInfoToAll(msg);
        updateGameStats(msg);
        gameOverSent = true;
        return true;
    }


    /**
     * Saves the result of the match. The games won and lost of the users are calculated
     * from the match results by the database view user_games_stats.
     */
    private void updateGameStats(GameOverResponse msg) {
        try {
            DatabaseMatchStates.addMatchState(getPlayerIdsFromRanking(msg.getPlayers()));
        } catch (Exception e) {
            System.err.println("Failed to save match result: " + e.getMessage());
        }
    }

    private List<Integer> getPlayerIdsFromRanking(ArrayList<Player> players) {
        List<Integer> playerIds = new ArrayList<>();
        for (Player player : players) {
            if (player != null && player.getUsername() != null) {
                try {
                    playerIds.add(DatabaseUser.getUserByUserName(player.getUsername()).getId());
                } catch (Exception e) {
                    System.err.println("Failed to retrieve user ID for username " + player.getUsername() + ": " + e.getMessage());
                }
            }
        }
        return playerIds;
    }


    private ArrayList<Player> buildRankingForGameOver() {
        ArrayList<Player> ranking = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (String username : finishOrder) {
            if (leftPlayers.contains(username)) {
                continue;
            }

            Player player = findPlayerByUsername(username);
            if (player != null && added.add(username)) {
                ranking.add(player);
            }
        }

        for (Player player : players) {
            if (player != null
                    && player.getUsername() != null
                    && !leftPlayers.contains(player.getUsername())
                    && added.add(player.getUsername())) {
                ranking.add(player);
            }
        }

        for (String username : leftPlayers) {
            Player player = findPlayerByUsername(username);
            if (player != null && added.add(username)) {
                ranking.add(player);
            }
        }

        return ranking;
    }

    private Player findPlayerByUsername(String username) {
        if (username == null) {
            return null;
        }

        for (Player player : players) {
            if (player != null && username.equals(player.getUsername())) {
                return player;
            }
        }

        return null;
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

    /**
     * Handles the UNO logic — called when the client sends a SayUnoRequest.
     * Clears the pending UNO penalty if the player said UNO in time.
     */
    synchronized void sayUno(SayUnoRequest msg) {
        if (msg == null || msg.player() == null) {
            return;
        }

        Player player = findPlayerByUsername(msg.player().getUsername());
        if (player == null) {
            return;
        }

        if (!player.getUsername().equals(lastPlayerWhoSaidUno)) {
            return;
        }

        // Player already finished — ignore any late UNO messages
        if (player.isPassive() || player.getHand().isEmpty()) {
            pendingUnoPenalty.remove(player.getUsername());
            lastPlayerWhoSaidUno = null;
            unoTimeoutTimestamp = 0;
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - unoTimeoutTimestamp > 3000) {
            handleForgottenUNO(player, false);
        } else {
            // Player said UNO in time — remove penalty obligation
            pendingUnoPenalty.remove(player.getUsername());
            handleSuccessfulUNO(player, true);
        }

        lastPlayerWhoSaidUno = null;
        unoTimeoutTimestamp = 0;
    }

    private void handleSuccessfulUNO(Player player, boolean didSayUno) {
        playersSaidUno.add(player.getUsername());

        UnoNotificationResponse response = new UnoNotificationResponse(player.getUsername(), didSayUno);
        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
            c.sendMessage(response);
        }
    }

    private void handleForgottenUNO(Player player, boolean didSayUno) {
        // Only penalize if still pending (not already handled via cardPlayed interception)
        if (!pendingUnoPenalty.contains(player.getUsername())) {
            return;
        }
        // Never penalize a player who already finished (passive = won or left)
        if (player.isPassive() || player.getHand().isEmpty()) {
            pendingUnoPenalty.remove(player.getUsername());
            return;
        }
        pendingUnoPenalty.remove(player.getUsername());

        for (int i = 0; i < 2; i++) {
            Card penaltyCard = getCardDeck().getCardFromStack();
            addCardsToPlayer(player, penaltyCard);
        }

        playersSaidUno.remove(player.getUsername());

        UnoNotificationResponse response = new UnoNotificationResponse(player.getUsername(), didSayUno);
        for (ServerSocketConnection c : new ArrayList<>(lobby.getConnections())) {
            c.sendMessage(response);
        }
    }

    /**
     * Called from the timeout thread after 3 seconds.
     * Uses the username snapshot taken at the time of the card play to avoid race conditions.
     */
    private synchronized void checkUnoTimeout(String username) {
        if (username == null) {
            return;
        }

        Player player = findPlayerByUsername(username);
        if (player != null && pendingUnoPenalty.contains(username)) {
            handleForgottenUNO(player, false);
        }

        if (username.equals(lastPlayerWhoSaidUno)) {
            lastPlayerWhoSaidUno = null;
            unoTimeoutTimestamp = 0;
        }
    }
}