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

public class LobbyWaitController implements Initializable {

    public Label lobbyCodeLabel;
    public Button playButton;
    public Label AccNameDisplayLabel;
    public Button leaveLobbyButton;
    public ListView<String> playerListView = new ListView<>();
    public ListView<ChatMessage> gameChatListView = new ListView<>();
    public TextField gameChatTextField;


    private boolean intentionalClose = false;
    private final BooleanProperty chatExpanded = new SimpleBooleanProperty(false);

    private final Client client;
    private LobbyInfoResponse lobby;

    public LobbyWaitController(Client client, LobbyInfoResponse lobby) {
        this.client = client;
        this.lobby = lobby;
    }

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

                //buttonBox.getChildren().addAll(kickButton, muteButton);
                //buttonBox.setAlignment(Pos.CENTER);

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

                profileImageView.setFitWidth(70); //44
                profileImageView.setFitHeight(70); //44
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

    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    public void playButtonClicked(ActionEvent actionEvent) throws IOException {
        if (lobby == null || lobby.users() == null || lobby.users().size() < 2) {
            return;
        }
        client.getConn().startGame();
    }

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

    public void startGameResponse(StartGameResponse msg) {
        Platform.runLater(() -> {
            try {
                startGame(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

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

    public LobbyInfoResponse getLobby() {
        return lobby;
    }

    public void setLobby(LobbyInfoResponse lobby) {
        this.lobby = lobby;
        Platform.runLater(this::updateLobbyInfo);
    }

    private void sendChatMessage(String message) {
        client.sendChatMessage(message);
    }

    private void sendPrivateMessage(String message) {
        // client.sendPrivateMesssage(message);
    }

    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.user().getUsername();
        String text = msg.message();

        Platform.runLater(() -> {
            gameChatListView.getItems().add(new ChatMessage(sender, text));

            // Chat aufklappen falls noch zu
            if (!chatExpanded.get()) {
                chatExpanded.set(true);
            }

            // Zur letzten Nachricht scrollen
            int lastIndex = gameChatListView.getItems().size() - 1;
            if (lastIndex >= 0) {
                gameChatListView.scrollTo(lastIndex);
            }
        });
    }

    public record ChatMessage(String sender, String text) {
    }
}