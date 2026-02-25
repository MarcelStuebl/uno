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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LobbyWaitController implements Initializable {


    public Label player1Label;
    public Label player2Label;
    public Button playButton;
    public Button leaveLobbyButton;
    public Label AccNameDisplayLabel;

    private final Client client;
    private LobbyInfoResponse lobby;

    public LobbyWaitController(Client client, LobbyInfoResponse lobby) {
        this.client = client;
        this.lobby = lobby;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            playButton.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });

        System.out.println("Es geht:\n" + client.getConn().getUser());
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

        GameTable controller = new GameTable();
        loader.setController(controller);

        Scene scene = new Scene(loader.load());

        stage.setTitle("UNO");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }

    public void leaveLobbyButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) leaveLobbyButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));

        LobbyController controller = new LobbyController(client);
        loader.setController(controller);

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
    }
}
