package htl.steyr.uno.Lobby;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class LobbyWaitController {


    public Label player1Label;
    public Label player2Label;
    public Button playButton;
    public Button leaveLobbyButton;
    public Label AccNameDisplayLabel;

    public void playButtonClicked(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Stage thisStage = (Stage) playButton.getScene().getWindow();
        GameTable table = new GameTable();
        table.open(stage);
        thisStage.close();
    }

    public void leaveLobbyButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) leaveLobbyButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("../../../../../resources/htl/steyr/uno/lobby.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("LobbyErstellen");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        thisStage.close();
    }
}
