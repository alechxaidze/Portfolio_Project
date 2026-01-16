package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.Asset;
import model.AssetType;
import model.Portfolio;
import model.User;
import org.isep.project_work.MainApp;
import service.ApiService;
import service.UserService;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.util.Optional;

public class PortfolioController {
    @FXML
    private TableView<Asset> assetsTable;
    @FXML
    private TableColumn<Asset, String> colType;
    @FXML
    private TableColumn<Asset, String> colSymbol;
    @FXML
    private TableColumn<Asset, String> colQty;
    @FXML
    private TableColumn<Asset, String> colBuy;
    @FXML
    private TableColumn<Asset, String> colNow;
    @FXML
    private TableColumn<Asset, String> colValue;

    @FXML
    private Label messageLabel;
    @FXML
    private ComboBox<Portfolio> portfolioSelector;
    @FXML
    private Label portfolioNameLabel;
    @FXML
    private Label portfolioDescLabel;

    // Form controls (needed for handleAddAsset)
    @FXML
    private ComboBox<AssetType> assetTypeBox;
    @FXML
    private TextField symbolField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField buyPriceField;

    private final service.ImportService importService = new service.ImportService();
    private final ApiService apiService = new ApiService();
    private final ObservableList<Asset> tableItems = FXCollections.observableArrayList();

    private Portfolio portfolio;

    @FXML
    public void initialize() {
        if (assetTypeBox != null) {
            assetTypeBox.setItems(FXCollections.observableArrayList(AssetType.values()));
            assetTypeBox.getSelectionModel().select(AssetType.STOCK);
        }

        if (assetsTable != null) {
            assetsTable.setItems(tableItems);
        }

        setupColumns();
        setupPortfolioSelector();
        setMessage("");
    }

    private void setupPortfolioSelector() {
        if (portfolioSelector != null) {
            User user = UserService.getCurrentUser();
            if (user != null) {
                portfolioSelector.setItems(FXCollections.observableArrayList(user.getPortfolios()));

                // Select the first one by default if none selected
                if (portfolio == null && !user.getPortfolios().isEmpty()) {
                    portfolio = user.getPortfolios().get(0);
                }

                portfolioSelector.setValue(portfolio);

                portfolioSelector.setOnAction(e -> {
                    portfolio = portfolioSelector.getValue();
                    refreshPortfolioView();
                });
            }
        }
        refreshPortfolioView();
    }

    private void refreshPortfolioView() {
        if (portfolio != null) {
            portfolioNameLabel.setText(portfolio.getName());
            portfolioDescLabel.setText(portfolio.getDescription() != null ? portfolio.getDescription() : "");
        }
        refreshTable();
    }

