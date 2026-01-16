# Quick Reference - New Features Guide

## 🔐 Encryption (XORCoder Integration)

### What It Does
Automatically encrypts your portfolio data when saving and decrypts when loading.

### Enable/Disable
```java
// Enable encryption (default)
UserService.setEncryptionEnabled(true);

// Disable encryption
UserService.setEncryptionEnabled(false);

// Check status
if (UserService.isEncryptionEnabled()) {
    System.out.println("Data is encrypted");
}
```

### Result
- `portfolio_data.json` becomes unreadable binary when encryption is ON
- Data is safely stored and automatically unlocked on load
- No password needed - encryption is automatic

---

## 📥 Binance Import

### What It Does
Imports your Binance transaction history from CSV files.

### CSV Format Required
```
Date,Coin,Change,Remark
2021-01-15 10:30:45,BTC,0.5,Buy
2021-02-20 14:22:10,ETH,5.0,Bought
2021-03-10 09:15:30,LINK,100,Sell
```

### Code
```java
ImportService importer = new ImportService();
File binanceFile = new File("binance_export.csv");
Portfolio myPortfolio = // your portfolio
List<Transaction> imported = importer.importBinanceCSV(binanceFile, myPortfolio);

System.out.println("Imported " + imported.size() + " transactions");
```

### Supported Remarks
- BUY, BOUGHT, DEPOSIT → Treated as BUY
- SELL, SOLD, WITHDRAW → Treated as SELL
- Case-insensitive

### Result
- All transactions added to portfolio
- Assets updated automatically
- Prices fetched from API

---

## 🐋 Whale Monitoring (Whale Hunting)

### What It Does
Tracks large cryptocurrency transactions and alerts when whales move tokens.

### Quick Setup
```java
WhaleMonitoringService wms = WhaleMonitoringService.getInstance();

// Start monitoring an address
wms.monitorAddress("Ethereum", "0xYourWalletAddress", portfolioId);

// Listen for alerts
List<WhaleMonitoringService.WhaleAlert> alerts = wms.getActiveAlerts();
for (WhaleAlert alert : alerts) {
    System.out.println(alert);
    // Output: 🐋 WHALE ALERT - ETH: 1000.00 ETH (≈$2000000)
}
```

### Customize Thresholds
```java
WhaleMonitoringService wms = WhaleMonitoringService.getInstance();

// Set custom threshold for a token (in USD)
wms.setThreshold("BTC", 1000000);      // Alert if > $1M
wms.setThreshold("ETH", 500000);       // Alert if > $500K
wms.setThreshold("SOL", 50000);        // Alert if > $50K

// Get current threshold
double btcThreshold = wms.getThreshold("BTC");  // 1000000.0
```

### Default Thresholds
| Token | Threshold |
|-------|-----------|
| BTC | $500,000 |
| ETH | $50,000 |
| BNB | $25,000 |
| SOL | $10,000 |
| XRP | $5,000 |
| ADA | $5,000 |

### Get Alerts
```java
// Get all alerts
List<WhaleAlert> allAlerts = wms.getActiveAlerts();

// Get alerts for specific token
List<WhaleAlert> ethAlerts = wms.getAlertsForToken("ETH");

// Get alerts for specific blockchain
List<WhaleAlert> ethereumAlerts = wms.getAlertsForBlockchain("Ethereum");

// Clean alerts older than 24 hours
wms.cleanupOldAlerts();
```

### Alert Information
Each whale alert contains:
- `token` - Token symbol (BTC, ETH, etc.)
- `amount` - Number of tokens
- `usdValue` - Value in USD
- `blockchain` - Which blockchain
- `timestamp` - When it happened
- `fromAddress` - Sender address
- `toAddress` - Receiver address
- `isRelatedToPortfolio` - Connected to your portfolio

### How It Works
1. Runs every 10 minutes automatically
2. Checks configured addresses
3. Compares transaction amounts to thresholds
4. Creates WhaleAlert objects
5. Integrates with Event system
6. Alert events appear in your portfolio timeline

---

## 💰 Wallet Monitoring

### What It Does
Tracks cryptocurrency wallet balances and alerts when balances change.

### Monitor Single Wallet
```java
WalletMonitoringService wms = WalletMonitoringService.getInstance();

// Start monitoring
wms.monitorWallet("Ethereum", "0xYourWalletAddress");

// Later, get snapshot
WalletMonitoringService.WalletSnapshot snapshot = 
    wms.getWalletSnapshot("Ethereum", "0xYourWalletAddress");

System.out.println(snapshot.totalUsdValue);  // Total USD value
System.out.println(snapshot.balances);       // { ETH: 10.5, BTC: 0.25 }
System.out.println(snapshot.timestamp);      // When it was checked
```

### Monitor Portfolio Wallets
```java
WalletMonitoringService wms = WalletMonitoringService.getInstance();

// Automatically monitor all wallets in portfolio
Portfolio myPortfolio = // your portfolio
wms.monitorPortfolioWallets(myPortfolio);

// Works if your Crypto assets have contractAddress set
```

### Get Balance Info
```java
WalletMonitoringService wms = WalletMonitoringService.getInstance();

// Get balance of specific token
double ethBalance = wms.getTokenBalance("Ethereum", "0xAddress", "ETH");
double btcBalance = wms.getTokenBalance("Bitcoin", "1address...", "BTC");

// Get all monitored wallets
List<WalletSnapshot> allWallets = wms.getAllWallets();

// Get total monitored value across all wallets
double totalValue = wms.getTotalMonitoredValue();
```

