package org.isep.project_work;

public class AlphaVantageClient {
    private static final String API_KEY = "demo"; // Use 'demo' for testing

    public static double getStockPrice(String symbol) {
        switch(symbol.toUpperCase()) {
            case "AAPL": return 175.25;
            case "GOOGL": return 135.50;
            case "MSFT": return 330.75;
            case "TSLA": return 245.80;
            case "BTC": return 45000.00;
            case "ETH": return 2500.00;
            default: return 100.00;
        }
    }

    public static String getStockInfo(String symbol) {
        return String.format("%s: $%.2f", symbol, getStockPrice(symbol));
    }
}