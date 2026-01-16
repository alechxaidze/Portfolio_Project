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
    private Tab whaleWatchTab;

    @FXML
    private PortfolioController portfolioViewController;
    @FXML
    private SavingsController savingsViewController;
    @FXML
    private ChartController chartsViewController;
    @FXML
    private WhaleWatchController whaleWatchViewController;

    @FXML
    public void initialize() {
        // Start monitoring for demonstration
        service.WhaleMonitoringService.getInstance().monitorAddress("Ethereum",
                "0x3f5CE5FBFe3E9af3971dD833D26bA9b5C936f0bE", "main-binance");
        service.WhaleMonitoringService.getInstance().monitorAddress("Bitcoin", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                "satoshi-wallet");

        if (userLabel != null) {
            String name = UserService.getCurrentUserName();
            userLabel.setText(name == null || name.isBlank() ? "Welcome" : "Welcome, " + name);
        }

        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == analysisTab && chartsViewController != null) {
                    chartsViewController.handleRefresh();
                } else if (newTab == whaleWatchTab && whaleWatchViewController != null) {
                    whaleWatchViewController.refreshAlerts();
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
