package htl.steyr.uno.Lobby;

import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.client.Client;
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

public class LobbyController implements Initializable {

    public Button createPartyButton;
    public Button joinButton;
    public Button logoutButton;
    public Label gerneratedPartyCode;

    private Client client;


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

        System.out.println("Es geht:\n" + client.getConn().getUser());
    }

    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }


    /**
     * @param actionEvent
     *
     * Generats a code für a new game
     */
    public void onCreatePartyClicked(ActionEvent actionEvent) {

    }

    /**
     * @param actionEvent
     *
     * Joins a new game with all players that are in the party
     */
    public void onJoinButtonClicked(ActionEvent actionEvent) {

    }

    /**
     * @param actionEvent
     *
     * Logs the acc out / goes back to the login screen
     */
    public void onLogoutButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
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
