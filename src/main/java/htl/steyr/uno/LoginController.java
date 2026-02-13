package htl.steyr.uno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {


    @FXML
    private Button showLogin;
    @FXML
    private VBox showNewAccScreen;
    @FXML
    private Button createAcc;
    @FXML
    private VBox showCreateAcc;
    @FXML
    private StackPane loginPane;
    @FXML
    private StackPane brandingPane;
    @FXML
    private PasswordField newAccPassword;
    @FXML
    private TextField newAccUserName;
    @FXML
    private TextField newAccLastName;
    @FXML
    private TextField newAccFirstName;
    @FXML
    private PasswordField welcomeBackPasswd;
    @FXML
    private TextField welcomeBackUserName;


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

    public void onCreateNewAccountButtonClicked(ActionEvent actionEvent) {
        String username = newAccUserName.getText();
        String password = newAccPassword.getText();
        String lastName = newAccLastName.getText();
        String firstName = newAccFirstName.getText();

        /*
        @TODO: Implement account creation logic here, such as validating the input and saving the new user to the database.
          Only if all fields are filled. If the account creation is successful, transition back to the login screen. Otherwise, show an error message.
         */
    }

    public void onLoginButtonClicked(ActionEvent actionEvent) {
        String username = welcomeBackUserName.getText();
        String password = welcomeBackPasswd.getText();

        /*
        @TODO: Implement login logic here, such as validating the username and password against the database.
          Only if username and password are not empty. If the login is successful, transition to the lobby screen. Otherwise, show an error message.
         */
    }
}
