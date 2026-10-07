package htl.steyr.uno.client;

import htl.steyr.uno.User;
import htl.steyr.uno.protocol.JsonMessageCodec;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for the JSON protocol. The public request methods and the
 * response callbacks intentionally retain the old client-facing API.
 */
public class ClientSocketConnection extends WebSocketClient implements Closeable {
    private final Client client;
    private volatile boolean running;
    private Thread heartbeatThread;
    private User user;
    private LobbyInfoResponse lobby;

    public ClientSocketConnection(String host, int port, Client client) throws IOException {
        super(createUri(host, port));
        this.client = client;
        try {
            if (!connectBlocking(10, TimeUnit.SECONDS)) {
                throw new IOException("WebSocket connection timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connection interrupted", e);
        }
    }

    private static URI createUri(String host, int port) throws IOException {
        String normalized = host.startsWith("ws://") || host.startsWith("wss://")
                ? host
                : "ws://" + host;
        try {
            URI uri = new URI(normalized);
            if (uri.getPort() >= 0) {
                return uri;
            }
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), port,
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid WebSocket server address", e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonMessageCodec.DecodedMessage decoded = JsonMessageCodec.decode(message);
            switch (decoded.type()) {
                case "LoginSuccessResponse" -> logInSuccessResponse(JsonMessageCodec.convert(decoded, LoginSuccessResponse.class));
                case "LoginFailedResponse" -> logInFailedResponse(JsonMessageCodec.convert(decoded, LoginFailedResponse.class));
                case "LobbyInfoResponse" -> lobbyInfoResponse(JsonMessageCodec.convert(decoded, LobbyInfoResponse.class));
                case "LobbyNotFoundResponse" -> lobbyNotFoundResponse(JsonMessageCodec.convert(decoded, LobbyNotFoundResponse.class));
                case "LobbyJoinRefusedResponse" -> lobbyJoinRefusedResponse(JsonMessageCodec.convert(decoded, LobbyJoinRefusedResponse.class));
                case "CreateAccountSuccessResponse" -> createAccountSuccessResponse(JsonMessageCodec.convert(decoded, CreateAccountSuccessResponse.class));
                case "ReceiveChatMessageResponse" -> receiveChatMessageResponse(JsonMessageCodec.convert(decoded, ReceiveChatMessageResponse.class));
                case "ForgotPasswordResponse" -> forgotPasswordResponse(JsonMessageCodec.convert(decoded, ForgotPasswordResponse.class));
                case "CheckIfUserAlreadyExistsResponse" -> checkIfUserAlreadyExistsResponse(JsonMessageCodec.convert(decoded, CheckIfUserAlreadyExistsResponse.class));
                case "StartGameResponse" -> startGameResponse(JsonMessageCodec.convert(decoded, StartGameResponse.class));
                case "CreateAccountFailedResponse" -> createAccountFailedResponse(JsonMessageCodec.convert(decoded, CreateAccountFailedResponse.class));
                case "CardAddResponse" -> cardAddResponse(JsonMessageCodec.convert(decoded, CardAddResponse.class));
                case "PlayerGetResponse" -> playerGetResponse(JsonMessageCodec.convert(decoded, PlayerGetResponse.class));
                case "StackInfoResponse" -> stackInfoResponse(JsonMessageCodec.convert(decoded, StackInfoResponse.class));
                case "UpdateEnemyResponse" -> updateEnemyResponse(JsonMessageCodec.convert(decoded, UpdateEnemyResponse.class));
                case "GameTurnResponse" -> gameTurnResponse(JsonMessageCodec.convert(decoded, GameTurnResponse.class));
                case "GameOverResponse" -> gameOverResponse(JsonMessageCodec.convert(decoded, GameOverResponse.class));
                case "UnoNotificationResponse" -> unoNotificationResponse(JsonMessageCodec.convert(decoded, UnoNotificationResponse.class));
                case "HeartbeatPongResponse" -> heartbeatPongResponse(JsonMessageCodec.convert(decoded, HeartbeatPongResponse.class));
                case "EnemyDrawnCardsResponse" -> enemyDrawnCardsResponse(JsonMessageCodec.convert(decoded, EnemyDrawnCardsResponse.class));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        running = false;
    }

    @Override
    public void onError(Exception exception) {
    }

    public synchronized void sendMessage(Object message) {
        if (!running || !isOpen()) {
            return;
        }
        try {
            send(JsonMessageCodec.encode(message));
        } catch (IOException e) {
            throw new RuntimeException("Could not encode UNO message", e);
        }
    }

    public void startReceiving() {
        running = true;
        startHeartbeat();
    }

    private void logInSuccessResponse(LoginSuccessResponse msg) {
        user = msg.user();
        client.getLoginController().logInSuccess(user);
    }

    private void logInFailedResponse(LoginFailedResponse msg) {
        client.getLoginController().logInFailed(msg);
    }

    private void lobbyInfoResponse(LobbyInfoResponse msg) {
        lobby = msg;
        if (msg.users() != null) {
            if (client.getLobbyWaitController() != null) {
                client.getLobbyWaitController().setLobby(msg);
            } else if (client.getLobbyController() != null) {
                client.getLobbyController().createOrJoinPartySuccess(msg);
            }
        }
    }

    private void lobbyNotFoundResponse(LobbyNotFoundResponse msg) {
        client.getLobbyController().lobbyNotFound();
    }

    private void lobbyJoinRefusedResponse(LobbyJoinRefusedResponse msg) {
        client.getLobbyController().joinPartyFailed(msg);
    }

    private void createAccountSuccessResponse(CreateAccountSuccessResponse msg) {
        client.getLoginController().createAccountSuccess(msg);
    }

    private void createAccountFailedResponse(CreateAccountFailedResponse msg) {
        client.getLoginController().createAccountFailedResponse(msg);
    }

    private void receiveChatMessageResponse(ReceiveChatMessageResponse msg) {
        if (client.getLobbyWaitController() != null) {
            client.getLobbyWaitController().receiveChatMessage(msg);
        } else if (client.getLobbyController() != null) {
            client.getLobbyController().receiveChatMessage(msg);
        }
    }

    private void forgotPasswordResponse(ForgotPasswordResponse msg) {
        client.getLoginController().forgotPasswordResponse(msg);
    }

    private void checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse msg) {
        client.getLoginController().checkIfUserAlreadyExistsResponse(msg);
    }

    private void startGameResponse(StartGameResponse msg) throws IOException {
        client.getLobbyWaitController().startGameResponse(msg);
    }

    private void cardAddResponse(CardAddResponse msg) {
        client.getGameTable().getGameLogic().cardAddResponse(msg);
    }

    private void playerGetResponse(PlayerGetResponse msg) {
        client.getGameTable().getGameLogic().playerGetResponse(msg);
    }

    private void stackInfoResponse(StackInfoResponse msg) {
        client.getGameTable().getGameLogic().withDrawStackInfoResponse(msg);
    }

    private void updateEnemyResponse(UpdateEnemyResponse msg) {
        if (client.getGameTable() != null) {
            client.getGameTable().getGameLogic().updateEnemyResponse(msg);
        }
    }

    private void gameTurnResponse(GameTurnResponse msg) {
        client.getGameTable().getGameLogic().gameTurnResponse(msg);
    }

    private void gameOverResponse(GameOverResponse msg) {
        client.getGameTable().getGameLogic().gameOverResponse(msg);
    }

    private void unoNotificationResponse(UnoNotificationResponse msg) {
        if (client.getGameTable() != null) {
            client.getGameTable().getGameLogic().unoNotificationResponse(msg);
        }
    }

    private void enemyDrawnCardsResponse(EnemyDrawnCardsResponse msg) {
        // Kept as a protocol callback for clients that use this response.
    }

    public void requestPasswordReset(String email) {
        sendMessage(new ForgotPasswordRequest(email));
    }

    public void verifyPasswordResetCode(int code) {
        sendMessage(new ForgotPasswordSendCodeRequest(code));
    }

    public void setNewPassword(String email, int code, String password) {
        sendMessage(new ChangePasswordRequest(email, code, password));
    }

    public void checkIfUserAlreadyExists(String username, String email) {
        sendMessage(new CheckIfUserAlreadyExistsRequest(username, email));
    }

    public void startGame() {
        sendMessage(new StartGameRequest(getUser()));
    }

    public void setProfileImageRequest(byte[] imageBytes) {
        sendMessage(new SetProfileImageRequest(getUser(), imageBytes));
    }

    private void heartbeatPongResponse(HeartbeatPongResponse msg) {
    }

    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(30000);
                    if (running && isOpen()) {
                        sendMessage(new HeartbeatPingRequest(new java.sql.Timestamp(System.currentTimeMillis())));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    return;
                }
            }
        }, "Heartbeat-Client");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    public void leaveLobby() {
        if (lobby != null) {
            lobby = null;
            sendMessage(new LeaveLobbyRequest(getUser()));
        }
    }

    @Override
    public void close() {
        running = false;
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
        super.close();
    }

    public User getUser() {
        return user;
    }
}
