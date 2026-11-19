package htl.steyr.uno;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.client.ChangeLobbyConfigRequest;
import htl.steyr.uno.requests.client.UserReadyRequest;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import htl.steyr.uno.requests.server.StartGameResponse;
import htl.steyr.uno.requests.server.UserRequestsJoinLobbyResponse;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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

    @FXML private Button unoClassicButton;
    @FXML private Button unoDosButton;

    @FXML private Button openLobbyButton;
    @FXML private Button onRequestLobbyButton;
    @FXML private Button codeOnlyButton;

    /** Zeigt den Lobby-Code an. */
    public Label lobbyCodeLabel;

    /** Button zum Setzen des eigenen Ready-Status. */
    public Button readyButton;

    private boolean currentPlayerReady;

    /** Zeigt den eingeloggten Benutzernamen an. */
    public Label AccNameDisplayLabel;

    /** Button zum Verlassen der Lobby. */
    public Button leaveLobbyButton;

    /** Container für den Chat-Bereich. */
    public StackPane chatBox;

    /** Overlay für das Kick-Pop-up. */
    public StackPane kickPopupOverlay;

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

    /** Steuert, ob der Lobby-Code in die Zwischenablage kopiert werden kann. */
    private boolean copyToClipboardEnabled = true;

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
        unoClassicButton.getStyleClass().add("selectedOption");
        codeOnlyButton.getStyleClass().add("selectedOption");

        Platform.runLater(() -> {
            if (readyButton.getScene() != null && readyButton.getScene().getWindow() != null) {
                readyButton.getScene().getWindow().setOnCloseRequest(event -> {
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

        chatBox.setPrefHeight(720);
        chatBox.setMinHeight(720);
        chatBox.setMaxHeight(720);
        chatBox.setOpacity(0);
        chatBox.setVisible(false);
        chatBox.setManaged(true);

        Timeline expand = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(chatBox.opacityProperty(), 0)
                ),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(chatBox.opacityProperty(), 1)
                )
        );

        Timeline collapse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(chatBox.opacityProperty(), 1)
                ),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(chatBox.opacityProperty(), 0)
                )
        );

        chatExpanded.addListener((obs, oldVal, open) -> {
            expand.stop();
            collapse.stop();

            if (open) {
                chatBox.setVisible(true);
                gameChatListView.setManaged(true);
                gameChatListView.setVisible(true);
                expand.playFromStart();
            } else {
                collapse.setOnFinished(ev -> {
                    chatBox.setVisible(false);
                    gameChatListView.setVisible(false);
                    gameChatListView.setManaged(false);
                });
                collapse.playFromStart();
            }
        });

        gameChatTextField.setOnMouseClicked(e -> chatExpanded.set(!chatExpanded.get()));
        lobbyCodeLabel.setOnMouseClicked(e -> copyLobbyCodeToClipboard());

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
        currentPlayerReady = users.stream()
                .filter(user -> currentUsername.equals(user.getUsername()))
                .findFirst()
                .map(User::isReady)
                .orElse(false);

        String hostUsername = users.isEmpty() ? null : users.getFirst().getUsername();
        boolean isHost = currentUsername.equals(hostUsername);
        boolean hasEnoughPlayersToStart = users.size() >= 2;

        readyButton.setVisible(true);
        readyButton.setDisable(!hasEnoughPlayersToStart);

        playerListView.setCellFactory(list -> new ListCell<String>() {
            private final BorderPane rootPane = new BorderPane();
            private final Label nameLabel = new Label();
            private final Label readyLabel = new Label();
            private final HBox statusAndActions = new HBox(10);
            private final HBox buttonBox = new HBox(10);
            private final Button kickButton = new Button("Kick");
            private final Button muteButton = new Button("Mute");

            {
                nameLabel.setMinWidth(0);
                nameLabel.setPrefWidth(180);
                nameLabel.setMaxWidth(220);
                nameLabel.setAlignment(Pos.CENTER_LEFT);

                readyLabel.setMinWidth(100);
                readyLabel.setMaxWidth(140);
                readyLabel.setPrefWidth(120);
                readyLabel.setAlignment(Pos.CENTER);
                readyLabel.getStyleClass().add("readyStatusLabel");

                kickButton.setPrefWidth(120);
                kickButton.setMinWidth(120);
                kickButton.setMaxWidth(120);
                kickButton.setAlignment(Pos.CENTER);
                kickButton.getStyleClass().addAll("playButton", "kickButton");
                buttonBox.setAlignment(Pos.CENTER);
                statusAndActions.setAlignment(Pos.CENTER_RIGHT);

                buttonBox.getChildren().add(kickButton);
                statusAndActions.getChildren().add(readyLabel);

                rootPane.setPadding(new Insets(6, 10, 6, 10));
                BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
                BorderPane.setAlignment(buttonBox, Pos.CENTER);
                BorderPane.setAlignment(statusAndActions, Pos.CENTER_RIGHT);
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

                HBox playerInfo = new HBox(10, profileImageView, nameLabel);
                playerInfo.setAlignment(Pos.CENTER_LEFT);
                rootPane.setLeft(playerInfo);
                boolean playerReady = item.equals(currentUsername)
                        ? currentPlayerReady
                        : matchedUser != null && matchedUser.isReady();
                readyLabel.setText(playerReady ? "Bereit" : "Nicht bereit");
                statusAndActions.getChildren().setAll(readyLabel);
                rootPane.setRight(statusAndActions);

                if (item.equals(currentUsername)) {
                    kickButton.setOnAction(null);
                    rootPane.setCenter(null);
                    nameLabel.setText(item);
                } else {
                    if (isHost) {
                        kickButton.toFront();
                        kickButton.setOnAction(e ->{
                                String usernameToKick = getItem();
                                    if (usernameToKick != null && !usernameToKick.equals(currentUsername)) {

                                        System.out.println("Kickbutton clickt for: " + usernameToKick);
                                        client.getConn().kickPlayerFromLobby(usernameToKick);
                                    }
                        }); // Setzt die OnAction für den Kick-Button, um den Spieler zu kicken weil man den Player name hier kennt.

                        statusAndActions.getChildren().setAll(buttonBox, readyLabel);
                        rootPane.setCenter(null);
                    } else {
                        kickButton.setOnAction(null);
                        rootPane.setCenter(null);
                    }
                    nameLabel.setText(item);
                }

                setGraphic(rootPane);
            }
        });

        for (User u : users) {
            playerListView.getItems().add(u.getUsername());
        }


        /**
         * @TODO: Implementiere die Logik um die Lobby-Informationen zu aktualisieren(LobbyMode und LobbyType) und die Buttons entsprechend zu setzen.
         * Die Buttons sollten den aktuellen Status der Lobby widerspiegeln und es dem Host ermöglichen, die Einstellungen zu ändern.
         * Die Buttons sollten nur durch den Server aktualisiert werden, wenn der Host die Einstellungen ändert.
         * Der gelbe Rand um die Buttons sollte nur durch Server aktualisiert werden, wenn der Host die Einstellungen ändert.
         * NICHT DURCH DEN CLIENT!
         * Somit weiß der Client immer, dass die Einstellungen vom Server richtig übernommen wurden.
         */
        Integer lobbyMode = lobby.mode(); // 0 = UNO Classic, 1 = UNO Does
        Integer lobbyType = lobby.type(); // 0 = open, 1 = on_request, 2 = only_code
    }


    /** @TODO
     * Sendet eine Anfrage an den Server, um die Lobby-Konfiguration zu ändern.
     *
     * @param lobbyMode
     * @param lobbyType
     */
    private void updateLobbyConfig(Integer lobbyMode, Integer lobbyType) {
        client.getConn().sendMessage(new ChangeLobbyConfigRequest(lobbyMode, lobbyType));
    }


    /**
     * Wird aufgerufen, wenn der Server eine Anfrage eines Spielers sendet, der die Lobby betreten möchte.
     * Der Lobby-Leiter kann dann entscheiden, ob er den Spieler akzeptiert oder ablehnt.
     *
     * @param msg
     */
    public void userRequestsJoinLobbyResponse(UserRequestsJoinLobbyResponse msg) {
        String username = msg.user().getUsername();

        /** @TODO: Implementiere hier die Logic um den LobbyLeiter zu benachrichtigen, dass ein Spieler die Lobby betreten möchte.
         * Der LobbyLeiter kann dann entscheiden, ob er den Spieler akzeptiert oder ablehnt.
         * Hier ein Beispiel wie man die Nachricht an den Server sendet, dass der Spieler akzeptiert wurde
         *
         * !!!!!!!!!!!!ACHTUNG!!!!!!!!!!!!!
         * Wenn der LobbyLeiter innerhalb 15 Sekunden keine Entscheidung trifft, wird der Spieler automatisch abgelehnt.
         * Das Popup sollte also nur ca. 14 Sekunden angezeigt werden, damit der LobbyLeiter genug Zeit hat zu reagieren.
         * Implementiere also eine Timer, der nach 14 Sekunden das Popup schließt.
         * Es wird Serverseitig automatisch abgelehnt, wenn der LobbyLeiter keine Entscheidung trifft.
         */

        client.sendUserRequestsJoinLobbyRequest(msg, true);
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
     * Kopiert den Lobby-Code in die Zwischenablage und zeigt eine
     * Bestätigung im lobbyCodeLabel an.
     *
     * <p>Der ursprüngliche Text wird nach 2 Sekunden wiederhergestellt.</p>
     */
    private void copyLobbyCodeToClipboard() {
        if (!copyToClipboardEnabled) {
            return;
        }else {
            copyToClipboardEnabled = false;
            String fullText = lobbyCodeLabel.getText();
            String code = fullText.replace("Lobby Code: ", "").trim();

            StringSelection stringSelection = new StringSelection(code);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

            String originalText = lobbyCodeLabel.getText();
            lobbyCodeLabel.setText("Code in der Zwischenablage gespeichert");

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), event -> {
                        lobbyCodeLabel.setText(originalText);
                        copyToClipboardEnabled = true;
                    })
            );
            timeline.play();
        }

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
     * Schaltet den eigenen Ready-Status um und sendet ihn an den Server.
     * Der Server startet das Spiel, sobald alle Spieler bereit sind.
     *
     * @param actionEvent das ausgelöste ActionEvent
     */
    public void readyButtonClicked(ActionEvent actionEvent) {
        if (lobby == null || lobby.users() == null || lobby.users().size() < 2) {
            return;
        }

        if (currentPlayerReady) {
            userNotReady();
        } else {
            userReady();
        }
    }

    private void userReady(){
        currentPlayerReady = true;
        playerListView.refresh();
        // Notify the server that the user is ready
        client.getConn().sendReadyRequest(new UserReadyRequest(true, client.getConn().getUser().getUsername()));
    }

    private void userNotReady(){
        currentPlayerReady = false;
        playerListView.refresh();
        // Notify the server that the user is not ready
        client.getConn().sendReadyRequest(new UserReadyRequest(false, client.getConn().getUser().getUsername()));
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
        Stage thisStage = (Stage) readyButton.getScene().getWindow();

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
        } catch (Exception ignored) {
        }
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
        showLobbyOverview();
    }

    public void kickedFromLobby() {
        Platform.runLater(() -> {
            try {
                intentionalClose = true;
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource("/htl/steyr/uno/kickPopup.fxml"))
                );
                KickPopupController popupController = new KickPopupController();
                loader.setController(popupController);
                StackPane popup = loader.load();
                popupController.setOnReturn(() -> {
                    kickPopupOverlay.getChildren().clear();
                    kickPopupOverlay.setVisible(false);
                    kickPopupOverlay.setManaged(false);
                    try {
                        showLobbyOverview();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not return to the lobby overview after being kicked", e);
                    }
                });
                kickPopupOverlay.getChildren().setAll(popup);
                kickPopupOverlay.setManaged(true);
                kickPopupOverlay.setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException("Could not load the kick popup", e);
            }
        });
    }

    private void showLobbyOverview() throws IOException {
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

    // Schaltet die Spielvarianten um
    @FXML
    private void handleGameVariant(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Entferne die Klasse bei beiden
        unoClassicButton.getStyleClass().remove("selectedOption");
        unoDosButton.getStyleClass().remove("selectedOption");

        // Füge sie dem angeklickten hinzu
        clickedButton.getStyleClass().add("selectedOption");
    }

    @FXML
    private void handleLobbyType(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Entferne die Klasse bei allen drei
        openLobbyButton.getStyleClass().remove("selectedOption");
        onRequestLobbyButton.getStyleClass().remove("selectedOption");
        codeOnlyButton.getStyleClass().remove("selectedOption");

        // Füge sie dem angeklickten hinzu
        clickedButton.getStyleClass().add("selectedOption");
    }
}