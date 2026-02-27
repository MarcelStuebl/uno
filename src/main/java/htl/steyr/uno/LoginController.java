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
    @FXML public Label errorLabel;
    @FXML public Label errorLabelCreateAcc;
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
        Thread clientThread = new Thread(() -> {
            client.start();
        });
        clientThread.start();

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
        errorLabel.setVisible(false);
        errorLabelCreateAcc.setVisible(false);
    }

    @FXML
    private void backToLogin() {
        showNewAccScreen.setVisible(false);
        showCreateAcc.setVisible(true);
        newAccPassword.clear();
        newAccUserName.clear();
        newAccLastName.clear();
        newAccFirstName.clear();
        errorLabel.setVisible(false);
        errorLabelCreateAcc.setVisible(false);
    }

    public void onCreateNewAccountButtonClicked(ActionEvent actionEvent) {
        String username = newAccUserName.getText();
        String password = newAccPassword.getText();
        String firstName = newAccFirstName.getText();
        String lastName = newAccLastName.getText();

        if (password.isEmpty()) {
            errorLabelCreateAcc.setText("Password cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (firstName.isEmpty()) {
            errorLabelCreateAcc.setText("First name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (lastName.isEmpty()) {
            errorLabelCreateAcc.setText("Last name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (!(username.matches("[a-z]+"))) {
            errorLabelCreateAcc.setText("Username must only contain lowercase letters!");
            errorLabelCreateAcc.setVisible(true);
        } else if (firstName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            errorLabelCreateAcc.setText("First name cannot contain numbers or special characters!");
            errorLabelCreateAcc.setVisible(true);
        } else if (lastName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            errorLabelCreateAcc.setText("Last name cannot contain numbers or special characters!");
            errorLabelCreateAcc.setVisible(true);
        } else {
            client.createAccount(username, lastName, firstName, password);
            errorLabelCreateAcc.setVisible(false);
        }
    }

    public void onLoginButtonClicked(ActionEvent actionEvent) throws IOException {
        String username = welcomeBackUserName.getText();
        String password = welcomeBackPasswd.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Username cannot be empty!");
            errorLabel.setVisible(true);
        } else if (password.isEmpty()) {
            errorLabel.setText("Password cannot be empty!");
            errorLabel.setVisible(true);
        } else {
            client.logIn(username, password);
            errorLabel.setVisible(false);
        }
    }

    public void createAccountSuccess(CreateAccountSuccessResponse msg) {
        Platform.runLater(this::backToLogin);
    }

    public void logInSuccess(User user) {
        Platform.runLater(() -> {
            try {
                switchScene();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void logInFailed(LoginFailedResponse msg) {
        Platform.runLater(() ->{
            errorLabel.setText("Username or Password wrong!");
            errorLabel.setVisible(true);
        });
    }


    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    public void switchScene() throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) errorLabel.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));

        LobbyController controller = new LobbyController(client);
        loader.setController(controller);

        Scene scene = new Scene(loader.load());

        stage.setTitle("LobbyErstellen");
        stage.setScene(scene);
        stage.setMaximized(true);
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