package htl.steyr.uno.Lobby;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LobbyWaitController implements Initializable {

    public Label lobbyCodeLabel;
    public Label player1Label;
    public Label player2Label;
    public Button playButton;
    public Label AccNameDisplayLabel;
    public Button leaveLobbyButton;
    public ListView<String> playerListView = new ListView<>();


    private final Client client;
    private LobbyInfoResponse lobby;

    /**
     * @param client
     * @param lobby
     */
    public LobbyWaitController(Client client, LobbyInfoResponse lobby) {
        this.client = client;
        this.lobby = lobby;
    }

    /**
     * Calls the onSceneClose funktion to close the Client when the Scene is closed
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            playButton.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });

        String lobbyCode = lobby.getLobbyId().toString();
        lobbyCodeLabel.setText("Lobby Code: " + lobbyCode);

        // Display current user's account name
        AccNameDisplayLabel.setText("Account: " + client.getConn().getUser().getUsername());

        updateLobbyInfo();

        System.out.println("Es geht:\n" + client.getConn().getUser());
        System.out.println("Lobby:\n" + lobby);
    }


    public void updateLobbyInfo() {
        System.out.println("Lobby Info Updated: " + lobby);
        String currentUsername = client.getConn().getUser().getUsername();

        playerListView.getItems().clear();

        if (lobby.getUsers().getFirst().getUsername().equals(currentUsername)) {
            // Current user is the host
            playButton.setVisible(true);

            // Display the host username in player1Label
            player1Label.setText(lobby.getUsers().getFirst().getUsername());
            playerListView.getItems().add(lobby.getUsers().getFirst().getUsername());

            // Display the guest username in player2Label if there are 2 players
            if (lobby.getUsers().size() > 1) {
                player2Label.setText(lobby.getUsers().getLast().getUsername());
                playerListView.getItems().add(lobby.getUsers().getLast().getUsername());
            } else {
                player2Label.setText("Wartet auf Spieler...");
                playerListView.getItems().add("Wartet auf Spieler...");
            }
        } else {
            // Current user is a guest
            playButton.setVisible(false);

            // Display the host username in player1Label
            player1Label.setText(lobby.getUsers().getFirst().getUsername());
            playerListView.getItems().add(lobby.getUsers().getFirst().getUsername());

            // Display the guest (current user) username in player2Label
            player2Label.setText(currentUsername);
            playerListView.getItems().add(currentUsername);
        }
    }

    /**
     *
     */
    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    /**
     * Changes the Scene from the WaitingLobby to Gametable when the playButton is clicked
     * @param actionEvent
     * @throws IOException
     */
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

    /**
     * Changes the Scene from the WaitingLobby to the Lobby when the leaveButton is clicked
     * @param actionEvent
     * @throws IOException
     */
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

}
