package htl.steyr.uno.Lobby;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.User;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import htl.steyr.uno.requests.server.StartGameResponse;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller für die Lobby-Warteansicht des UNO-Spiels.
 *
 * <p>Verwaltet die Anzeige aller Spieler in der Lobby, den Gruppen-Chat,
 * sowie die Aktionen "Spielen" und "Lobby verlassen". Der Host kann das
 * Spiel starten, sobald mindestens zwei Spieler anwesend sind.</p>
 */
public class LobbyWaitController implements Initializable {

    /** Zeigt den Lobby-Code an. */
    public Label lobbyCodeLabel;

    /** Button zum Starten des Spiels (nur für den Host sichtbar). */
    public Button playButton;

    /** Zeigt den eingeloggten Benutzernamen an. */
    public Label AccNameDisplayLabel;

    /** Button zum Verlassen der Lobby. */
    public Button leaveLobbyButton;

    /** Liste aller Spieler in der Lobby. */
    public ListView<String> playerListView = new ListView<>();

    /** Liste aller Chat-Nachrichten. */
    public ListView<ChatMessage> gameChatListView = new ListView<>();

    /** Eingabefeld für neue Chat-Nachrichten. */
    public TextField gameChatTextField;

    /** Gibt an, ob das Fenster absichtlich geschlossen wurde (z.B. durch Lobby verlassen). */
    private boolean intentionalClose = false;

    /** Steuert, ob der Chat-Bereich ein- oder ausgeklappt ist. */
    private final BooleanProperty chatExpanded = new SimpleBooleanProperty(false);

    /** Der Client für die Serververbindung. */
    private final Client client;

    /** Die aktuellen Lobby-Informationen vom Server. */
    private LobbyInfoResponse lobby;

    /**
     * Erstellt einen neuen {@code LobbyWaitController}.
     *
     * @param client der aktive Client mit der Serververbindung
     * @param lobby  die initialen Lobby-Informationen
     */
    public LobbyWaitController(Client client, LobbyInfoResponse lobby) {
        this.client = client;
        this.lobby = lobby;
    }

