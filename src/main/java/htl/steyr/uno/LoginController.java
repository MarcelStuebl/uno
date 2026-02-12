package htl.steyr.uno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {


    @FXML private Button showLogin;
    @FXML private VBox showNewAccScreen;
    @FXML private Button createAcc;
    @FXML private VBox showCreateAcc;
    @FXML private StackPane loginPane;
    @FXML private StackPane brandingPane;
    @FXML private PasswordField newAccPassword;
    @FXML private TextField newAccUserName;
    @FXML private TextField newAccLastName;
    @FXML private TextField newAccFirstName;
    @FXML private PasswordField welcomeBackPasswd;
    @FXML private TextField welcomeBackUserName;

    @FXML
    private void onCreateAccountButtonClicked(ActionEvent actionEvent) {
        showCreateAcc.setVisible(false);
        showNewAccScreen.setVisible(true);
        welcomeBackPasswd.clear();
        welcomeBackUserName.clear();
    }

    @FXML
    private void backToLogin(ActionEvent actionEvent) {
        showNewAccScreen.setVisible(false);
        showCreateAcc.setVisible(true);
        newAccPassword.clear();
        newAccUserName.clear();
        newAccLastName.clear();
        newAccFirstName.clear();
    }
}
