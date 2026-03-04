package htl.steyr.uno;

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
    @FXML public TextField newAccEmail;
    @FXML public TextField twoFACode;
    @FXML public VBox show2FA;
    @FXML public Label errorLabel2FA;
    @FXML public VBox showForgotPassword;
    @FXML public TextField resetPasswordEmail;
    @FXML public Label errorLabelForgotPassword;
    @FXML public VBox showResetPassword2FA;
    @FXML public TextField resetPassword2FACode;
    @FXML public Label errorLabelReset2FA;
    @FXML public VBox showNewPasswordScreen;
    @FXML public PasswordField newPassword;
    @FXML public PasswordField confirmNewPassword;
    @FXML public Label errorLabelNewPassword;
    @FXML public VBox showAccountVerificationScreen;
    @FXML public TextField verifyAccount;
    @FXML private Button showLogin;
    @FXML private VBox showNewAccScreen;
    @FXML private Button createAcc;
    @FXML private VBox showLoginScreen;
    @FXML private StackPane loginPane;
    @FXML private StackPane brandingPane;
    @FXML private PasswordField newAccPassword;
    @FXML private TextField newAccUserName;
    @FXML private TextField newAccLastName;
    @FXML private TextField newAccFirstName;
    @FXML private PasswordField welcomeBackPasswd;
    @FXML private TextField welcomeBackUserName;

    private Client client;

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;

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
        showLoginScreen.setVisible(false);
        showNewAccScreen.setVisible(true);
        welcomeBackPasswd.clear();
        welcomeBackUserName.clear();
        errorLabel.setVisible(false);
        errorLabelCreateAcc.setVisible(false);
    }

    @FXML
    private void backToLogin() {
        showNewAccScreen.setVisible(false);
        showLoginScreen.setVisible(true);
        newAccPassword.clear();
        newAccUserName.clear();
        newAccLastName.clear();
        newAccFirstName.clear();
        errorLabel.setVisible(false);
        errorLabelCreateAcc.setVisible(false);
    }

    @FXML
    private void backToLoginFrom2FA() {
        show2FA.setVisible(false);
        showLoginScreen.setVisible(true);
        twoFACode.clear();
    }

    @FXML
    private void onVerify2FA(ActionEvent actionEvent) {
        String code = twoFACode.getText();
        if (code.isEmpty()) {
            // Handle error
        } else {
            // TODO: Verify the 2FA code with the server
            twoFACode.clear();
        }
    }

    public void onCreateNewAccountButtonClicked(ActionEvent actionEvent) {
        username = newAccUserName.getText();
        password = newAccPassword.getText();
        firstName = newAccFirstName.getText();
        lastName = newAccLastName.getText();
        email = newAccEmail.getText();

        if (password.isEmpty()) {
            errorLabelCreateAcc.setText("Password cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (firstName.isEmpty()) {
            errorLabelCreateAcc.setText("First name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (lastName.isEmpty()) {
            errorLabelCreateAcc.setText("Last name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (email.isEmpty()) {
            errorLabelCreateAcc.setText("Email cannot be empty!");
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
        } else if (!(email.contains("@") && email.contains("."))) {
            errorLabelCreateAcc.setText("Email must be valid!");
            errorLabelCreateAcc.setVisible(true);
        } else {
            client.createAccount(username, lastName, firstName, email, password);
            client.getConn().checkIfUserAlreadyExists(username);
        }
    }

    public void checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse msg) {
        if (!msg.isUserAlreadyExists()) {
            System.out.println("User already exists.");
            errorLabelCreateAcc.setText("Username already exists!");
            errorLabelCreateAcc.setVisible(true);
        } else {
            System.out.println("User does not exist.");
            errorLabelCreateAcc.setVisible(false);
            showAccountVerificationScreen.setVisible(true);
            showNewAccScreen.setVisible(false);
        }
    }

    public void onVerifyNewAccount(ActionEvent actionEvent) {
        String code = verifyAccount.getText();
        if (code.isEmpty()) {
            errorLabel2FA.setText("Code cannot be empty!");
            errorLabel2FA.setVisible(true);
        } else if (code.length() != 6 || !code.matches("\\d+")) {
            errorLabel2FA.setText("Code must be 6 digits!");
            errorLabel2FA.setVisible(true);
        } else {
            client.verifyNewAccount(username, firstName, lastName, email, password, Integer.parseInt(code));
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

    @FXML
    private void onForgotPasswordClicked(ActionEvent actionEvent) {
        showLoginScreen.setVisible(false);
        showForgotPassword.setVisible(true);
        resetPasswordEmail.clear();
        errorLabelForgotPassword.setVisible(false);
    }

    @FXML
    private void backToLoginFromForgotPassword() {
        showForgotPassword.setVisible(false);
        showLoginScreen.setVisible(true);
        resetPasswordEmail.clear();
    }

    @FXML
    private void onResetPasswordClicked(ActionEvent actionEvent) {
        String email = resetPasswordEmail.getText();

        if (email.isEmpty()) {
            errorLabelForgotPassword.setText("Email cannot be empty!");
            errorLabelForgotPassword.setVisible(true);
        } else if (!(email.contains("@") && email.contains("."))) {
            errorLabelForgotPassword.setText("Email must be valid!");
            errorLabelForgotPassword.setVisible(true);
        } else {
            // TODO: Email-Verifizierung auf dem Server durchführen und 2FA Code versenden
            showForgotPassword.setVisible(false);
            showResetPassword2FA.setVisible(true);
            resetPassword2FACode.clear();
            errorLabelReset2FA.setVisible(false);
        }
    }

    @FXML
    private void onVerifyResetPassword2FA(ActionEvent actionEvent) {
        String code = resetPassword2FACode.getText();

        if (code.isEmpty()) {
            errorLabelReset2FA.setText("Code cannot be empty!");
            errorLabelReset2FA.setVisible(true);
        } else if (code.length() != 6 || !code.matches("\\d+")) {
            errorLabelReset2FA.setText("Code must be 6 digits!");
            errorLabelReset2FA.setVisible(true);
        } else {
            // TODO: 2FA Code auf dem Server überprüfen
            showResetPassword2FA.setVisible(false);
            showNewPasswordScreen.setVisible(true);
            newPassword.clear();
            confirmNewPassword.clear();
            errorLabelNewPassword.setVisible(false);
        }
    }

    @FXML
    private void onSetNewPasswordClicked(ActionEvent actionEvent) {
        String password = newPassword.getText();
        String confirmPassword = confirmNewPassword.getText();

        if (password.isEmpty()) {
            errorLabelNewPassword.setText("Password cannot be empty!");
            errorLabelNewPassword.setVisible(true);
        } else if (confirmPassword.isEmpty()) {
            errorLabelNewPassword.setText("Please confirm your password!");
            errorLabelNewPassword.setVisible(true);
        } else if (!password.equals(confirmPassword)) {
            errorLabelNewPassword.setText("Passwords do not match!");
            errorLabelNewPassword.setVisible(true);
        } else {
            // TODO: Neues Passwort auf dem Server speichern
            errorLabelNewPassword.setText("Passwort erfolgreich geändert!");
            errorLabelNewPassword.setStyle("-fx-text-fill: #90EE90;");
            errorLabelNewPassword.setVisible(true);

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::backToLoginFromNewPassword);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void backToLoginFromReset2FA() {
        showResetPassword2FA.setVisible(false);
        showForgotPassword.setVisible(true);
        resetPassword2FACode.clear();
    }

    @FXML
    private void backToLoginFromNewPassword() {
        showNewPasswordScreen.setVisible(false);
        showLoginScreen.setVisible(true);
        newPassword.clear();
        confirmNewPassword.clear();
        resetPasswordEmail.clear();
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
        controller.getClient().setLobbyController(controller);

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