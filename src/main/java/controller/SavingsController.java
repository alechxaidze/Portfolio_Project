package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.SavingsAccount;
import model.Transaction;
import org.isep.project_work.MainApp;
import service.SavingsService;
import service.UserService;

import java.util.Optional;

public class SavingsController {

    @FXML
    private ListView<SavingsAccount> savingsList;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label interestLabel;
    @FXML
    private ListView<Transaction> transactionList;

    private SavingsService savingsService;

    @FXML
    public void initialize() {
        savingsService = MainApp.getSavingsService();

        if (UserService.getCurrentUser() != null) {
            savingsList.setItems(FXCollections.observableArrayList(UserService.getCurrentUser().getSavingsAccounts()));
        }

        savingsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SavingsAccount item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - $" + String.format("%.2f", item.getBalance()));
                }
            }
        });

        savingsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDetails(newVal);
            }
        });
    }

    private void updateDetails(SavingsAccount account) {
        balanceLabel.setText(String.format("$%.2f", account.getBalance()));
        interestLabel.setText(String.format("%.2f%%", account.getInterestRate()));
        transactionList.setItems(FXCollections.observableArrayList(account.getTransactions()));
        transactionList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getType() + ": $" + item.getQuantity() + " on " + item.getTimestamp().toLocalDate());
                }
            }
        });
    }

    @FXML
    private void handleCreateAccount() {
        Dialog<SavingsAccount> dialog = new Dialog<>();
        dialog.setTitle("New Savings Account");
        dialog.setHeaderText("Create a new Savings Account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Account Name");
        TextField balanceField = new TextField();
        balanceField.setPromptText("Initial Balance");
        TextField rateField = new TextField();
        rateField.setPromptText("Interest Rate (%)");

        content.getChildren().addAll(new Label("Name:"), nameField, new Label("Balance:"), balanceField,
                new Label("Interest Rate:"), rateField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    double bal = Double.parseDouble(balanceField.getText());
                    double rate = Double.parseDouble(rateField.getText());
                    return new SavingsAccount(name, bal, rate);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<SavingsAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            savingsService.createSavingsAccount(account.getName(), account.getBalance(), account.getInterestRate());
            refreshList();
        });
    }

    @FXML
    private void handleDeposit() {
        SavingsAccount selected = savingsList.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit to " + selected.getName());
        dialog.setContentText("Amount:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                savingsService.deposit(selected, amount);
                refreshList(); // Update balance display in list
                updateDetails(selected);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid Amount");
            }
        });
    }

    @FXML
    private void handleWithdraw() {
        SavingsAccount selected = savingsList.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw from " + selected.getName());
        dialog.setContentText("Amount:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                savingsService.withdraw(selected, amount);
                refreshList();
                updateDetails(selected);
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void refreshList() {
        if (UserService.getCurrentUser() != null) {
            savingsList.setItems(FXCollections.observableArrayList(UserService.getCurrentUser().getSavingsAccounts()));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}