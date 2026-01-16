package org.isep.project_work;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static final service.ApiService apiService = new service.ApiService();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Portfolio Tracker");
        showLoginScreen();
        primaryStage.show();
    }

    public static void showLoginScreen() {
        loadScene("/org/isep/project_work/login.fxml");
    }

    public static void showDashboard() {
        loadScene("/org/isep/project_work/dashboard.fxml");
    }

    public static void showRegisterScreen() {
        loadScene("/org/isep/project_work/register.fxml");
    }

    public static service.ApiService getApiService() {
        return apiService;
    }

    private static void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not load FXML: " + fxmlPath);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