    /**
     * Initialisiert die Lobby-Warteansicht.
     *
     * <p>Setzt den Lobby-Code, den Benutzernamen, die Chat-Zelldarstellung
     * sowie die Ein-/Ausklapp-Animation des Chats. Registriert außerdem
     * einen Handler für unerwartete Fenster-Schließereignisse.</p>
     *
     * @param url            nicht verwendet
     * @param resourceBundle nicht verwendet
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            if (playButton.getScene() != null && playButton.getScene().getWindow() != null) {
                playButton.getScene().getWindow().setOnCloseRequest(event -> {
                    if (!intentionalClose) onSceneClose();
                });
            }
        });

        String lobbyCode = lobby.lobbyId().toString();
        lobbyCodeLabel.setText("Lobby Code: " + lobbyCode);

        AccNameDisplayLabel.setText("Account: " + client.getConn().getUser().getUsername());

        final String currentUsername = client.getConn().getUser().getUsername();

        gameChatListView.setCellFactory(list -> new ListCell<ChatMessage>() {
            @Override
            protected void updateItem(ChatMessage item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean myMessage = currentUsername.equals(item.sender());

                Label nameLabel = new Label(item.sender());
                nameLabel.getStyleClass().add("chatSenderLabel");

                Label bubble = new Label(item.text());
                bubble.setWrapText(true);
                bubble.setMaxWidth(260);
                bubble.getStyleClass().add(myMessage ? "chatBubbleMine" : "chatBubbleOther");

                VBox messageBox = new VBox(3, nameLabel, bubble);
                messageBox.setFillWidth(false);
                messageBox.setAlignment(myMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                HBox row = new HBox(messageBox);
                row.setPadding(new Insets(6, 10, 6, 10));
                row.setAlignment(myMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setText(null);
                setGraphic(row);
            }
        });

        gameChatListView.setPrefHeight(0);
        gameChatListView.setMinHeight(0);
        gameChatListView.setVisible(false);
        gameChatListView.setManaged(false);

        Timeline expand = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(gameChatListView.prefHeightProperty(), 300))
        );

        Timeline collapse = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(gameChatListView.prefHeightProperty(), 0))
        );

        chatExpanded.addListener((obs, oldVal, open) -> {
            expand.stop();
            collapse.stop();

            if (open) {
                gameChatListView.setManaged(true);
                gameChatListView.setVisible(true);
                expand.playFromStart();
            } else {
                collapse.setOnFinished(ev -> {
                    gameChatListView.setVisible(false);
                    gameChatListView.setManaged(false);
                });
                collapse.playFromStart();
            }
        });

        gameChatTextField.setOnMouseClicked(e -> chatExpanded.set(!chatExpanded.get()));

        updateLobbyInfo();
    }

    /**
     * Aktualisiert die Spielerliste und den Zustand des Spielen-Buttons
     * anhand der aktuellen Lobby-Informationen.
     *
     * <p>Der Spielen-Button ist nur für den Host sichtbar und nur aktiviert,
     * wenn mindestens zwei Spieler in der Lobby sind. Jede Zelle zeigt
     * das Profilbild und den Benutzernamen des Spielers an.</p>
     */
    public void updateLobbyInfo() {
        playerListView.getItems().clear();

        String currentUsername = client.getConn().getUser().getUsername();
        ArrayList<User> users = new ArrayList<>(lobby.users());

        String hostUsername = users.isEmpty() ? null : users.getFirst().getUsername();
        boolean isHost = currentUsername.equals(hostUsername);
        boolean hasEnoughPlayersToStart = users.size() >= 2;

        playButton.setVisible(isHost);
        playButton.setDisable(!isHost || !hasEnoughPlayersToStart);

        playerListView.setCellFactory(list -> new ListCell<String>() {
            private final BorderPane rootPane = new BorderPane();
            private final Label nameLabel = new Label();
            private final HBox buttonBox = new HBox(10);
            private final Button kickButton = new Button("Kick");
            private final Button muteButton = new Button("Mute");

            {
                nameLabel.setMinWidth(180);
                nameLabel.setMaxWidth(180);
                nameLabel.setPrefWidth(180);
                nameLabel.setAlignment(Pos.CENTER_LEFT);

                rootPane.setPadding(new Insets(6, 10, 6, 10));
                BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
                rootPane.getStyleClass().add("playerCellRoot");

                setPrefHeight(85);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                User matchedUser = users.stream()
                        .filter(u -> u.getUsername().equals(item))
                        .findFirst()
                        .orElse(null);

                ImageView profileImageView = new ImageView(
                        matchedUser != null
                                ? getProfileImage(matchedUser)
                                : new Image(Objects.requireNonNull(
                                getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")
                        ), 50, 50, true, true)
                );

                profileImageView.setFitWidth(70);
                profileImageView.setFitHeight(70);
                profileImageView.setPreserveRatio(true);
                profileImageView.setPickOnBounds(true);

                HBox nameAndImage = new HBox(10, profileImageView, nameLabel);
                nameAndImage.setAlignment(Pos.CENTER_LEFT);
                rootPane.setLeft(nameAndImage);

                if (item.equals(currentUsername)) {
                    rootPane.setRight(null);
                    nameLabel.setText(item);
                } else {
                    if (isHost) {
                        rootPane.setRight(buttonBox);
                        BorderPane.setAlignment(buttonBox, Pos.CENTER);
                    } else {
                        rootPane.setRight(null);
                    }
                    nameLabel.setText(item);
                }

                setGraphic(rootPane);
            }
        });

        for (User u : users) {
            playerListView.getItems().add(u.getUsername());
        }
    }

