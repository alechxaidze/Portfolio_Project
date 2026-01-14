package model;

public class Asset {
    private String symbol;
    private String name;
    private AssetType type;
    private double quantity;
    private double avgPurchasePrice;
    private double currentPrice;

    public Asset() {
    }

    public Asset(String symbol, String name, AssetType type, double quantity, double avgPurchasePrice) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.avgPurchasePrice = avgPurchasePrice;
    }

    public Asset(String name, String symbol, AssetType type, double currentPrice) {
        this.name = name;
        this.symbol = symbol;
        this.type = type;
        this.currentPrice = currentPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAvgPurchasePrice() {
        return avgPurchasePrice;
    }

    public void setAvgPurchasePrice(double avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getValue() {
        return quantity * currentPrice;
    }

    public double getCostBasis() {
        return quantity * avgPurchasePrice;
    }

    public double getProfitLoss() {
        return getValue() - getCostBasis();
    }
}