package model;

import java.util.ArrayList;
import java.util.List;

public class SavingsAccount {
    private String name;
    private double balance;
    private double interestRate;
    private List<Transaction> transactions = new ArrayList<>();

    public SavingsAccount() {
    }

    public SavingsAccount(String name, double initialBalance, double interestRate) {
        this.name = name;
        this.balance = initialBalance;
        this.interestRate = interestRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= balance) {
            this.balance -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }
}