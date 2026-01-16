package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.SavingsAccount;
import model.Transaction;
import org.isep.project_work.MainApp;
import service.SavingsService;
import service.UserService;

import java.time.format.DateTimeFormatter;

public class SavingsController {

    @FXML private ListView<SavingsAccount> savingsList;
    @FXML private Label balanceLabel;
    @FXML private Label interestLabel;
    @FXML private ListView<Transaction> transactionList;

    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField noteField;
    @FXML private Label messageLabel;
    @FXML private ListView<Transaction> transactionsList;

    private final SavingsService savingsService = new SavingsService();

    private final ObservableList<SavingsAccount> savingsItems = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup for SplitPane UI
        if (savingsList != null) {
            savingsList.setItems(savingsItems);
            savingsList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(SavingsAccount item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText(item.getName() + " • Balance: " + money(item.getBalance()));
                }
            });

            savingsList.getSelectionModel().selectedItemProperty().addListener((obs, oldAcc, newAcc) -> {
                refreshAccountDetails(newAcc);
            });
        }

        if (transactionList != null) {
            transactionList.setItems(transactionItems);
            transactionList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Transaction item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    String date = item.getTimestamp() == null
                            ? ""
                            : item.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    setText(date + " • " + item.getType() + " • " + money(item.getQuantity()));
                }
            });
        }

        if (typeBox != null) {
            typeBox.getItems().setAll("Deposit", "Withdraw");
            typeBox.getSelectionModel().selectFirst();
        }

        if (transactionsList != null) {
            transactionsList.setItems(transactionItems);
        }

        loadAccountsIntoUI();
    }

    @FXML
    private void handleCreateAccount() {
        if (!requireLogin()) return;

        TextInputDialog nameDialog = new TextInputDialog("Main Savings");
        nameDialog.setTitle("New Savings Account");
        nameDialog.setHeaderText("Create a savings account");
        nameDialog.setContentText("Account name:");

        String name = nameDialog.showAndWait().orElse("").trim();
        if (name.isEmpty()) {
            showError("Savings", "Please enter an account name.");
            return;
        }

        double initialBalance = askForDouble("Initial Balance", "Enter starting balance:", 0.0);
        if (Double.isNaN(initialBalance)) return;

        double interestRate = askForDouble("Interest Rate", "Enter interest rate (%):", 0.0);
        if (Double.isNaN(interestRate)) return;

        savingsService.createSavingsAccount(name, initialBalance, interestRate);
        UserService.save();

        loadAccountsIntoUI();
        showInfo("Done", "Savings account created.");
    }

    @FXML
    private void handleDeposit() {
        SavingsAccount acc = getSelectedAccount();
        if (acc == null) return;

        double amount = askForDouble("Deposit", "How much do you want to deposit?", 0.0);
        if (Double.isNaN(amount)) return;

        if (amount <= 0) {
            showError("Deposit", "Amount must be greater than 0.");
            return;
        }

        savingsService.deposit(acc, amount);
        UserService.save();

        loadAccountsIntoUI();
        selectAccount(acc);
    }

    @FXML
    private void handleWithdraw() {
        SavingsAccount acc = getSelectedAccount();
        if (acc == null) return;

        double amount = askForDouble("Withdraw", "How much do you want to withdraw?", 0.0);
        if (Double.isNaN(amount)) return;

        if (amount <= 0) {
            showError("Withdraw", "Amount must be greater than 0.");
            return;
        }

        try {
            savingsService.withdraw(acc, amount);
            UserService.save();

            loadAccountsIntoUI();
            selectAccount(acc);
        } catch (IllegalArgumentException ex) {
            showError("Withdraw", ex.getMessage());
        }
    }

    @FXML
    private void addTransaction() {
        // This supports old UI where you had amount + type dropdown
        SavingsAccount acc = getSelectedAccountOrFirst();
        if (acc == null) return;

        Double amount = parseDouble(amountField == null ? "" : amountField.getText());
        if (amount == null || amount <= 0) {
            showError("Savings", "Enter a valid amount.");
            return;
        }

        String type = typeBox == null ? "Deposit" : String.valueOf(typeBox.getValue());
        try {
            if ("Withdraw".equalsIgnoreCase(type)) {
                savingsService.withdraw(acc, amount);
            } else {
                savingsService.deposit(acc, amount);
            }

            // Optional note
            if (noteField != null && acc.getTransactions() != null && !acc.getTransactions().isEmpty()) {
                String note = noteField.getText() == null ? "" : noteField.getText().trim();
                if (!note.isBlank()) {
                    acc.getTransactions().get(acc.getTransactions().size() - 1).setNotes(note);
                }
            }

            UserService.save();
            if (amountField != null) amountField.clear();
            if (noteField != null) noteField.clear();

            loadAccountsIntoUI();
            selectAccount(acc);

        } catch (IllegalArgumentException ex) {
            showError("Savings", ex.getMessage());
        }
    }

    @FXML
    private void goToDashboard() {
    }


    private void loadAccountsIntoUI() {
        if (UserService.getCurrentUser() == null) {
            savingsItems.clear();
            refreshAccountDetails(null);
            return;
        }

        var list = UserService.getCurrentUser().getSavingsAccounts();
        savingsItems.setAll(list == null ? java.util.List.of() : list);

        if (!savingsItems.isEmpty() && savingsList != null && savingsList.getSelectionModel().getSelectedItem() == null) {
            savingsList.getSelectionModel().selectFirst();
        }

        refreshAccountDetails(savingsList == null ? null : savingsList.getSelectionModel().getSelectedItem());
    }

    private void refreshAccountDetails(SavingsAccount account) {
        if (account == null) {
            if (balanceLabel != null) balanceLabel.setText("$0.00");
            if (interestLabel != null) interestLabel.setText("0.00%");
            transactionItems.clear();
            return;
        }

        if (balanceLabel != null) balanceLabel.setText(money(account.getBalance()));
        if (interestLabel != null) interestLabel.setText(String.format("%.2f%%", account.getInterestRate()));

        transactionItems.setAll(account.getTransactions() == null ? java.util.List.of() : account.getTransactions());
    }

    private SavingsAccount getSelectedAccount() {
        if (!requireLogin()) return null;

        SavingsAccount selected = savingsList == null ? null : savingsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Savings", "Please select a savings account first.");
        }
        return selected;
    }

    private SavingsAccount getSelectedAccountOrFirst() {
        if (!requireLogin()) return null;

        SavingsAccount selected = savingsList == null ? null : savingsList.getSelectionModel().getSelectedItem();
        if (selected != null) return selected;

        var user = UserService.getCurrentUser();
        if (user.getSavingsAccounts() == null || user.getSavingsAccounts().isEmpty()) {
            showError("Savings", "Create a savings account first.");
            return null;
        }
        return user.getSavingsAccounts().get(0);
    }

    private void selectAccount(SavingsAccount acc) {
        if (savingsList == null || acc == null) return;
        savingsList.getSelectionModel().select(acc);
    }

    private boolean requireLogin() {
        if (UserService.getCurrentUser() != null) return true;
        showError("Not logged in", "Please login again.");
        return false;
    }

    private static String money(double v) {
        return String.format("$%.2f", v);
    }

    private static Double parseDouble(String raw) {
        if (raw == null) return null;
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) return null;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double askForDouble(String title, String msg, double defaultValue) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(msg);

        String raw = dialog.showAndWait().orElse("").trim();
        if (raw.isEmpty()) return Double.NaN;

        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            showError("Invalid number", "Please enter a valid number.");
            return Double.NaN;
        }
    }

    private static void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private static void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
