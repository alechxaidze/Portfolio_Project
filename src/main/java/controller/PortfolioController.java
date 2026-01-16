package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Asset;
import model.AssetType;
import model.Portfolio;
import model.User;
import org.isep.project_work.MainApp;
import service.ApiService;
import service.UserService;

import java.util.Optional;

public class PortfolioController {

    @FXML private Label portfolioValueLabel;
    @FXML private TableView<Asset> assetsTable;

    @FXML private TextField symbolField;
    @FXML private ComboBox<AssetType> typeBox;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Asset> tableItems = FXCollections.observableArrayList();

    private Portfolio portfolio;

    @FXML
    public void initialize() {
        if (typeBox != null) {
            typeBox.setItems(FXCollections.observableArrayList(AssetType.values()));
            typeBox.getSelectionModel().select(AssetType.STOCK);
        }

        if (assetsTable != null) {
            assetsTable.setItems(tableItems);
        }

        loadOrCreatePortfolio();
        refreshTableAndTotals();
    }

    @FXML
    private void handleAddAsset() {
        if (portfolio == null) {
            showError("No portfolio", "Could not find or create a portfolio.");
            return;
        }

        String symbol = safeUpper(symbolField);
        AssetType type = typeBox != null ? typeBox.getValue() : null;
        Double quantity = parseDouble(quantityField, "Quantity");
        Double price = parseDouble(priceField, "Price per unit");

        if (symbol.isEmpty() || type == null || quantity == null || price == null) return;

        if (quantity <= 0 || price < 0) {
            showError("Invalid values", "Quantity must be > 0 and price must be >= 0.");
            return;
        }

        Asset asset = new Asset(symbol, symbol, type, quantity, price);

        try {
            double latest = apiService.getCurrentPrice(symbol);
            if (latest > 0) asset.setCurrentPrice(latest);
        } catch (Exception ignored) {}

        portfolio.addAsset(asset);
        UserService.save();

        clearInputs();
        refreshTableAndTotals();
    }

    @FXML
    private void refreshPrices() {
        if (portfolio == null || portfolio.getAssets().isEmpty()) return;

        for (Asset asset : portfolio.getAssets()) {
            try {
                double latest = apiService.getCurrentPrice(asset.getSymbol());
                if (latest > 0) asset.setCurrentPrice(latest);
            } catch (Exception ignored) {}
        }

        UserService.save();
        refreshTableAndTotals();
    }

    @FXML
    private void removeSelected() {
        if (portfolio == null) return;

        Asset selected = assetsTable != null ? assetsTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showError("Nothing selected", "Select an asset in the table first.");
            return;
        }

        portfolio.removeAsset(selected);
        UserService.save();
        refreshTableAndTotals();
    }

    @FXML
    private void goToDashboard() {
        MainApp.showDashboard();
    }
    private void loadOrCreatePortfolio() {
        User user = UserService.getCurrentUser();
        if (user == null) {
            portfolio = null;
            return;
        }

        Optional<Portfolio> first = user.getPortfolios().stream().findFirst();
        portfolio = first.orElseGet(() -> {
            Portfolio created = new Portfolio("My Portfolio");
            user.addPortfolio(created);
            UserService.save();
            return created;
        });
    }

    private void refreshTableAndTotals() {
        tableItems.clear();
        if (portfolio != null) tableItems.addAll(portfolio.getAssets());

        if (portfolioValueLabel != null) {
            double total = portfolio != null ? portfolio.getTotalValue() : 0.0;
            portfolioValueLabel.setText(String.format("$%.2f", total));
        }
    }

    private void clearInputs() {
        if (symbolField != null) symbolField.clear();
        if (quantityField != null) quantityField.clear();
        if (priceField != null) priceField.clear();
        if (typeBox != null) typeBox.getSelectionModel().select(AssetType.STOCK);
    }

    private static String safeUpper(TextField field) {
        if (field == null || field.getText() == null) return "";
        return field.getText().trim().toUpperCase();
    }

    private Double parseDouble(TextField field, String label) {
        if (field == null) return null;
        String raw = field.getText() == null ? "" : field.getText().trim();
        if (raw.isEmpty()) {
            showError("Missing value", "Please enter " + label.toLowerCase() + ".");
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            showError("Invalid number", label + " must be a valid number.");
            return null;
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
