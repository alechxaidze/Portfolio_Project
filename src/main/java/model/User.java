package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String email;
    private String password;
    private List<Portfolio> portfolios;
    private List<SavingsAccount> savingsAccounts;
    private List<Event> globalEvents;
    private Currency preferredCurrency;

    public User() {
        this.portfolios = new ArrayList<>();
        this.savingsAccounts = new ArrayList<>();
        this.globalEvents = new ArrayList<>();
        this.preferredCurrency = Currency.USD;
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    public List<SavingsAccount> getSavingsAccounts() {
        return savingsAccounts;
    }

    public void setSavingsAccounts(List<SavingsAccount> savingsAccounts) {
        this.savingsAccounts = savingsAccounts;
    }

    public List<Event> getGlobalEvents() {
        return globalEvents;
    }

    public void setGlobalEvents(List<Event> globalEvents) {
        this.globalEvents = globalEvents;
    }

    public Currency getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(Currency preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public void addPortfolio(Portfolio portfolio) {
        this.portfolios.add(portfolio);
    }

    public void removePortfolio(Portfolio portfolio) {
        this.portfolios.removeIf(p -> p.getId().equals(portfolio.getId()));
    }

    public Portfolio getPortfolioById(String id) {
        return portfolios.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addGlobalEvent(Event event) {
        event.setPortfolioId(null);
        this.globalEvents.add(event);
    }

    public double getTotalPortfolioValue() {
        return portfolios.stream().mapToDouble(Portfolio::getTotalValue).sum();
    }

    public double getTotalSavingsBalance() {
        return savingsAccounts.stream().mapToDouble(SavingsAccount::getBalance).sum();
    }

    public double getNetWorth() {
        return getTotalPortfolioValue() + getTotalSavingsBalance();
    }
}
