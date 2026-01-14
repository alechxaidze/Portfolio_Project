package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.*;
import org.isep.project_work.MainApp;
import service.AnalysisService;
import service.UserService;

import java.time.LocalDate;
import java.util.*;

public class ChartController {

    @FXML
    private ComboBox<Portfolio> portfolioSelector;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private CheckBox showEventsCheckbox;
    @FXML
    private CheckBox overlayPortfoliosCheckbox;
    @FXML
    private PieChart allocationChart;
    @FXML
    private LineChart<String, Number> historyChart;
    @FXML
    private ListView<Event> eventList;
    @FXML
    private Label summaryLabel;
    @FXML
    private VBox analysisBox;

    private AnalysisService analysisService;

    @FXML
    public void initialize() {
        analysisService = new AnalysisService(MainApp.getApiService());

        // Setup date pickers with defaults
        if (endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now());
        }
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now().minusMonths(6));
        }

        // Setup portfolio selector with proper display
        if (portfolioSelector != null) {
            // Set up cell factory for dropdown items
            portfolioSelector.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Portfolio item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
            portfolioSelector.setConverter(new StringConverter<>() {
                @Override
                public String toString(Portfolio portfolio) {
                    return portfolio != null ? portfolio.getName() : "";
                }

                @Override
                public Portfolio fromString(String string) {
                    return null; // Not needed for non-editable ComboBox
                }
            });

            portfolioSelector.setOnAction(e -> updateCharts());
        }
        if (startDatePicker != null) {
            startDatePicker.setOnAction(e -> updateCharts());
        }
        if (endDatePicker != null) {
            endDatePicker.setOnAction(e -> updateCharts());
        }

        if (showEventsCheckbox != null) {
            showEventsCheckbox.setOnAction(e -> updateCharts());
        }
        if (overlayPortfoliosCheckbox != null) {
            overlayPortfoliosCheckbox.setOnAction(e -> updateCharts());
        }
        refreshPortfolios();
    }

    @FXML
    public void handleRefresh() {
        refreshPortfolios();
        updateCharts();
    }

    private void refreshPortfolios() {
        User user = UserService.getCurrentUser();
        if (user != null && portfolioSelector != null) {
            Portfolio previousSelection = portfolioSelector.getValue();

            portfolioSelector.setItems(FXCollections.observableArrayList(user.getPortfolios()));

            if (previousSelection != null) {
                for (Portfolio p : user.getPortfolios()) {
                    if (p.getId().equals(previousSelection.getId())) {
                        portfolioSelector.setValue(p);
                        break;
                    }
                }
            }

            // If no selection, select first
            if (portfolioSelector.getValue() == null && !user.getPortfolios().isEmpty()) {
                portfolioSelector.setValue(user.getPortfolios().get(0));
            }

            // Update charts with current selection
            updateCharts();
        }
    }

    public void updateCharts() {
        Portfolio portfolio = portfolioSelector != null ? portfolioSelector.getValue() : null;
        if (portfolio == null) {
            clearCharts();
            return;
        }

        updateAllocationChart(portfolio);
        updateHistoryChart(portfolio);
        updateEventList(portfolio);
        updateSummary(portfolio);
    }

    private void updateAllocationChart(Portfolio portfolio) {
        if (allocationChart == null)
            return;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        double totalValue = portfolio.getTotalValue();

        if (totalValue > 0) {
            for (Asset asset : portfolio.getAssets()) {
                double value = asset.getValue();
                if (value > 0) {
                    String label = String.format("%s (%.1f%%)",
                            asset.getSymbol(),
                            (value / totalValue) * 100);
                    pieData.add(new PieChart.Data(label, value));
                }
            }
        }

        allocationChart.setData(pieData);
        allocationChart.setTitle("Portfolio Allocation - " + portfolio.getName());
    }

    private void updateHistoryChart(Portfolio portfolio) {
        if (historyChart == null)
            return;

        historyChart.getData().clear();

        LocalDate start = startDatePicker != null ? startDatePicker.getValue() : LocalDate.now().minusMonths(6);
        LocalDate end = endDatePicker != null ? endDatePicker.getValue() : LocalDate.now();

        if (start == null)
            start = LocalDate.now().minusMonths(6);
        if (end == null)
            end = LocalDate.now();
        boolean overlay = overlayPortfoliosCheckbox != null && overlayPortfoliosCheckbox.isSelected();

        if (overlay) {
            User user = UserService.getCurrentUser();
            if (user != null) {
                for (Portfolio p : user.getPortfolios()) {
                    addPortfolioSeries(p, start, end);
                }
            }
        } else {
            addPortfolioSeries(portfolio, start, end);
        }
    }

    private void addPortfolioSeries(Portfolio portfolio, LocalDate start, LocalDate end) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(portfolio.getName());
        List<PortfolioSnapshot> snapshots = portfolio.getSnapshots();

        if (snapshots == null || snapshots.isEmpty()) {
            double currentValue = portfolio.getTotalValue();
            if (currentValue <= 0) {
                currentValue = 1000; // Default value for display
            }

            Random rand = new Random(portfolio.getId().hashCode());

            LocalDate date = start;
            while (!date.isAfter(end)) {
                double variation = 0.90 + (rand.nextDouble() * 0.20);
                double value = currentValue * variation;
                series.getData().add(new XYChart.Data<>(date.toString(), value));
                date = date.plusDays(7);
            }
            series.getData().add(new XYChart.Data<>(end.toString(), currentValue));
        } else {
            for (PortfolioSnapshot snap : snapshots) {
                if (!snap.getDate().isBefore(start) && !snap.getDate().isAfter(end)) {
                    series.getData().add(new XYChart.Data<>(snap.getDate().toString(), snap.getTotalValue()));
                }
            }
        }

        historyChart.getData().add(series);
    }

    private void updateEventList(Portfolio portfolio) {
        if (eventList == null)
            return;

        User user = UserService.getCurrentUser();
        if (user == null)
            return;

        List<Event> events = new ArrayList<>();
        if (user.getGlobalEvents() != null) {
            events.addAll(user.getGlobalEvents());
        }
        if (portfolio.getEvents() != null) {
            events.addAll(portfolio.getEvents());
        }
        LocalDate start = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate end = endDatePicker != null ? endDatePicker.getValue() : null;

        if (start != null && end != null) {
            events.removeIf(e -> e.getDate() == null || e.getDate().isBefore(start) || e.getDate().isAfter(end));
        }

        events.sort(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
        eventList.setItems(FXCollections.observableArrayList(events));
    }

    private void updateSummary(Portfolio portfolio) {
        if (summaryLabel == null)
            return;

        try {
            Map<String, Object> summary = analysisService.getPortfolioSummary(portfolio);
            model.Currency currency = portfolio.getReferenceCurrency();
            if (currency == null)
                currency = model.Currency.USD;

            String text = String.format(
                    "Value: %s%.2f | P/L: %s%.2f (%.1f%%) | Tax Est: %s%.2f",
                    currency.getSymbol(),
                    (Double) summary.getOrDefault("totalValue", 0.0),
                    currency.getSymbol(),
                    (Double) summary.getOrDefault("profitLoss", 0.0),
                    (Double) summary.getOrDefault("profitLossPercent", 0.0),
                    currency.getSymbol(),
                    (Double) summary.getOrDefault("estimatedTax", 0.0));

            summaryLabel.setText(text);
        } catch (Exception e) {
            summaryLabel.setText("Value: $0.00 | P/L: $0.00 (0.0%)");
        }
    }

    private void clearCharts() {
        if (allocationChart != null) {
            allocationChart.setData(FXCollections.observableArrayList());
            allocationChart.setTitle("No portfolio selected");
        }
        if (historyChart != null) {
            historyChart.getData().clear();
        }
        if (eventList != null) {
            eventList.setItems(FXCollections.observableArrayList());
        }
        if (summaryLabel != null) {
            summaryLabel.setText("Select a portfolio from the dropdown above");
        }
    }

    @FXML
    private void handleAddEvent() {
        Portfolio portfolio = portfolioSelector != null ? portfolioSelector.getValue() : null;

        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Add Event");
        dialog.setHeaderText("Add a new market event");

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField titleField = new TextField();
        titleField.setPromptText("Event Title");
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<EventType> typeBox = new ComboBox<>(FXCollections.observableArrayList(EventType.values()));
        typeBox.setValue(EventType.OTHER);
        CheckBox globalCheck = new CheckBox("Global Event (not portfolio-specific)");

        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descField,
                new Label("Date:"), datePicker,
                new Label("Type:"), typeBox,
                globalCheck);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == addType) {
                String portfolioId = globalCheck.isSelected() ? null : (portfolio != null ? portfolio.getId() : null);
                return new Event(
                        titleField.getText(),
                        descField.getText(),
                        datePicker.getValue(),
                        typeBox.getValue(),
                        portfolioId);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(event -> {
            User user = UserService.getCurrentUser();
            if (user != null) {
                if (event.isGlobal()) {
                    user.addGlobalEvent(event);
                } else if (portfolio != null) {
                    portfolio.addEvent(event);
                }
                UserService.save();
                updateCharts();
            }
        });
    }
}
