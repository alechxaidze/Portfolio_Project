package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.isep.project_work.MainApp;
import service.UserService;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String email = safe(emailField);
        String password = safe(passwordField);

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both email and password.");
            return;
        }

        boolean ok = UserService.loginUser(email, password);
        if (ok) {
            clearMessage();
            MainApp.showDashboard();
        } else {
            showMessage("Invalid email or password.");
        }
    }

    @FXML
    private void goToRegister() {
        MainApp.showRegisterScreen();
    }

    private String safe(TextField tf) {
        return tf == null ? "" : tf.getText().trim();
    }

    private String safe(PasswordField pf) {
        return pf == null ? "" : pf.getText().trim();
    }

    private void showMessage(String msg) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
        }
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
}
