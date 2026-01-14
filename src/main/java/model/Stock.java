package model;

public class Stock extends Asset {
    private String companyName;
    private String sector;
    private double dividendYield;
    private double marketCap;

    public Stock() {
        super();
        this.setType(AssetType.STOCK);
    }

    public Stock(String name, String symbol, String companyName, double currentPrice) {
        super(name, symbol, AssetType.STOCK, currentPrice);
        this.companyName = companyName;
    }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }
}