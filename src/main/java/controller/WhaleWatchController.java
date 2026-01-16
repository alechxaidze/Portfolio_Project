package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import service.WhaleMonitoringService;
import service.WhaleMonitoringService.WhaleAlert;

import java.time.format.DateTimeFormatter;

public class WhaleWatchController {

    @FXML
    private ListView<WhaleAlert> alertsListView;

    private final ObservableList<WhaleAlert> alertsList = FXCollections.observableArrayList();
    private final WhaleMonitoringService whaleService = WhaleMonitoringService.getInstance();

    @FXML
    public void initialize() {
        if (alertsListView != null) {
            alertsListView.setItems(alertsList);
            alertsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(WhaleAlert alert, boolean empty) {
                    super.updateItem(alert, empty);
                    if (empty || alert == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String time = alert.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        setText(String.format("[%s] 🐋 %s: %.2f %s ($%.0f)",
                                time, alert.blockchain, alert.amount, alert.token, alert.usdValue));

                        // Style based on value
                        if (alert.usdValue > 100000) {
                            setStyle("-fx-text-fill: #ff5555; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #55ff55;");
                        }
                    }
                }
            });
        }
        refreshAlerts();
    }

    @FXML
    private void handleRefresh() {
        refreshAlerts();
    }

    public void refreshAlerts() {
        alertsList.setAll(whaleService.getActiveAlerts());
    }
}
