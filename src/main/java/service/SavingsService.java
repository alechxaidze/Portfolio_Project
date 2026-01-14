package service;

import model.SavingsAccount;
import model.Transaction;
import model.User;

import java.time.LocalDateTime;

public class SavingsService {

    public SavingsService() {
    }

    public void createSavingsAccount(String name, double initialBalance, double interestRate) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            SavingsAccount account = new SavingsAccount(name, initialBalance, interestRate);
            user.getSavingsAccounts().add(account);
            UserService.save();
        }
    }

    public void deposit(SavingsAccount account, double amount) {
        account.deposit(amount);
        Transaction tx = new Transaction(account.getName(), "DEPOSIT", amount, 1.0, LocalDateTime.now());
        account.getTransactions().add(tx);
        UserService.save();
    }

    public void withdraw(SavingsAccount account, double amount) {
        account.withdraw(amount);
        Transaction tx = new Transaction(account.getName(), "WITHDRAWAL", amount, 1.0, LocalDateTime.now());
        account.getTransactions().add(tx);
        UserService.save();
    }

    public double calculateInterest(SavingsAccount account, int months) {
        double rate = account.getInterestRate() / 100 / 12; // Monthly rate
        double balance = account.getBalance();
        return balance * Math.pow(1 + rate, months) - balance;
    }

    public void deleteSavingsAccount(SavingsAccount account) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            user.getSavingsAccounts().removeIf(a -> a.getName().equals(account.getName()));
            UserService.save();
        }
    }
}