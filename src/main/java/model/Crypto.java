package model;

public class Crypto extends Asset {
    private String blockchain;
    private String contractAddress;
    private double marketCap;
    private double volume24h;

    public Crypto() {
        super();
        this.setType(AssetType.CRYPTO);
    }

    public Crypto(String name, String symbol, String blockchain, double currentPrice) {
        super(name, symbol, AssetType.CRYPTO, currentPrice);
        this.blockchain = blockchain;
    }
    public String getBlockchain() { return blockchain; }
    public void setBlockchain(String blockchain) { this.blockchain = blockchain; }

    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

    public double getVolume24h() { return volume24h; }
    public void setVolume24h(double volume24h) { this.volume24h = volume24h; }
}