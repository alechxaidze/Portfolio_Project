package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service for fetching real-time market data
 * Uses mock data by default, can be configured for real APIs
 */
public class ApiService {

    // API Keys (set via setApiKey method)
    private static String alphaVantageKey = "demo";
    private static String coinGeckoKey = null; // CoinGecko has free tier

    // Cache for prices
    private static final Map<String, Double> priceCache = new HashMap<>();
    private static final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION_MS = 60 * 1000; // 1 minute cache

    // Mock data for demo
    private static final Map<String, Double> MOCK_STOCK_PRICES = new HashMap<>();
    private static final Map<String, Double> MOCK_CRYPTO_PRICES = new HashMap<>();

    static {
        // Mock stock prices
        MOCK_STOCK_PRICES.put("AAPL", 178.50);
        MOCK_STOCK_PRICES.put("MSFT", 374.25);
        MOCK_STOCK_PRICES.put("GOOGL", 141.80);
        MOCK_STOCK_PRICES.put("TSLA", 248.90);
        MOCK_STOCK_PRICES.put("AMZN", 178.25);
        MOCK_STOCK_PRICES.put("NVDA", 495.50);
        MOCK_STOCK_PRICES.put("META", 505.75);
        MOCK_STOCK_PRICES.put("JPM", 195.30);
        MOCK_STOCK_PRICES.put("V", 275.40);
        MOCK_STOCK_PRICES.put("SPY", 478.20);
        MOCK_STOCK_PRICES.put("QQQ", 405.60);

        // Mock crypto prices
        MOCK_CRYPTO_PRICES.put("BTC", 43250.00);
        MOCK_CRYPTO_PRICES.put("ETH", 2280.50);
        MOCK_CRYPTO_PRICES.put("BNB", 312.75);
        MOCK_CRYPTO_PRICES.put("XRP", 0.62);
        MOCK_CRYPTO_PRICES.put("SOL", 98.40);
        MOCK_CRYPTO_PRICES.put("ADA", 0.58);
        MOCK_CRYPTO_PRICES.put("DOGE", 0.082);
    }

    private boolean useMockData = true;

    public ApiService() {
    }

    public void setUseMockData(boolean useMock) {
        this.useMockData = useMock;
    }

    public static void setAlphaVantageApiKey(String key) {
        alphaVantageKey = key;
    }

    /**
     * Get current price for any symbol (stock or crypto)
     */
    public double getCurrentPrice(String symbol) {
        symbol = symbol.toUpperCase();

        // Check cache first
        if (isCacheValid(symbol)) {
            return priceCache.get(symbol);
        }

        double price;
        if (useMockData) {
            price = getMockPrice(symbol);
        } else {
            // Try stock API first, then crypto
            price = fetchStockPrice(symbol);
            if (price <= 0) {
                price = fetchCryptoPrice(symbol);
            }
        }

        // Cache the result
        priceCache.put(symbol, price);
        cacheTimestamps.put(symbol, System.currentTimeMillis());

        return price;
    }

    /**
     * Get mock price with small random variation
     */
    private double getMockPrice(String symbol) {
        Random rand = new Random();
        double variation = 0.98 + (rand.nextDouble() * 0.04); // ±2% variation

        if (MOCK_STOCK_PRICES.containsKey(symbol)) {
            return MOCK_STOCK_PRICES.get(symbol) * variation;
        } else if (MOCK_CRYPTO_PRICES.containsKey(symbol)) {
            return MOCK_CRYPTO_PRICES.get(symbol) * variation;
        }

        // Unknown symbol - return random value
        return 50 + rand.nextDouble() * 100;
    }

    private double fetchStockPrice(String symbol) {
        try {
            String urlStr = String.format(
                    "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    symbol, alphaVantageKey);

            String response = httpGet(urlStr);
            if (response != null && response.contains("05. price")) {
                int start = response.indexOf("05. price") + 14;
                int end = response.indexOf("\"", start);
                return Double.parseDouble(response.substring(start, end));
            }
        } catch (Exception e) {
            System.err.println("Error fetching stock price: " + e.getMessage());
        }
        return -1;
    }
    private double fetchCryptoPrice(String symbol) {
        try {
            String coinId = mapToCoinGeckoId(symbol);
            String urlStr = String.format(
                    "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd",
                    coinId);

            String response = httpGet(urlStr);
            if (response != null && response.contains("usd")) {
                int start = response.indexOf("usd") + 5;
                int end = response.indexOf("}", start);
                if (end == -1)
                    end = response.length();
                String priceStr = response.substring(start, end).replaceAll("[^0-9.]", "");
                return Double.parseDouble(priceStr);
            }
        } catch (Exception e) {
            System.err.println("Error fetching crypto price: " + e.getMessage());
        }
        return -1;
    }

    private String mapToCoinGeckoId(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC":
                return "bitcoin";
            case "ETH":
                return "ethereum";
            case "BNB":
                return "binancecoin";
            case "XRP":
                return "ripple";
            case "SOL":
                return "solana";
            case "ADA":
                return "cardano";
            case "DOGE":
                return "dogecoin";
            default:
                return symbol.toLowerCase();
        }
    }

    private String httpGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (Exception e) {
            System.err.println("HTTP error: " + e.getMessage());
        }
        return null;
    }

    private boolean isCacheValid(String symbol) {
        if (!priceCache.containsKey(symbol))
            return false;
        Long timestamp = cacheTimestamps.get(symbol);
        if (timestamp == null)
            return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
    }
    public Map<String, Double> getHistoricalPrices(String symbol, int days) {
        Map<String, Double> history = new HashMap<>();
        double basePrice = getCurrentPrice(symbol);
        Random rand = new Random();

        java.time.LocalDate date = java.time.LocalDate.now();
        for (int i = 0; i < days; i++) {
            // Add some randomness to simulate historical prices
            double variation = 0.95 + (rand.nextDouble() * 0.10);
            history.put(date.minusDays(i).toString(), basePrice * variation);
        }

        return history;
    }
}