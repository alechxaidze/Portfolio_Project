package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Portfolio {
    private String id;
    private String name;
    private String description;
    private List<Asset> assets;
    private List<Transaction> transactionHistory;
    private List<Event> events;
    private List<PortfolioSnapshot> snapshots;
    private Currency referenceCurrency;

    public Portfolio() {
        this.id = UUID.randomUUID().toString();
        this.assets = new ArrayList<>();
        this.transactionHistory = new ArrayList<>();
        this.events = new ArrayList<>();
        this.snapshots = new ArrayList<>();
        this.referenceCurrency = Currency.USD;
    }

    public Portfolio(String name) {
        this();
        this.name = name;
    }

    public Portfolio(String name, String description) {
        this(name);
        this.description = description;
    }
    public Portfolio clone(String newName) {
        Portfolio cloned = new Portfolio(newName, this.description);
        for (Asset asset : this.assets) {
            Asset clonedAsset = new Asset(
                    asset.getSymbol(),
                    asset.getName(),
                    asset.getType(),
                    asset.getQuantity(),
                    asset.getAvgPurchasePrice());
            clonedAsset.setCurrentPrice(asset.getCurrentPrice());
            cloned.addAsset(clonedAsset);
        }
        cloned.setReferenceCurrency(this.referenceCurrency);
        return cloned;
    }

    public void addAsset(Asset asset) {
        for (Asset existing : assets) {
            if (existing.getSymbol().equalsIgnoreCase(asset.getSymbol())) {
                double totalQty = existing.getQuantity() + asset.getQuantity();
                double avgPrice = ((existing.getQuantity() * existing.getAvgPurchasePrice())
                        + (asset.getQuantity() * asset.getAvgPurchasePrice())) / totalQty;
                existing.setQuantity(totalQty);
                existing.setAvgPurchasePrice(avgPrice);
                return;
            }
        }
        assets.add(asset);
    }

    public void removeAsset(Asset asset) {
        assets.removeIf(a -> a.getSymbol().equalsIgnoreCase(asset.getSymbol()));
    }

    public double getTotalValue() {
        return assets.stream().mapToDouble(Asset::getValue).sum();
    }

    public double getTotalCost() {
        return assets.stream().mapToDouble(Asset::getCostBasis).sum();
    }

    public double getProfitLoss() {
        return getTotalValue() - getTotalCost();
    }

    public double getProfitLossPercent() {
        double cost = getTotalCost();
        return cost > 0 ? (getProfitLoss() / cost) * 100 : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(List<Transaction> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<PortfolioSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<PortfolioSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public Currency getReferenceCurrency() {
        return referenceCurrency;
    }

    public void setReferenceCurrency(Currency referenceCurrency) {
        this.referenceCurrency = referenceCurrency;
    }

    public void addEvent(Event event) {
        event.setPortfolioId(this.id);
        this.events.add(event);
    }

    public void recordSnapshot() {
        PortfolioSnapshot snapshot = new PortfolioSnapshot(
                java.time.LocalDate.now(),
                getTotalValue(),
                this.id);
        this.snapshots.add(snapshot);
    }

    @Override
    public String toString() {
        return name + (description != null && !description.isEmpty() ? " - " + description : "");
    }
}