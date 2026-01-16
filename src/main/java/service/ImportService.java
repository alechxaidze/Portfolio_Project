package service;

import model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ImportService handles the parsing of CSV files for portfolio data.
 * This is designed to be educational for students, showing how to parse
 * CSV files manually and map them to domain models.
 */
public class ImportService {

    public ImportService() {
    }

    /**
     * Main entry point for importing CSV files.
     * It detects the format by scanning the first few lines for known headers.
     */
    public List<Transaction> importCSV(File file, Portfolio targetPortfolio) throws IOException {
        boolean coinbaseMode = false;
        boolean simpleMode = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int linesRead = 0;
            // Scan up to 20 lines for the header
            while ((line = reader.readLine()) != null && linesRead < 20) {
                if (line.startsWith("Timestamp,Transaction Type")) {
                    coinbaseMode = true;
                    break;
                } else if (line.toLowerCase().startsWith("type,symbol,quantity")) {
                    simpleMode = true;
                    break;
                }
                linesRead++;
            }
        }

        if (coinbaseMode) {
            return importCoinbaseCSV(file, targetPortfolio);
        } else if (simpleMode) {
            return importSimpleCSV(file, targetPortfolio);
        } else {
            throw new IOException(
                    "Unknown CSV format. The file must contain a recognized header (e.g., Coinbase or Simple format).");
        }
    }

    private List<Transaction> importCoinbaseCSV(File file, Portfolio targetPortfolio) throws IOException {
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
                Transaction tx = parseCoinbaseLine(line);
                if (tx != null) {
                    transactions.add(tx);
                    // Only "BUY" transactions affect our current asset holdings in this simplified
                    // model
                    if (tx.getType().equals("BUY")) {
                        addOrUpdateAsset(targetPortfolio, tx);
                    }
                }
            }
        }
        UserService.save();
        return transactions;
    }

    private List<Transaction> importSimpleCSV(File file, Portfolio targetPortfolio) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith("type,symbol,quantity")) {
                    headerFound = true;
                    continue;
                }
                if (!headerFound || line.trim().isEmpty()) {
                    continue;
                }
                Transaction tx = parseSimpleLine(line);
                if (tx != null) {
                    transactions.add(tx);
                    addOrUpdateAsset(targetPortfolio, tx);
                }
            }
        }
        UserService.save();
        return transactions;
    }

    private Transaction parseCoinbaseLine(String line) {
        String[] parts = parseCSVLine(line);
        if (parts.length < 8)
            return null;

        try {
            String timestampStr = parts[0].trim();
            String type = parts[1].trim().toUpperCase();
            String asset = parts[2].trim();
            double quantity = parseDouble(parts[3]);
            String currency = parts[4].trim();
            double spotPrice = parseDouble(parts[5]);
            double total = parseDouble(parts[7]);

            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.parse(timestampStr.replace("Z", ""));
            } catch (Exception e) {
                timestamp = LocalDateTime.now();
            }

            Transaction tx = new Transaction(asset, type, quantity, spotPrice, timestamp);
            tx.setCurrency(currency);
            tx.setTotal(total);
            return tx;
        } catch (Exception e) {
            return null;
        }
    }

    private Transaction parseSimpleLine(String line) {
        String[] parts = parseCSVLine(line);
        if (parts.length < 4)
            return null;

        try {
            String type = parts[0].trim().toUpperCase();
            String symbol = parts[1].trim().toUpperCase();
            double quantity = parseDouble(parts[2]);
            double price = parseDouble(parts[3]);

            return new Transaction(symbol, type, quantity, price, LocalDateTime.now());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Splits a CSV line handling quoted values correctly.
     */
    private String[] parseCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString().trim());
        return parts.toArray(new String[0]);
    }

    private double parseDouble(String value) {
        try {
            String cleaned = value.replace(",", ".").replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * This is a critical method for students to understand.
     * It handles the logic of merging a new transaction into existing portfolio
     * holdings.
     * It uses the weighted average cost basis formula.
     */
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
            // Weighted average formula: (NewQty * NewPrice + OldQty * OldPrice) / (NewQty +
            // OldQty)
            double oldTotalCost = existing.getQuantity() * existing.getAvgPurchasePrice();
            double newTotalCost = quantity * price;
            double totalQty = existing.getQuantity() + quantity;

            double avgPrice = (oldTotalCost + newTotalCost) / totalQty;

            existing.setQuantity(totalQty);
            existing.setAvgPurchasePrice(avgPrice);
        } else {
            AssetType type = determineAssetType(symbol);
            Asset newAsset = new Asset(symbol, symbol, type, quantity, price);
            // Default current price to buy price initially
            newAsset.setCurrentPrice(price);
            portfolio.getAssets().add(newAsset);
        }

        portfolio.getTransactionHistory().add(tx);
    }

    private AssetType determineAssetType(String symbol) {
        String s = symbol.toUpperCase();
        String[] cryptos = { "BTC", "ETH", "LTC", "SOL", "LINK", "XRP", "ADA", "DOGE", "BNB", "DOT", "MATIC", "AVAX" };
        for (String c : cryptos) {
            if (s.equals(c))
                return AssetType.CRYPTO;
        }
        return AssetType.STOCK; // Default to stock
    }
}
