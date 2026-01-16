package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.isep.project_work.MainApp;
import service.UserService;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        clearMessage();

        String name = safeText(nameField);
        String email = safeText(emailField);
        String password = passwordField != null ? passwordField.getText() : "";
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        if (password.length() < 3) {
            showError("Password must be at least 3 characters.");
            return;
        }

        boolean success = UserService.registerUser(name, email, password);

        if (success) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setHeaderText("Welcome, " + name + "!");
            ok.setContentText("Your account is created. You are now logged in.");
            ok.showAndWait();

            MainApp.showDashboard();
        } else {
            showError("An account with this email already exists.");
        }
    }

    @FXML
    private void handleBack() {
        MainApp.showLoginScreen();
    }

    private static String safeText(TextField field) {
        return field == null ? "" : field.getText().trim();
    }

    private void clearMessage() {
        if (messageLabel != null) messageLabel.setText("");
    }

    private void showError(String msg) {
        if (messageLabel != null) messageLabel.setText(msg);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Register");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
