package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.isep.project_work.MainApp;
import service.UserService;

public class MainController {

    @FXML private Label userLabel;

    @FXML private TabPane mainTabPane;
    @FXML private Tab portfoliosTab;
    @FXML private Tab savingsTab;
    @FXML private Tab analysisTab;

    @FXML private PortfolioController portfolioViewController;
    @FXML private SavingsController savingsViewController;
    @FXML private ChartController chartsViewController;

    @FXML
    public void initialize() {
        if (userLabel != null) {
            String name = UserService.getCurrentUserName();
            userLabel.setText(name == null || name.isBlank() ? "Welcome" : "Welcome, " + name);
        }

        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == analysisTab && chartsViewController != null) {
                    chartsViewController.handleRefresh();
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
