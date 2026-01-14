package org.isep.project_work;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.*;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static PortfolioService portfolioService;
    private static ApiService apiService;
    private static SavingsService savingsService;
    private static AnalysisService analysisService;
    private static EventService eventService;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        apiService = new ApiService();
        portfolioService = new PortfolioService();
        savingsService = new SavingsService();
        analysisService = new AnalysisService(apiService);
        eventService = new EventService();

        showLoginScreen();
    }

    @Override
    public void stop() {
        UserService.save();
    }

    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/org/isep/project_work/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(MainApp.class.getResource("/org/isep/project_work/styles.css").toExternalForm());
            primaryStage.setTitle("Portfolio Manager - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/org/isep/project_work/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(MainApp.class.getResource("/org/isep/project_work/styles.css").toExternalForm());
            primaryStage.setTitle("Portfolio Manager - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/org/isep/project_work/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 550);
            scene.getStylesheets().add(MainApp.class.getResource("/org/isep/project_work/styles.css").toExternalForm());
            primaryStage.setTitle("Portfolio Manager - Register");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static PortfolioService getPortfolioService() {
        return portfolioService;
    }

    public static ApiService getApiService() {
        return apiService;
    }

    public static SavingsService getSavingsService() {
        return savingsService;
    }

    public static AnalysisService getAnalysisService() {
        return analysisService;
    }

    public static EventService getEventService() {
        return eventService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}