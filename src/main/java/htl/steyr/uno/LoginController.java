package htl.steyr.uno;

import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML public Button anmeldeButton;
    @FXML public Label errorLable;
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

    private Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = new Client(this);
        try {
            client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Platform.runLater(() -> {
            loginPane.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });
    }


    @FXML
    private void onCreateAccountButtonClicked(ActionEvent actionEvent) {
        showCreateAcc.setVisible(false);
        showNewAccScreen.setVisible(true);
        welcomeBackPasswd.clear();
        welcomeBackUserName.clear();
    }

    @FXML
    private void backToLoginButtonPressed() {
        backToLogin();
    }

    private void backToLogin() {
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
        String firstName = newAccFirstName.getText();
        String lastName = newAccLastName.getText();

        /*
        @TODO: Implement account creation logic here, such as validating the input and saving the new user to the database.
          Only if all fields are filled. If the account creation is successful, transition back to the login screen. Otherwise, show an error message.
         */

        if (password.isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
        } else if (firstName.isEmpty()) {
            System.out.println("Error: First name cannot be empty.");
        } else if (lastName.isEmpty()) {
            System.out.println("Error: Last name cannot be empty.");
        } else if (!(username.matches("[a-z]+"))) {
            System.out.println("Error: Username must only contain lowercase letters.");
        } else if (firstName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            System.out.println("Error: First name cannot contain numbers or special characters.");
        } else if (lastName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            System.out.println("Error: Last name cannot contain numbers or special characters.");
        } else {
            client.createAccount(username, lastName, firstName, password);
        }
    }

    public void onLoginButtonClicked(ActionEvent actionEvent) throws IOException {
        String username = welcomeBackUserName.getText();
        String password = welcomeBackPasswd.getText();

        /*
        @TODO: Implement login logic here, such as validating the username and password against the database.
          Only if username and password are not empty. If the login is successful, transition to the lobby screen. Otherwise, show an error message.
         */

        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
        } else if (password.isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
        } else {
            client.logIn(username, password);
        }
    }

    public void createAccountSuccess(CreateAccountSuccessResponse msg) {
        System.out.println("Account created successfully in LoginController:" + msg.getUser().getUsername());
        Platform.runLater(this::backToLogin);
    }

    public void logInSuccess(User user) {
        System.out.println("Logged in successfully in LoginController: " + user.getUsername());

        Platform.runLater(() -> {
            try {
                switchScene();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void logInFailed(LoginFailedResponse msg) {
        System.out.println("Login failed. Please try again.");
    }


    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    public void switchScene() throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) anmeldeButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));

        LobbyController controller = new LobbyController(client);
        loader.setController(controller);

        Scene scene = new Scene(loader.load());

        stage.setTitle("LobbyErstellen");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        onSceneClose();
        thisStage.close();
    }

    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }


}