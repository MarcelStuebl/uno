package htl.steyr.uno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {


    public Button testButton;

    public void onHelloButtonClick(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) testButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("UNO-Lobby");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        thisStage.close();
    }
}
