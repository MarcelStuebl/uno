package htl.steyr.uno;

import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LobbyController implements Initializable {

    @FXML private Button createPartyButton;
    @FXML private Button joinPartyButton;
    @FXML private TextField partyCodeField;

    private Client client;
    private LobbyInfoResponse lobby;


    public LobbyController(Client client) {
        this.client = client;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            createPartyButton.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });

        getClient().setLobbyController(this);
    }


    private void onSceneClose() {
        if (getClient().getConn() != null) {
            getClient().getConn().close();
        }
    }


    /**
     * @param actionEvent Generats a code für a new game
     */
    @FXML
    private void onCreatePartyButtonClicked(ActionEvent actionEvent) {
        System.out.println(client.getConn().getUser());
        client.createLobby();
    }

    public void createOrJoinPartySuccess(LobbyInfoResponse lobby) {
        this.lobby = lobby;

        Platform.runLater(() -> {
            try {
                switchToLobbyWait();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * @param actionEvent Joins a new game with all players that are in the party
     */
    @FXML
    private void onJoinButtonClicked(ActionEvent actionEvent) throws IOException {
        int lobbyId = Integer.parseInt(partyCodeField.getText());
        if (lobbyId <= 10000 || lobbyId >= 99999) {
            System.out.println("Invalid lobby ID");
            /*
             * @todo show error message in UI
             */
        } else {
            client.joinLobby(lobbyId);
        }
    }

    public void lobbyNotFound() {
        System.out.println("Invalid lobby ID. Please try again.");
    }


    private void switchToLobbyWait() throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) joinPartyButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobbyWait.fxml"));

        LobbyWaitController controller = new LobbyWaitController(client, lobby);
        loader.setController(controller);

        Scene scene = new Scene(loader.load());

        stage.setTitle("WarteLobby");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }

    /**
     * @param actionEvent Logs the acc out / goes back to the login screen
     */
    @FXML
    private void onLogoutButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) createPartyButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("UNO-AnmeldeBildschirm");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
