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
import java.util.ArrayList;
import java.util.List;

public class SavingsController {

    @FXML private Label balanceLabel;
    @FXML private TextField accountNameField;
    @FXML private Label accountInfoLabel;

    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField noteField;
    @FXML private Label messageLabel;
    @FXML private ListView<Transaction> transactionsList;

    private final SavingsService savingsService = new SavingsService();
    private final ObservableList<Transaction> transactionItems = FXCollections.observableArrayList();

    private SavingsAccount currentAccount; // we use one main savings account for now

    @FXML
    public void initialize() {
        if (typeBox != null) {
            typeBox.getItems().setAll("Deposit", "Withdraw");
            typeBox.getSelectionModel().selectFirst();
        }

        if (transactionsList != null) {
            transactionsList.setItems(transactionItems);
            transactionsList.setCellFactory(list -> new ListCell<>() {
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

                    String notes = (item.getNotes() == null || item.getNotes().isBlank())
                            ? ""
                            : " • " + item.getNotes().trim();

                    setText(date + " • " + item.getType() + " • " + money(item.getQuantity()) + notes);
                }
            });
        }

        loadOrCreateDefaultAccountIfExists();
        refreshView();
    }

    @FXML
    private void handleCreateAccount() {
        if (UserService.getCurrentUser() == null) {
            showError("Not logged in", "Please login again.");
            return;
        }

        var user = UserService.getCurrentUser();

        if (user.getSavingsAccounts() == null) {
            user.setSavingsAccounts(new ArrayList<>());
        }

        if (!user.getSavingsAccounts().isEmpty()) {
            showInfo("Already exists", "You already have a savings account created.");
            currentAccount = user.getSavingsAccounts().get(0);
            refreshView();
            return;
        }

        String name = (accountNameField == null) ? "" : accountNameField.getText().trim();
        if (name.isBlank()) name = "Main Savings";

        // create one account, simple student approach
        SavingsAccount created = new SavingsAccount(name, 0.0, 0.0);
        user.getSavingsAccounts().add(created);

        UserService.save();

        currentAccount = created;
        if (accountInfoLabel != null) accountInfoLabel.setText("Account created: " + name);
        refreshView();
    }

    @FXML
    private void addTransaction() {
        clearMessage();

        if (!ensureAccountReady()) return;

        Double amount = parseDouble(amountField == null ? "" : amountField.getText());
        if (amount == null || amount <= 0) {
            setMessage("Enter a valid amount.", true);
            return;
        }

        String type = typeBox == null ? "Deposit" : String.valueOf(typeBox.getValue());
        String note = (noteField == null) ? "" : noteField.getText().trim();

        try {
            if ("Withdraw".equalsIgnoreCase(type)) {
                savingsService.withdraw(currentAccount, amount);
            } else {
                savingsService.deposit(currentAccount, amount);
            }

            // attach note to latest transaction (optional)
            if (!note.isBlank() && currentAccount.getTransactions() != null && !currentAccount.getTransactions().isEmpty()) {
                Transaction last = currentAccount.getTransactions().get(currentAccount.getTransactions().size() - 1);
                last.setNotes(note);
            }

            UserService.save();

            if (amountField != null) amountField.clear();
            if (noteField != null) noteField.clear();

            setMessage("Transaction saved.", false);
            refreshView();

        } catch (IllegalArgumentException ex) {
            setMessage(ex.getMessage(), true);
        }
    }

    @FXML
    private void goToDashboard() {
        MainApp.showDashboard();
    }

    private boolean ensureAccountReady() {
        if (UserService.getCurrentUser() == null) {
            showError("Not logged in", "Please login again.");
            return false;
        }

        if (currentAccount != null) return true;

        var user = UserService.getCurrentUser();
        List<SavingsAccount> list = user.getSavingsAccounts();
        if (list != null && !list.isEmpty()) {
            currentAccount = list.get(0);
            return true;
        }

        setMessage("Create a savings account first (left card).", true);
        return false;
    }

    private void loadOrCreateDefaultAccountIfExists() {
        var user = UserService.getCurrentUser();
        if (user == null) return;

        if (user.getSavingsAccounts() != null && !user.getSavingsAccounts().isEmpty()) {
            currentAccount = user.getSavingsAccounts().get(0);
        }
    }

    private void refreshView() {
        if (currentAccount == null) {
            if (balanceLabel != null) balanceLabel.setText("€0.00");
            transactionItems.clear();
            return;
        }

        if (balanceLabel != null) {
            balanceLabel.setText(moneyEUR(currentAccount.getBalance()));
        }

        transactionItems.setAll(currentAccount.getTransactions() == null ? List.of() : currentAccount.getTransactions());
    }

    private void setMessage(String text, boolean error) {
        if (messageLabel != null) messageLabel.setText(text == null ? "" : text);
        if (error) showError("Savings", text);
    }

    private void clearMessage() {
        if (messageLabel != null) messageLabel.setText("");
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

    private static String money(double v) {
        return String.format("%.2f", v);
    }

    private static String moneyEUR(double v) {
        return String.format("€%.2f", v);
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
