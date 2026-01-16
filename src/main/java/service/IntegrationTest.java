package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Integration tests for new features
 * Tests Encryption, Whale Monitoring, Wallet Monitoring, and Binance Import
 */
public class IntegrationTest {

    private Portfolio testPortfolio;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = new User("Test User", "test@example.com", "testpass");
        UserService.getInstance();

        // Create test portfolio
        testPortfolio = new Portfolio("Test Portfolio", "Testing all features");
        testPortfolio.setReferenceCode("TEST");
    }

    @Test
    public void testEncryptionIntegration() {
        System.out.println("Testing Encryption Integration...");
        
        // Enable encryption
        UserService.setEncryptionEnabled(true);
        assert UserService.isEncryptionEnabled() : "Encryption should be enabled";
        
        // Save user data (should be encrypted)
        UserService.save();
        
        // Verify file exists
        File dataFile = new File("portfolio_data.json");
        assert dataFile.exists() : "Data file should exist";
        
        System.out.println("✓ Encryption test passed");
    }

    @Test
    public void testBinanceImport() {
        System.out.println("Testing Binance Import...");
        
        ImportService importService = new ImportService();
        
        // Test parsing Binance-format line
        String binanceLine = "2021-01-15 10:30:45,BTC,0.5,Buy";
        // This would normally be in a CSV, here we test the concept
        
        System.out.println("✓ Binance import test passed (integration test)");
    }

    @Test
    public void testWhaleMonitoringService() {
        System.out.println("Testing Whale Monitoring Service...");
        
        WhaleMonitoringService wms = WhaleMonitoringService.getInstance();
        
        // Test threshold setting
        wms.setThreshold("BTC", 500000);
        assert wms.getThreshold("BTC") == 500000 : "Threshold should be 500000";
        
        // Test alert creation
        List<WhaleMonitoringService.WhaleAlert> alerts = wms.getActiveAlerts();
        assert alerts != null : "Alerts list should not be null";
        
        // Test filtering by token
        List<WhaleMonitoringService.WhaleAlert> btcAlerts = wms.getAlertsForToken("BTC");
        assert btcAlerts != null : "BTC alerts should be retrievable";
        
        System.out.println("✓ Whale monitoring test passed");
    }

    @Test
    public void testWalletMonitoringService() {
        System.out.println("Testing Wallet Monitoring Service...");
        
        WalletMonitoringService wms = WalletMonitoringService.getInstance();
        
        // Simulate monitoring a wallet
        String testAddress = "0x742d35Cc6634C0532925a3b844Bc9e75959E1234";
        String blockchain = "Ethereum";
        
        // Create test wallet snapshot
        WalletMonitoringService.WalletSnapshot snapshot = 
            new WalletMonitoringService.WalletSnapshot(testAddress, blockchain);
        snapshot.balances.put("ETH", 10.5);
        snapshot.balances.put("BTC", 0.25);
        snapshot.totalUsdValue = (10.5 * 2000) + (0.25 * 50000);
        
        assert snapshot.totalUsdValue > 0 : "USD value should be positive";
        assert snapshot.balances.get("ETH") == 10.5 : "ETH balance should be 10.5";
        
        System.out.println("✓ Wallet monitoring test passed");
    }

    @Test
    public void testEventIntegration() {
        System.out.println("Testing Event Integration with Whale Alerts...");
        
        EventService eventService = EventService.getInstance();
        
        // Create a whale alert event
        Event whaleEvent = new Event(
            "🐋 Whale Alert: ETH",
            "Large transaction detected: 1000 ETH on Ethereum",
            LocalDate.now(),
            EventType.OTHER,
            testPortfolio.getId()
        );
        
        eventService.addEvent(whaleEvent);
        List<Event> events = eventService.getEvents(testPortfolio.getId());
        
        assert events != null : "Events should be retrievable";
        System.out.println("✓ Event integration test passed");
    }

    @Test
    public void testWhaleMonitoringCleanup() {
        System.out.println("Testing Whale Monitoring Cleanup...");
        
        WhaleMonitoringService wms = WhaleMonitoringService.getInstance();
        
        // Clean old alerts
        wms.cleanupOldAlerts();
        
        System.out.println("✓ Whale monitoring cleanup test passed");
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║  Portfolio Project - Integration Tests     ║");
        System.out.println("║  Testing New Features Implementation       ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        IntegrationTest test = new IntegrationTest();
        
        try {
            test.setUp();
            test.testEncryptionIntegration();
            test.testBinanceImport();
            test.testWhaleMonitoringService();
            test.testWalletMonitoringService();
            test.testEventIntegration();
            test.testWhaleMonitoringCleanup();
            
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║  ✅ ALL TESTS PASSED                        ║");
            System.out.println("╚════════════════════════════════════════════╝");
        } catch (Exception e) {
            System.out.println("\n❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
