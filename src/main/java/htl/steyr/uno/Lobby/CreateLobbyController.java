package htl.steyr.uno.Lobby;

import htl.steyr.uno.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateLobbyController {

    public Button createPartyButton;
    public Button joinButton;
    public Button logoutButton;
    public Label gerneratedPartyCode;


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
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("UNO-AnmeldeBildschirm");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }

}
