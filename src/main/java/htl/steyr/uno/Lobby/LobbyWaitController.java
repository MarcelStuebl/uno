package htl.steyr.uno.Lobby;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
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
import javafx.scene.layout.HBox;
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

    private final BooleanProperty chatExpanded = new SimpleBooleanProperty(false);

    private final Client client;
    private LobbyInfoResponse lobby;

    public LobbyWaitController(Client client, LobbyInfoResponse lobby) {
        this.client = client;
        this.lobby = lobby;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> playButton.getScene().getWindow().setOnCloseRequest(event -> onSceneClose()));

        String lobbyCode = lobby.getLobbyId().toString();
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

                boolean mine = currentUsername.equals(item.sender());

                Label nameLabel = new Label(item.sender());
                nameLabel.setStyle("-fx-text-fill: #c12424; -fx-font-size: 12;");

                Label bubble = new Label(item.text());
                bubble.setWrapText(true);
                bubble.setPadding(new Insets(8));
                bubble.setMaxWidth(320);

                if (mine) {
                    bubble.setStyle("-fx-background-color: #91ec4e; -fx-background-radius: 12;");
                } else {
                    bubble.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                            "-fx-border-color: #E0E0E0; -fx-border-radius: 12; -fx-background-radius: 12;");
                }

                javafx.scene.layout.VBox messageBox = new javafx.scene.layout.VBox(2, nameLabel, bubble);

                HBox row = new HBox(messageBox);
                row.setPadding(new Insets(4, 8, 4, 8));
                row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setText(null);
                setGraphic(row);
            }
        });

        // Chat-ListView initial "eingeklappt" (nicht sichtbar und nimmt keinen Platz weg)
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

        System.out.println("Es geht:\n" + client.getConn().getUser());
        System.out.println("Lobby:\n" + lobby);
    }

    public void updateLobbyInfo() {
        System.out.println("Lobby Info Updated: " + lobby);
        String currentUsername = client.getConn().getUser().getUsername();

        playerListView.getItems().clear();

        if (lobby.getUsers().getFirst().getUsername().equals(currentUsername)) {
            playButton.setVisible(true);

            playerListView.getItems().add(lobby.getUsers().getFirst().getUsername());

            if (lobby.getUsers().size() > 1) {
                playerListView.getItems().add(lobby.getUsers().getLast().getUsername());
            } else {
                playerListView.getItems().add("Wartet auf Spieler...");
            }
        } else {
            playButton.setVisible(false);

            playerListView.getItems().add(lobby.getUsers().getFirst().getUsername());
            playerListView.getItems().add(currentUsername);
        }
    }

    public void onSendMassage(ActionEvent actionEvent) {
        String text = gameChatTextField.getText();

        if (text != null && !text.isBlank()) {
            gameChatTextField.clear();
            sendChatMessage(text.trim());
            System.out.println("Sent chat message: " + text.trim());
        }
    }

    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    public void playButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) playButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("gameTable.fxml"));

        GameTable controller = new GameTable(client);
        loader.setController(controller);

        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.show();
        thisStage.close();
    }

    public void leaveLobbyButtonClicked(ActionEvent actionEvent) throws IOException {
        client.getConn().leaveLobby();

        Stage stage = new Stage();
        Stage thisStage = (Stage) leaveLobbyButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));

        LobbyController controller = new LobbyController(client);
        loader.setController(controller);
        client.setLobbyController(controller);
        client.setLobbyWaitController(null);

        Scene scene = new Scene(loader.load());

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

    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.getUser().getUsername();
        String text = msg.getMessage();
        System.out.println("Received chat message from " + sender + ": " + text);

        Platform.runLater(() -> gameChatListView.getItems().add(new ChatMessage(sender, text)));
    }

    public record ChatMessage(String sender, String text) {
    }
}