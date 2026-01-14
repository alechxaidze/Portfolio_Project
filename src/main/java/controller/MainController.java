package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.isep.project_work.MainApp;
import service.UserService;

public class MainController {

    @FXML
    private Label userLabel;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab portfoliosTab;
    @FXML
    private Tab savingsTab;
    @FXML
    private Tab analysisTab;

    @FXML
    private PortfolioController portfolioController;
    @FXML
    private ChartController chartsController;

    @FXML
    public void initialize() {
        if (userLabel != null) {
            userLabel.setText("Welcome, " + UserService.getCurrentUserName());
        }
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == analysisTab && chartsController != null) {
                    // Refresh charts when Analysis tab is selected
                    chartsController.handleRefresh();
                }
            });
        }
    }

    @FXML
    private void handleLogout() {
        UserService.logout();
        MainApp.showLoginScreen();
    }
}