package model;

import java.time.LocalDate;


public class PortfolioSnapshot {
    private LocalDate date;
    private double totalValue;
    private String portfolioId;

    public PortfolioSnapshot() {
    }

    public PortfolioSnapshot(LocalDate date, double totalValue, String portfolioId) {
        this.date = date;
        this.totalValue = totalValue;
        this.portfolioId = portfolioId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }
}
