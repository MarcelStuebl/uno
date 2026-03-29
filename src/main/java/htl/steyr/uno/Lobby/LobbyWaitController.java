package htl.steyr.uno.Lobby;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.UiStyleUtil;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
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

                //Label or displaying the Autor oft the Message
                Label nameLabel = new Label(item.sender());
                nameLabel.setStyle("-fx-text-fill: #c12424; -fx-font-size: 12;");

                Label bubble = new Label(item.text());
                bubble.setWrapText(true);
                bubble.setPadding(new Insets(8));
                bubble.setMaxWidth(320);

                if (myMessage) {
                    bubble.setStyle("-fx-background-color: #91ec4e; -fx-background-radius: 12;");
                } else {
                    bubble.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                            "-fx-border-color: #E0E0E0; -fx-border-radius: 12; -fx-background-radius: 12;");
                }

                javafx.scene.layout.VBox messageBox = new javafx.scene.layout.VBox(2, nameLabel, bubble);

                HBox row = new HBox(messageBox);
                row.setPadding(new Insets(4, 8, 4, 8));
                row.setAlignment(myMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setText(null);
                setGraphic(row);
            }
        });

        // Chat-ListView initial eingeklappt (nicht sichtbar und nimmt keinen Platz weg)
        gameChatListView.setPrefHeight(0);
        gameChatListView.setMinHeight(0);
        gameChatListView.setVisible(false);
        gameChatListView.setManaged(false);

        Timeline expand = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(gameChatListView.prefHeightProperty(), 300))
        );

        Timeline collapse = new Timeline(
                new KeyFrame(Duration.millis(200),
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

        gameChatTextField.focusedProperty().addListener((obs, oldV, focused) -> {
            if (!focused) chatExpanded.set(false);
        });

        // beim Klick aufs TextField auf-/zuklappen
        gameChatTextField.setOnMouseClicked(e -> chatExpanded.set(!chatExpanded.get()));

        updateLobbyInfo();
    }

    public void updateLobbyInfo() {
        String currentUsername = client.getConn().getUser().getUsername();

        playerListView.getItems().clear();

        if (lobby.users().getFirst().getUsername().equals(currentUsername)) {
            playButton.setVisible(true);




            playerListView.getItems().add(lobby.users().getFirst().getUsername());

            playerListView.setCellFactory(list -> new ListCell<String>() {
                private final BorderPane rootPane = new BorderPane();
                private final Label nameLabel = new Label();
                private final HBox buttonBox = new HBox(10);
                public final Button kickButton = new Button("Kick");
                public final Button muteButton = new Button("Mute");

                {

                    nameLabel.setMinWidth(180);
                    nameLabel.setMaxWidth(180);
                    nameLabel.setPrefWidth(180);

                    nameLabel.setAlignment(Pos.CENTER_LEFT);

                    buttonBox.getChildren().addAll(kickButton, muteButton);
                    buttonBox.setAlignment(Pos.CENTER);

                    rootPane.setLeft(nameLabel);
                    rootPane.setCenter(buttonBox);
                    rootPane.setPadding(new Insets(4, 8, 4, 8));

                    BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
                    BorderPane.setAlignment(buttonBox, Pos.CENTER);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        nameLabel.setText(item);
                        setGraphic(rootPane);
                    }
                }
            });





            if (lobby.users().size() > 1) {
                playerListView.getItems().add(lobby.users().getLast().getUsername());
            } else {
                playerListView.getItems().add("Wartet auf Spieler...");
            }
        } else {
            playButton.setVisible(false);

            playerListView.getItems().add(lobby.users().getFirst().getUsername());
            playerListView.getItems().add(currentUsername);
        }
    }

    public void onSendMassage(ActionEvent actionEvent) {
        String text = gameChatTextField.getText();

        if (text != null && !text.isBlank()) {
            gameChatTextField.clear();
            sendChatMessage(text.trim());
            System.out.println("Sent chat message: " + text.trim());

            if (text.startsWith("@")){
                gameChatTextField.clear();
                sendPrivateMessage(text.trim());
                System.out.println("Sent private message: " + text.trim());
            }

        }
    }

    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    public void playButtonClicked(ActionEvent actionEvent) throws IOException {
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

        stage.setScene(scene);
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

    private void sendPrivateMessage(String message){
        //client.sendPrivateMesssage(message);
    }

    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.user().getUsername();
        String text = msg.message();
        System.out.println("Received chat message from " + sender + ": " + text);

        Platform.runLater(() -> gameChatListView.getItems().add(new ChatMessage(sender, text)));
    }

    public record ChatMessage(String sender, String text) {
    }
}
