package service;

import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static java.util.UUID.randomUUID;

public class WhaleMonitoringService {
    private static WhaleMonitoringService instance;
    private Map<String, Double> tokenThresholds;
    private List<WhaleAlert> activeAlerts;
    private ScheduledExecutorService executor;
    private Map<String, LocalDateTime> lastCheckTime;

    public static class WhaleAlert {
        public String id;
        public String token;
        public double amount;
        public double usdValue;
        public String fromAddress;
        public String toAddress;
        public LocalDateTime timestamp;
        public String blockchain;
        public boolean isRelatedToPortfolio;

        public WhaleAlert() {
            this.id = randomUUID().toString();
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("WHALE ALERT - %s: %.2f %s (≈$%.0f) at %s",
                    blockchain, amount, token, usdValue, timestamp);
        }
    }

    public static WhaleMonitoringService getInstance() {
        if (instance == null) {
            instance = new WhaleMonitoringService();
        }
        return instance;
    }

    private WhaleMonitoringService() {
        tokenThresholds = new HashMap<>();
        activeAlerts = Collections.synchronizedList(new ArrayList<>());
        lastCheckTime = new HashMap<>();
        executor = Executors.newScheduledThreadPool(2);

        // Default thresholds (in USD)
        setThreshold("BTC", 500000);
        setThreshold("ETH", 50000);
        setThreshold("BNB", 25000);
        setThreshold("SOL", 10000);
        setThreshold("XRP", 5000);
        setThreshold("ADA", 5000);
    }

    public void setThreshold(String token, double usdValue) {
        tokenThresholds.put(token.toUpperCase(), usdValue);
    }

    public double getThreshold(String token) {
        return tokenThresholds.getOrDefault(token.toUpperCase(), 50000.0);
    }

    /**
     * Monitor a blockchain address for large transactions
     */
    public void monitorAddress(String blockchain, String address, String portfolioId) {
        executor.scheduleAtFixedRate(() -> {
            checkAddressTransactions(blockchain, address, portfolioId);
        }, 0, 10, TimeUnit.MINUTES);
    }

    private void checkAddressTransactions(String blockchain, String address, String portfolioId) {
        try {
            List<WhaleTransaction> transactions = fetchTransactionsForAddress(blockchain, address);
            
            for (WhaleTransaction tx : transactions) {
                // Check if transaction amount exceeds threshold
                double threshold = getThreshold(tx.tokenSymbol);
                double usdValue = tx.amount * getTokenPrice(tx.tokenSymbol);
                
                if (usdValue >= threshold) {
                    WhaleAlert alert = createAlert(tx, blockchain, usdValue, portfolioId != null);
                    activeAlerts.add(alert);
                    System.out.println("🐋 " + alert);
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking address " + address + ": " + e.getMessage());
        }
    }

    private List<WhaleTransaction> fetchTransactionsForAddress(String blockchain, String address) {
        List<WhaleTransaction> transactions = new ArrayList<>();
        Random random = new Random();
        int numTransactions = random.nextInt(3) + 1;
        String[] tokens = {"BTC", "ETH", "BNB", "SOL"};
        
        for (int i = 0; i < numTransactions; i++) {
            WhaleTransaction tx = new WhaleTransaction();
            tx.blockchain = blockchain;
            tx.fromAddress = "0x" + String.format("%040x", random.nextLong()).substring(0, 40);
            tx.toAddress = address;
            tx.tokenSymbol = tokens[random.nextInt(tokens.length)];
            
            if (tx.tokenSymbol.equals("BTC")) {
                tx.amount = random.nextDouble() * 50 + 10;
            } else if (tx.tokenSymbol.equals("ETH")) {
                tx.amount = random.nextDouble() * 500 + 100;
            } else {
                tx.amount = random.nextDouble() * 10000 + 1000;
            }
            
            tx.timestamp = LocalDateTime.now().minusHours(random.nextInt(24));
            transactions.add(tx);
        }
        
        return transactions;
    }

    private double getTokenPrice(String token) {
        ApiService apiService = new ApiService();
        return apiService.getCurrentPrice(token);
    }

    private WhaleAlert createAlert(WhaleTransaction tx, String blockchain, double usdValue, boolean relatedToPortfolio) {
        WhaleAlert alert = new WhaleAlert();
        alert.token = tx.tokenSymbol;
        alert.amount = tx.amount;
        alert.usdValue = usdValue;
        alert.fromAddress = tx.fromAddress;
        alert.toAddress = tx.toAddress;
        alert.timestamp = tx.timestamp;
        alert.blockchain = blockchain;
        alert.isRelatedToPortfolio = relatedToPortfolio;
        return alert;
    }

    public List<WhaleAlert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }

    public List<WhaleAlert> getAlertsForToken(String token) {
        List<WhaleAlert> result = new ArrayList<>();
        for (WhaleAlert alert : activeAlerts) {
            if (alert.token.equalsIgnoreCase(token)) {
                result.add(alert);
            }
        }
        return result;
    }

    public List<WhaleAlert> getAlertsForBlockchain(String blockchain) {
        List<WhaleAlert> result = new ArrayList<>();
        for (WhaleAlert alert : activeAlerts) {
            if (alert.blockchain.equalsIgnoreCase(blockchain)) {
                result.add(alert);
            }
        }
        return result;
    }

    public void clearOldAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        activeAlerts.removeIf(alert -> alert.timestamp.isBefore(cutoff));
    }

    public void shutdown() {
        executor.shutdown();
    }

    public static class WhaleTransaction {
        public String blockchain;
        public String fromAddress;
        public String toAddress;
        public String tokenSymbol;
        public double amount;
        public LocalDateTime timestamp;
    }
}
