package service;

import model.Crypto;
import model.Portfolio;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WalletMonitoringService {
    private static WalletMonitoringService instance;
    private Map<String, WalletSnapshot> walletSnapshots;
    private ScheduledExecutorService executor;
    private Map<String, Long> lastUpdateTime;

    public static class WalletSnapshot {
        public String address;
        public String blockchain;
        public Map<String, Double> balances;
        public LocalDateTime timestamp;
        public double totalUsdValue;

        public WalletSnapshot(String address, String blockchain) {
            this.address = address;
            this.blockchain = blockchain;
            this.balances = new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("Wallet %s on %s - Total USD Value: $%.2f (Updated: %s)",
                    address.substring(0, Math.min(10, address.length())), blockchain, totalUsdValue, timestamp);
        }
    }

    public static class BalanceChange {
        public String address;
        public String token;
        public double previousBalance;
        public double currentBalance;
        public double changeAmount;
        public double changePercent;
        public LocalDateTime timestamp;

        @Override
        public String toString() {
            String direction = changeAmount >= 0 ? "▲" : "▼";
            return String.format("%s %s: %.2f %s (%.1f%%) at %s",
                    direction, token, Math.abs(changeAmount), token, changePercent, timestamp);
        }
    }

    public static WalletMonitoringService getInstance() {
        if (instance == null) {
            instance = new WalletMonitoringService();
        }
        return instance;
    }

    private WalletMonitoringService() {
        walletSnapshots = Collections.synchronizedMap(new HashMap<>());
        lastUpdateTime = Collections.synchronizedMap(new HashMap<>());
        executor = Executors.newScheduledThreadPool(2);
    }

    public void monitorWallet(String blockchain, String address) {
        executor.scheduleAtFixedRate(() -> {
            updateWalletBalance(blockchain, address);
        }, 0, 15, TimeUnit.MINUTES);
    }

    private void updateWalletBalance(String blockchain, String address) {
        try {
            WalletSnapshot newSnapshot = fetchWalletBalance(blockchain, address);
            
            String key = blockchain + ":" + address;
            WalletSnapshot oldSnapshot = walletSnapshots.get(key);
            
            if (oldSnapshot != null) {
                // Check for balance changes
                List<BalanceChange> changes = compareSnapshots(oldSnapshot, newSnapshot);
                for (BalanceChange change : changes) {
                    System.out.println("💰 Balance Change: " + change);
                }
            }
            
            walletSnapshots.put(key, newSnapshot);
            lastUpdateTime.put(key, System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("Error updating wallet " + address + ": " + e.getMessage());
        }
    }

    private WalletSnapshot fetchWalletBalance(String blockchain, String address) {
        WalletSnapshot snapshot = new WalletSnapshot(address, blockchain);
        ApiService apiService = new ApiService();

        // Simulate fetching balances
        String[] tokens = {"BTC", "ETH", "BNB", "SOL", "ADA", "XRP"};
        Random random = new Random();

            if (random.nextDouble() > 0.4) { // 60% chance of having each token
                double balance = random.nextDouble() * 100;
                double price = apiService.getCurrentPrice(token);
                snapshot.balances.put(token, balance);
                snapshot.totalUsdValue += balance * price;
            }
        }

        snapshot.timestamp = LocalDateTime.now();
        return snapshot;
    }

    private List<BalanceChange> compareSnapshots(WalletSnapshot oldSnapshot, WalletSnapshot newSnapshot) {
        List<BalanceChange> changes = new ArrayList<>();
        ApiService apiService = new ApiService();

        // Check for balance changes
        Set<String> allTokens = new HashSet<>();
        allTokens.addAll(oldSnapshot.balances.keySet());
        allTokens.addAll(newSnapshot.balances.keySet());

        for (String token : allTokens) {
            double oldBalance = oldSnapshot.balances.getOrDefault(token, 0.0);
            double newBalance = newSnapshot.balances.getOrDefault(token, 0.0);

            if (Math.abs(oldBalance - newBalance) > 0.0001) {
                BalanceChange change = new BalanceChange();
                change.address = oldSnapshot.address;
                change.token = token;
                change.previousBalance = oldBalance;
                change.currentBalance = newBalance;
                change.changeAmount = newBalance - oldBalance;
                change.changePercent = oldBalance > 0 ? (change.changeAmount / oldBalance) * 100 : 0;
                change.timestamp = LocalDateTime.now();
                changes.add(change);
            }
        }

        return changes;
    }

    public WalletSnapshot getWalletSnapshot(String blockchain, String address) {
        String key = blockchain + ":" + address;
        return walletSnapshots.get(key);
    }

    public double getTokenBalance(String blockchain, String address, String token) {
        WalletSnapshot snapshot = getWalletSnapshot(blockchain, address);
        if (snapshot != null) {
            return snapshot.balances.getOrDefault(token.toUpperCase(), 0.0);
        }
        return 0.0;
    }

    public double getTotalMonitoredValue() {
        double total = 0;
        for (WalletSnapshot snapshot : walletSnapshots.values()) {
            total += snapshot.totalUsdValue;
        }
        return total;
    }

    /**
     * Monitor all wallets in a portfolio
     */
                Crypto crypto = (Crypto) asset;
                if (crypto.getContractAddress() != null && !crypto.getContractAddress().isEmpty()) {
                    monitorWallet(crypto.getBlockchain(), crypto.getContractAddress());
                    System.out.println("Started monitoring " + crypto.getSymbol() + " wallet: " + 
                            crypto.getContractAddress().substring(0, Math.min(10, crypto.getContractAddress().length())));
                }
            }
        }
    }

    /**
     * Get all monitored wallets
     */
    
    /**
     * Stop monitoring
     */
    