    /**
     * Lädt das Profilbild eines Benutzers.
     *
     * <p>Gibt das gespeicherte Profilbild zurück, falls vorhanden.
     * Andernfalls wird das Standard-Profilbild aus den Ressourcen geladen.</p>
     *
     * @param user der Benutzer, dessen Profilbild geladen werden soll
     * @return das Profilbild als {@link Image}
     */
    private Image getProfileImage(User user) {
        byte[] imageData = user.getProfileImageData();
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData), 50, 50, true, true);
        }

        return new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")),
                50, 50, true, true
        );
    }

    /**
     * Sendet eine Chat-Nachricht beim Drücken der Enter-Taste im Textfeld.
     *
     * <p>Leere Nachrichten werden ignoriert. Nachrichten die mit {@code @}
     * beginnen, werden zusätzlich als Privatnachricht behandelt.</p>
     *
     * @param actionEvent das ausgelöste ActionEvent
     */
    public void onSendMassage(ActionEvent actionEvent) {
        String text = gameChatTextField.getText();

        if (text != null && !text.isBlank()) {
            String trimmed = text.trim();
            gameChatTextField.clear();
            sendChatMessage(trimmed);

            if (trimmed.startsWith("@")) {
                gameChatTextField.clear();
                sendPrivateMessage(trimmed);
            }
        }
    }

    /**
     * Wird aufgerufen, wenn das Fenster geschlossen wird.
     * Trennt die Serververbindung des Clients.
     */
    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    /**
     * Startet das Spiel auf Anfrage des Hosts.
     *
     * <p>Wird ignoriert, wenn die Lobby nicht existiert oder weniger
     * als zwei Spieler vorhanden sind.</p>
     *
     * @param actionEvent das ausgelöste ActionEvent
     * @throws IOException wenn das Laden der Spielansicht fehlschlägt
     */
    public void playButtonClicked(ActionEvent actionEvent) throws IOException {
        if (lobby == null || lobby.users() == null || lobby.users().size() < 2) {
            return;
        }
        client.getConn().startGame();
    }

    /**
     * Wechselt zur Spielansicht nachdem der Server den Spielstart bestätigt hat.
     *
     * <p>Lädt die {@code gameTable.fxml}, setzt den entsprechenden Controller
     * und schließt das aktuelle Lobby-Fenster.</p>
     *
     * @param msg die Spielstart-Antwort des Servers
     * @throws IOException wenn das Laden der Spielansicht fehlschlägt
     */
    private void startGame(StartGameResponse msg) throws IOException {
        intentionalClose = true;

        Stage stage = new Stage();
        Stage thisStage = (Stage) playButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("gameTable.fxml"));

        GameTable controller = new GameTable(client, msg);
        loader.setController(controller);
        client.setLobbyController(null);
        client.setLobbyWaitController(null);
        client.setGameTable(controller);

        Scene scene = new Scene(loader.load());
        UiStyleUtil.applyGlobalFocusStyle(scene);

        stage.setTitle("UNO - Game");
        stage.setScene(scene);
        UiStyleUtil.setAppIcon(stage);
        stage.show();

        try {
            thisStage.setOnCloseRequest(null);
        } catch (Exception ignored) {}
        thisStage.close();
    }

    /**
     * Wird vom Client aufgerufen, wenn der Server den Spielstart signalisiert.
     * Führt den Szenenwechsel auf dem JavaFX-Application-Thread aus.
     *
     * @param msg die Spielstart-Antwort des Servers
     */
    public void startGameResponse(StartGameResponse msg) {
        Platform.runLater(() -> {
            try {
                startGame(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Verlässt die aktuelle Lobby und kehrt zur Lobby-Übersicht zurück.
     *
     * <p>Benachrichtigt den Server, lädt die {@code lobby.fxml} und
     * schließt das aktuelle Fenster.</p>
     *
     * @param actionEvent das ausgelöste ActionEvent
     * @throws IOException wenn das Laden der Lobby-Ansicht fehlschlägt
     */
    public void leaveLobbyButtonClicked(ActionEvent actionEvent) throws IOException {
        intentionalClose = true;

        client.getConn().leaveLobby();

        Stage stage = new Stage();
        Stage thisStage = (Stage) leaveLobbyButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));

        LobbyController controller = new LobbyController(client);
        loader.setController(controller);
        client.setLobbyController(controller);
        client.setLobbyWaitController(null);

        Scene scene = new Scene(loader.load());
        UiStyleUtil.applyGlobalFocusStyle(scene);

        stage.setTitle("WarteLobby");
        stage.setScene(scene);
        UiStyleUtil.setAppIcon(stage);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }

    /**
     * Gibt die aktuellen Lobby-Informationen zurück.
     *
     * @return die aktuelle {@link LobbyInfoResponse}
     */
    public LobbyInfoResponse getLobby() {
        return lobby;
    }

    /**
     * Aktualisiert die Lobby-Informationen und erneuert die Spielerliste
     * auf dem JavaFX-Application-Thread.
     *
     * @param lobby die neuen Lobby-Informationen vom Server
     */
    public void setLobby(LobbyInfoResponse lobby) {
        this.lobby = lobby;
        Platform.runLater(this::updateLobbyInfo);
    }

    /**
     * Sendet eine normale Chat-Nachricht an alle Spieler in der Lobby.
     *
     * @param message der zu sendende Nachrichtentext
     */
    private void sendChatMessage(String message) {
        client.sendChatMessage(message);
    }

    /**
     * Sendet eine Privatnachricht an einen bestimmten Spieler.
     *
     * @param message die Privatnachricht im Format {@code @Benutzername Nachricht}
     */
    private void sendPrivateMessage(String message) {
        // client.sendPrivateMesssage(message);
    }

    /**
     * Empfängt eine Chat-Nachricht vom Server und zeigt sie in der Chat-Liste an.
     *
     * <p>Klappt den Chat automatisch auf, falls er noch geschlossen ist,
     * und scrollt zur neuesten Nachricht.</p>
     *
     * @param msg die empfangene Chat-Nachricht vom Server
     */
    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.user().getUsername();
        String text = msg.message();

        Platform.runLater(() -> {
            gameChatListView.getItems().add(new ChatMessage(sender, text));

            if (!chatExpanded.get()) {
                chatExpanded.set(true);
            }

            int lastIndex = gameChatListView.getItems().size() - 1;
            if (lastIndex >= 0) {
                gameChatListView.scrollTo(lastIndex);
            }
        });
    }

    /**
     * Repräsentiert eine einzelne Chat-Nachricht mit Absender und Text.
     *
     * @param sender der Benutzername des Absenders
     * @param text   der Inhalt der Nachricht
     */
    public record ChatMessage(String sender, String text) {
    }
}