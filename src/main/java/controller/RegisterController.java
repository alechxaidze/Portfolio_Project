package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.isep.project_work.MainApp;
import service.UserService;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField != null ? emailField.getText().trim() : username;
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        if (password.length() < 3) {
            showAlert("Error", "Password must be at least 3 characters.");
            return;
        }

        String userEmail = (email != null && !email.isEmpty()) ? email : username;

        boolean success = UserService.registerUser(username, userEmail, password);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Welcome to Portfolio Manager!");
            alert.setContentText("Your account has been created. You are now logged in.");
            alert.showAndWait();
            MainApp.showDashboard();
        } else {
            showAlert("Registration Failed", "An account with this email already exists.");
        }
    }

    @FXML
    private void handleBack() {
        MainApp.showLoginScreen();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}