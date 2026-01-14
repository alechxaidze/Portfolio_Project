package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.*;
import org.isep.project_work.MainApp;
import service.ApiService;
import service.ImportService;
import service.PortfolioService;
import service.UserService;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class PortfolioController {

    @FXML
    private ListView<Portfolio> portfolioList;
    @FXML
    private TableView<Asset> assetTable;
    @FXML
    private TableColumn<Asset, String> symbolColumn;
    @FXML
    private TableColumn<Asset, String> nameColumn;
    @FXML
    private TableColumn<Asset, String> typeColumn;
    @FXML
    private TableColumn<Asset, Double> quantityColumn;
    @FXML
    private TableColumn<Asset, Double> priceColumn;
    @FXML
    private TableColumn<Asset, Double> valueColumn;
    @FXML
    private TableColumn<Asset, Double> plColumn;
    @FXML
    private Label totalValueLabel;
    @FXML
    private Label profitLossLabel;
    @FXML
    private Label portfolioDescLabel;
    @FXML
    private ComboBox<Currency> currencySelector;

    private PortfolioService portfolioService;
    private ApiService apiService;
    private ImportService importService;

    @FXML
    public void initialize() {
        portfolioService = MainApp.getPortfolioService();
        apiService = MainApp.getApiService();
        importService = new ImportService();

        if (currencySelector != null) {
            currencySelector.setItems(FXCollections.observableArrayList(Currency.values()));
            currencySelector.setValue(Currency.USD);
            currencySelector.setOnAction(e -> updateAssetTable(portfolioList.getSelectionModel().getSelectedItem()));
        }refreshPortfolioList();

        portfolioList.setCellFactory(param -> new ListCell<>() {
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

        portfolioList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateAssetTable(newVal);
            }
        });

        symbolColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSymbol()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        quantityColumn.setCellValueFactory(
                cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(
                cellData -> new SimpleDoubleProperty(cellData.getValue().getCurrentPrice()).asObject());
        valueColumn
                .setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValue()).asObject());

        if (plColumn != null) {
            plColumn.setCellValueFactory(
                    cellData -> new SimpleDoubleProperty(cellData.getValue().getProfitLoss()).asObject());
            plColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%.2f", item));
                        setStyle(item >= 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #f44336;");
                    }
                }
            });
        }
    }

    private void updateAssetTable(Portfolio portfolio) {
        if (portfolio == null)
            return;

        for (Asset asset : portfolio.getAssets()) {
            double currentPrice = apiService.getCurrentPrice(asset.getSymbol());
            asset.setCurrentPrice(currentPrice);
        }

        assetTable.setItems(FXCollections.observableArrayList(portfolio.getAssets()));

        Currency currency = currencySelector != null ? currencySelector.getValue() : Currency.USD;
        double total = portfolioService.calculateTotalValue(portfolio);
        double pl = portfolio.getProfitLoss();
        double plPercent = portfolio.getProfitLossPercent();

        totalValueLabel.setText(String.format("Total Value: %s%.2f", currency.getSymbol(), total));

        if (profitLossLabel != null) {
            profitLossLabel.setText(String.format("P/L: %s%.2f (%.1f%%)", currency.getSymbol(), pl, plPercent));
            profitLossLabel.setStyle(pl >= 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #f44336;");
        }

        if (portfolioDescLabel != null && portfolio.getDescription() != null) {
            portfolioDescLabel.setText(portfolio.getDescription());
        }
    }

    @FXML
    private void handleImportCSV() {
        Portfolio selectedPortfolio = portfolioList.getSelectionModel().getSelectedItem();
        if (selectedPortfolio == null) {
            showAlert("No Portfolio Selected", "Please select a portfolio to import transactions into.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Coinbase CSV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showOpenDialog(portfolioList.getScene().getWindow());
        if (file != null) {
            try {
                List<Transaction> transactions = importService.importCoinbaseCSV(file, selectedPortfolio);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Import Complete");
                success.setHeaderText("Successfully imported transactions!");
                success.setContentText(String.format(
                        "Imported %d transactions into portfolio '%s'.\n\nAssets have been added or updated accordingly.",
                        transactions.size(),
                        selectedPortfolio.getName()));
                success.showAndWait();
                updateAssetTable(selectedPortfolio);

            } catch (Exception e) {
                showAlert("Import Failed", "Failed to import CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreatePortfolio() {
        Dialog<Portfolio> dialog = new Dialog<>();
        dialog.setTitle("New Portfolio");
        dialog.setHeaderText("Create a new Portfolio");

        ButtonType createType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Portfolio Name");
        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == createType && !nameField.getText().isEmpty()) {
                return new Portfolio(nameField.getText(), descField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(portfolio -> {
            User user = UserService.getCurrentUser();
            if (user != null) {
                user.addPortfolio(portfolio);
                UserService.save();
                refreshPortfolioList();
            }
        });
    }

    @FXML
    private void handleClonePortfolio() {
        Portfolio selected = portfolioList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Portfolio Selected", "Please select a portfolio to clone.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getName() + " (Copy)");
        dialog.setTitle("Clone Portfolio");
        dialog.setHeaderText("Clone: " + selected.getName());
        dialog.setContentText("New portfolio name:");

        dialog.showAndWait().ifPresent(name -> {
            Portfolio cloned = selected.clone(name);
            User user = UserService.getCurrentUser();
            if (user != null) {
                user.addPortfolio(cloned);
                UserService.save();
                refreshPortfolioList();
            }
        });
    }

    @FXML
    private void handleDeletePortfolio() {
        Portfolio selected = portfolioList.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Portfolio");
        confirm.setHeaderText("Delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                User user = UserService.getCurrentUser();
                if (user != null) {
                    user.removePortfolio(selected);
                    UserService.save();
                    refreshPortfolioList();
                }
            }
        });
    }

    @FXML
    private void handleAddAsset() {
        Portfolio selectedPortfolio = portfolioList.getSelectionModel().getSelectedItem();
        if (selectedPortfolio == null) {
            showAlert("No Portfolio Selected", "Please select a portfolio first.");
            return;
        }

        Dialog<Asset> dialog = new Dialog<>();
        dialog.setTitle("Add Asset");
        dialog.setHeaderText("Add a new Asset");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField symbolField = new TextField();
        symbolField.setPromptText("Symbol (e.g. AAPL, BTC)");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        ComboBox<AssetType> typeBox = new ComboBox<>(FXCollections.observableArrayList(AssetType.values()));
        typeBox.setValue(AssetType.STOCK);
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Purchase Price");

        content.getChildren().addAll(
                new Label("Symbol:"), symbolField,
                new Label("Name:"), nameField,
                new Label("Type:"), typeBox,
                new Label("Quantity:"), quantityField,
                new Label("Avg Price:"), priceField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String symbol = symbolField.getText().toUpperCase();
                    String name = nameField.getText();
                    AssetType type = typeBox.getValue();
                    double qty = Double.parseDouble(quantityField.getText());
                    double price = Double.parseDouble(priceField.getText());
                    return new Asset(symbol, name, type, qty, price);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Asset> result = dialog.showAndWait();
        result.ifPresent(asset -> {
            double currentPrice = apiService.getCurrentPrice(asset.getSymbol());
            asset.setCurrentPrice(currentPrice);
            portfolioService.addAssetToPortfolio(selectedPortfolio, asset, currentPrice);
            UserService.save();
            updateAssetTable(selectedPortfolio);
        });
    }

    @FXML
    private void handleRemoveAsset() {
        Portfolio selectedPortfolio = portfolioList.getSelectionModel().getSelectedItem();
        Asset selectedAsset = assetTable.getSelectionModel().getSelectedItem();
        if (selectedPortfolio != null && selectedAsset != null) {
            double currentPrice = selectedAsset.getCurrentPrice();
            portfolioService.removeAssetFromPortfolio(selectedPortfolio, selectedAsset, currentPrice);
            UserService.save();
            updateAssetTable(selectedPortfolio);
        }
    }

    private void refreshPortfolioList() {
        User user = UserService.getCurrentUser();
        if (user != null) {
            portfolioList.setItems(FXCollections.observableArrayList(user.getPortfolios()));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}