### Balance Change Information
When balances change, system automatically generates:
- Token changed
- Previous balance
- Current balance
- Change amount (positive/negative)
- Change percentage
- Timestamp

### Supported Tokens
- BTC (Bitcoin)
- ETH (Ethereum)
- BNB (Binance Coin)
- SOL (Solana)
- ADA (Cardano)
- XRP (Ripple)

### How It Works
1. Runs every 15 minutes
2. Fetches current balances
3. Compares to previous snapshot
4. Detects changes
5. Calculates USD values
6. Stores historical data

---

## 📊 Integration Example

Monitor a complete portfolio with all features:

```java
// Setup
Portfolio myPortfolio = new Portfolio("My Holdings", "Tracked assets");

// 1. Import historical transactions
ImportService importer = new ImportService();
List<Transaction> txs = importer.importBinanceCSV(
    new File("binance.csv"), 
    myPortfolio
);

// 2. Enable encryption for security
UserService.setEncryptionEnabled(true);
UserService.save();

// 3. Monitor for whale activity
WhaleMonitoringService wms = WhaleMonitoringService.getInstance();
wms.setThreshold("ETH", 100000);  // Alert on $100K+ transactions
wms.monitorAddress("Ethereum", "0x742d35Cc6634C0532925a3b844Bc9e7595f...", 
                   myPortfolio.getId());

// 4. Monitor wallet balances
WalletMonitoringService wallet = WalletMonitoringService.getInstance();
wallet.monitorPortfolioWallets(myPortfolio);

// 5. Check status
System.out.println("Portfolio value: $" + myPortfolio.getCurrentValue());
System.out.println("Whale alerts: " + wms.getActiveAlerts().size());
System.out.println("Monitored wallets: " + wallet.getAllWallets().size());
```

---

## 🔧 Configuration

### Encryption Key (Optional)
To change the encryption key, edit [UserService.java](src/main/java/service/UserService.java) line 12:

```java
private static final String ENCRYPTION_KEY = "your_custom_key_here";
```

### Monitoring Intervals
- **Whale Monitoring**: Every 10 minutes
- **Wallet Monitoring**: Every 15 minutes

To change, edit service files:
```java
executor.scheduleAtFixedRate(() -> { ... }, 0, 5, TimeUnit.MINUTES);
//                                            ↑
//                                            Change this
```

### Use Real Data
By default, services use mock data. To use real API calls:

```java
ApiService api = new ApiService();
api.setUseMockData(false);
api.setAlphaVantageApiKey("YOUR_API_KEY");  // Get from alphavantage.co
```

---

## ✅ Verification

### Is Encryption Working?
```
1. Save a portfolio
2. Open portfolio_data.json in text editor
3. Should be unreadable binary (not JSON)
4. If you see JSON text → encryption is OFF
```

### Is Whale Monitoring Working?
```
1. Check console for:
   "🐋 WHALE ALERT - ETH: 50.00 ETH (≈$115000)"
2. Check Events in portfolio
3. Look for whale event titles
```

### Is Wallet Monitoring Working?
```
1. Check console for:
   "💰 Balance Change: ▲ ETH: 0.50 ETH (25.0%)"
2. Verify snapshots are created
3. Check total USD value updates
```

---

## 🐛 Troubleshooting

### Encryption not saving
- Check file permissions on portfolio_data.json
- Verify UserService.save() is called
- Check console for IO exceptions

### Binance import fails
- Verify CSV format is correct (Date, Coin, Change, Remark)
- Ensure dates are in format: YYYY-MM-DD HH:MM:SS
- Check that coin symbols are recognized

### Whale alerts not appearing
- Verify threshold is set: `wms.setThreshold("BTC", 500000)`
- Check address is being monitored: `wms.monitorAddress(...)`
- Monitor runs every 10 minutes - wait for update
- Check that portfolio ID is provided

### Wallet monitoring not updating
- Ensure addresses are set in Crypto assets
- Check contractAddress field is populated
- Verify blockchain name matches configured blockchain
- Wait 15 minutes for first update

---

## 📞 Common Issues

### Q: Data file is encrypted but I can't load it
A: Make sure encryption is ENABLED when you save, and ENABLED when you load

### Q: Whale alerts stopped appearing
A: Call `wms.cleanupOldAlerts()` if too many alerts pile up

### Q: Portfolio value is empty
A: Import transactions first, then prices are fetched automatically

### Q: Services consume too much memory
A: Reduce monitoring frequency or call `shutdown()` on services you don't need

---

## 📚 Files Reference

| Feature | Class | Location |
|---------|-------|----------|
| Encryption | UserService | src/.../service/UserService.java |
| Binance Import | ImportService | src/.../service/ImportService.java |
| Whale Monitoring | WhaleMonitoringService | src/.../service/WhaleMonitoringService.java |
| Wallet Monitoring | WalletMonitoringService | src/.../service/WalletMonitoringService.java |
| XOR Cipher | XORCoder | src/.../util/XORCoder.java |
| Tests | IntegrationTest | src/.../service/IntegrationTest.java |

---

**Last Updated**: January 16, 2026
**Status**: Ready to Use ✅
