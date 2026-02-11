package htl.steyr.uno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private VBox hideLogin;
    @FXML private Button createAcc;

    @FXML
    private void onCreateButtonClicked(ActionEvent actionEvent) {
        hideLogin.setVisible(false);
    }
}
