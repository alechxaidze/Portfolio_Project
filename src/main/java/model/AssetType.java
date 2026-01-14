package model;

public enum AssetType {
    STOCK("Stock"),
    CRYPTO("Cryptocurrency"),
    BOND("Bond"),
    ETF("ETF"),
    MUTUAL_FUND("Mutual Fund"),
    REAL_ESTATE("Real Estate"),
    CASH("Cash");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}