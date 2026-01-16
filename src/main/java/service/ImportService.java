package service;

import model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class ImportService {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter BINANCE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ImportService() {
    }

    public List<Transaction> importCoinbaseCSV(File file, Portfolio targetPortfolio) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Timestamp,Transaction Type")) {
                    headerFound = true;
                    continue;
                }
                if (!headerFound || line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Transaction tx = parseCoinbaseLine(line);
                    if (tx != null) {
                        transactions.add(tx);
                        if (tx.getType().equals("BUY")) {
                            addOrUpdateAsset(targetPortfolio, tx);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse line: " + line + " - " + e.getMessage());
                }
            }
        }
        UserService.save();

        return transactions;
    }

    private Transaction parseCoinbaseLine(String line) {
        String[] parts = parseCSVLine(line);

        if (parts.length < 8) {
            return null;
        }

        String timestampStr = parts[0].trim();
        String type = parts[1].trim().toUpperCase();
        String asset = parts[2].trim();
        double quantity = parseDouble(parts[3]);
        String currency = parts[4].trim();
        double spotPrice = parseDouble(parts[5]);
        double total = parseDouble(parts[7]);
        double fees = parts.length > 8 ? parseDouble(parts[8]) : 0;

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(timestampStr.replace("Z", ""));
        } catch (Exception e) {
            timestamp = LocalDateTime.now();
        }

        Transaction tx = new Transaction(asset, type, quantity, spotPrice, timestamp);
        tx.setFees(fees);
        tx.setCurrency(currency);
        tx.setTotal(total);

        return tx;
    }

    private String[] parseCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());

        return parts.toArray(new String[0]);
    }

    /**
     * Import transactions from Binance CSV export
     */
    public List<Transaction> importBinanceCSV(File file, Portfolio targetPortfolio) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                // Skip header and empty lines
                if (lineNum == 1 || line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Transaction tx = parseBinanceLine(line);
                    if (tx != null) {
                        transactions.add(tx);
                        if (tx.getType().equals("BUY") || tx.getType().equals("DEPOSIT")) {
                            addOrUpdateAsset(targetPortfolio, tx);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse Binance line " + lineNum + ": " + line + " - " + e.getMessage());
                }
            }
        }
        UserService.save();
        return transactions;
    }

    /**
     * Parse a Binance CSV line
     * Expected format: Date,Coin,Change,Remark
     * Example: 2021-01-15 10:30:45,BTC,0.5,Buy
     */
    private Transaction parseBinanceLine(String line) {
        String[] parts = parseCSVLine(line);

        if (parts.length < 4) {
            return null;
        }

        String dateStr = parts[0].trim();
        String coin = parts[1].trim().toUpperCase();
        double change = parseDouble(parts[2]);
        String remark = parts[3].trim().toUpperCase();

        // Determine transaction type
        String type = "BUY";
        if (remark.contains("SELL") || remark.contains("SOLD")) {
            type = "SELL";
        } else if (remark.contains("WITHDRAW")) {
            type = "SELL";
        } else if (remark.contains("DEPOSIT")) {
            type = "BUY";
        }

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(dateStr, BINANCE_FORMAT);
        } catch (Exception e) {
            timestamp = LocalDateTime.now();
        }

        // Mock price lookup (in real scenario, would fetch from API)
        ApiService apiService = new ApiService();
        double price = apiService.getCurrentPrice(coin);

        Transaction tx = new Transaction(coin, type, Math.abs(change), price, timestamp);
        tx.setCurrency("USD");
        tx.setTotal(Math.abs(change) * price);
        tx.setFees(0);

        return tx;
    }

    private double parseDouble(String value) {
        try {
            String cleaned = value.replace(",", ".").replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private void addOrUpdateAsset(Portfolio portfolio, Transaction tx) {
        String symbol = tx.getSymbol();
        double quantity = tx.getQuantity();
        double price = tx.getPrice();
        Asset existing = null;
        for (Asset a : portfolio.getAssets()) {
            if (a.getSymbol().equalsIgnoreCase(symbol)) {
                existing = a;
                break;
            }
        }

        if (existing != null) {
            double totalQty = existing.getQuantity() + quantity;
            double avgPrice = ((existing.getQuantity() * existing.getAvgPurchasePrice())
                    + (quantity * price)) / totalQty;
            existing.setQuantity(totalQty);
            existing.setAvgPurchasePrice(avgPrice);
        } else {
            AssetType type = determineAssetType(symbol);
            Asset newAsset = new Asset(symbol, symbol, type, quantity, price);
            portfolio.getAssets().add(newAsset);
        }

        portfolio.getTransactionHistory().add(tx);
    }

    private AssetType determineAssetType(String symbol) {
        String[] cryptoSymbols = { "BTC", "ETH", "LTC", "SOL", "LINK", "XRP", "ADA", "DOGE", "BNB", "DOT" };
        for (String crypto : cryptoSymbols) {
            if (symbol.equalsIgnoreCase(crypto)) {
                return AssetType.CRYPTO;
            }
        }

        String[] stockSymbols = { "AAPL", "APPLE", "MSFT", "GOOGL", "TSLA", "TESLA", "AMZN", "META", "NVDA" };
        for (String stock : stockSymbols) {
            if (symbol.equalsIgnoreCase(stock)) {
                return AssetType.STOCK;
            }
        }
        return AssetType.CRYPTO;
    }
    public static class ImportResult {
        public int totalTransactions;
        public int successfulImports;
        public int failedImports;
        public double totalValue;
        public List<String> errors = new ArrayList<>();

        @Override
        public String toString() {
            return String.format("Imported %d/%d transactions (Total: €%.2f)",
                    successfulImports, totalTransactions, totalValue);
        }
    }
}