    private void setupColumns() {
        if (colType != null) {
            colType.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getType())));
        }
        if (colSymbol != null) {
            colSymbol.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getSymbol())));
        }
        if (colQty != null) {
            colQty.setCellValueFactory(c -> new SimpleStringProperty(format2(c.getValue().getQuantity())));
        }
        if (colBuy != null) {
            colBuy.setCellValueFactory(c -> new SimpleStringProperty(format2(getBuyPriceSafe(c.getValue()))));
        }
        if (colNow != null) {
            colNow.setCellValueFactory(c -> new SimpleStringProperty(format2(c.getValue().getCurrentPrice())));
        }
        if (colValue != null) {
            colValue.setCellValueFactory(c -> {
                Asset a = c.getValue();
                double value = a.getQuantity() * a.getCurrentPrice();
                return new SimpleStringProperty(format2(value));
            });
        }
    }

    // ---------- Actions ----------

    @FXML
    private void handleAddAsset() {
        setMessage("");

        if (!ensurePortfolio())
            return;

        String symbol = text(symbolField).toUpperCase();
        AssetType type = (assetTypeBox == null) ? null : assetTypeBox.getValue();

        Double qty = parseNumber(text(quantityField));
        Double buy = parseNumber(text(buyPriceField));

        if (symbol.isBlank() || type == null || qty == null || buy == null) {
            showError("Input Error", "Please fill all fields correctly.");
            return;
        }
        if (qty <= 0) {
            showError("Input Error", "Quantity must be greater than 0.");
            return;
        }

        Asset asset = new Asset(symbol, symbol, type, qty, buy);

        try {
            double live = apiService.getCurrentPrice(symbol);
            asset.setCurrentPrice(live > 0 ? live : buy);
        } catch (Exception e) {
            asset.setCurrentPrice(buy);
        }

        // Log transaction event
        if (portfolio != null) {
            String title = "Added " + qty + " " + symbol;
            String desc = String.format("Manually added %s units of %s at %s each.", qty, symbol, format2(buy));
            portfolio.addEvent(
                    new model.Event(title, desc, java.time.LocalDate.now(), model.EventType.OTHER, portfolio.getId()));
        }

        portfolio.addAsset(asset);
        UserService.save();

        clearInputs();
        refreshTable();
        setMessage("Asset added to " + portfolio.getName());
    }

    @FXML
    private void handleCreatePortfolio() {
        TextInputDialog dialog = new TextInputDialog("New Portfolio");
        dialog.setTitle("Create Portfolio");
        dialog.setHeaderText("Create a new portfolio to track different assets.");
        dialog.setContentText("Portfolio Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Portfolio p = new Portfolio(name);
            User user = UserService.getCurrentUser();
            if (user != null) {
                user.addPortfolio(p);
                UserService.save();
                setupPortfolioSelector();
                portfolioSelector.setValue(p);
                setMessage("Portfolio '" + name + "' created.");
            }
        });
    }

    @FXML
    private void handleClonePortfolio() {
        if (portfolio == null)
            return;

        TextInputDialog dialog = new TextInputDialog(portfolio.getName() + " (Copy)");
        dialog.setTitle("Clone Portfolio");
        dialog.setHeaderText("Clone '" + portfolio.getName() + "' with all its assets.");
        dialog.setContentText("New Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            Portfolio cloned = portfolio.clone(newName);
            User user = UserService.getCurrentUser();
            if (user != null) {
                user.addPortfolio(cloned);
                UserService.save();
                setupPortfolioSelector();
                portfolioSelector.setValue(cloned);
                setMessage("Portfolio cloned as '" + newName + "'");
            }
        });
    }

    @FXML
    private void importCSV() {
        setMessage("");

        if (!ensurePortfolio())
            return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = chooser.showOpenDialog(assetsTable.getScene().getWindow());
        if (file == null)
            return;

        try {
            importService.importCSV(file, portfolio);

            // Log import event
            String title = "CSV Data Import";
            String desc = "Successfully imported transaction data from: " + file.getName();
            portfolio.addEvent(
                    new model.Event(title, desc, java.time.LocalDate.now(), model.EventType.OTHER, portfolio.getId()));

            UserService.save();
            refreshTable();
            setMessage("Import successful ✅");
        } catch (Exception e) {
            showError("CSV Import Failed", e.getMessage());
        }
    }

    @FXML
    private void refreshPrices() {
        setMessage("");

        if (!ensurePortfolio())
            return;

        if (portfolio.getAssets() == null || portfolio.getAssets().isEmpty()) {
            setMessage("No assets to refresh.");
            return;
        }

        for (Asset a : portfolio.getAssets()) {
            try {
                double live = apiService.getCurrentPrice(a.getSymbol());
                if (live > 0)
                    a.setCurrentPrice(live);
            } catch (Exception ignored) {
            }
        }

        UserService.save();
        assetsTable.refresh();
        setMessage("Prices updated ✅");
    }

    @FXML
    private void removeSelected() {
        setMessage("");

        if (!ensurePortfolio())
            return;

        Asset selected = assetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Remove", "Select an asset first.");
            return;
        }

        portfolio.removeAsset(selected);
        UserService.save();
        refreshTable();
        setMessage("Removed ");
    }

    @FXML
    private void goToDashboard() {
        MainApp.showDashboard();
    }

    /**
     * Refreshes the table with assets from the currently selected portfolio.
     */
    private void refreshTable() {
        tableItems.clear();
        if (portfolio != null && portfolio.getAssets() != null) {
            tableItems.addAll(portfolio.getAssets());
        }
        if (assetsTable != null)
            assetsTable.refresh();
    }

    private double getBuyPriceSafe(Asset asset) {
        return asset != null ? asset.getAvgPurchasePrice() : 0.0;
    }

    private void clearInputs() {
        if (symbolField != null)
            symbolField.clear();
        if (quantityField != null)
            quantityField.clear();
        if (buyPriceField != null)
            buyPriceField.clear();
        if (assetTypeBox != null)
            assetTypeBox.getSelectionModel().selectFirst();
    }

    private void setMessage(String msg) {
        if (messageLabel != null)
            messageLabel.setText(msg == null ? "" : msg);
    }

    private static String text(TextField f) {
        return (f == null || f.getText() == null) ? "" : f.getText().trim();
    }

    private static String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String format2(double v) {
        return String.format("%.2f", v);
    }

    private static Double parseNumber(String raw) {
        if (raw == null)
            return null;
        String s = raw.trim().replace("\"", "").replace(",", ".");
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean ensurePortfolio() {
        if (portfolio == null) {
            showError("No Portfolio", "Please create or select a portfolio first.");
            return false;
        }
        return true;
    }
}
