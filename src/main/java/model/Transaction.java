package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class Transaction {
    private String assetSymbol;
    private String type; // BUY, SELL, DEPOSIT, WITHDRAWAL
    private double quantity;
    private double price;
    private double fees;
    private double total;
    private String currency;
    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public Transaction() {
    }

    public Transaction(String assetSymbol, String type, double quantity, double price, LocalDateTime timestamp) {
        this.assetSymbol = assetSymbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getSymbol() {
        return assetSymbol;
    } // Alias

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getValue() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %.4f %s @ %.2f",
                timestamp != null ? timestamp.toLocalDate() : "?",
                type, quantity, assetSymbol, price);
    }
}