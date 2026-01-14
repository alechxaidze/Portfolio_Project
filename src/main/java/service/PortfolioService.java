package service;

import model.Asset;
import model.Portfolio;
import model.Transaction;
import model.User;

import java.time.LocalDateTime;

public class PortfolioService {

    public PortfolioService() {
    }

    public void createPortfolio(String name) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            Portfolio portfolio = new Portfolio(name);
            user.addPortfolio(portfolio);
            UserService.save();
        }
    }

    public void createPortfolio(String name, String description) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            Portfolio portfolio = new Portfolio(name, description);
            user.addPortfolio(portfolio);
            UserService.save();
        }
    }

    public void addAssetToPortfolio(Portfolio portfolio, Asset asset, double price) {
        portfolio.addAsset(asset);
        // Record transaction
        Transaction tx = new Transaction(asset.getSymbol(), "BUY", asset.getQuantity(), price, LocalDateTime.now());
        portfolio.getTransactionHistory().add(tx);
        // Record snapshot
        portfolio.recordSnapshot();
        UserService.save();
    }

    public void removeAssetFromPortfolio(Portfolio portfolio, Asset asset, double price) {
        portfolio.removeAsset(asset);
        Transaction tx = new Transaction(asset.getSymbol(), "SELL", asset.getQuantity(), price, LocalDateTime.now());
        portfolio.getTransactionHistory().add(tx);
        portfolio.recordSnapshot();
        UserService.save();
    }

    public double calculateTotalValue(Portfolio portfolio) {
        return portfolio.getTotalValue();
    }

    public double calculateProfitLoss(Portfolio portfolio) {
        return portfolio.getProfitLoss();
    }

    public Portfolio clonePortfolio(Portfolio original, String newName) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            Portfolio cloned = original.clone(newName);
            user.addPortfolio(cloned);
            UserService.save();
            return cloned;
        }
        return null;
    }

    public void deletePortfolio(Portfolio portfolio) {
        User user = UserService.getCurrentUser();
        if (user != null) {
            user.removePortfolio(portfolio);
            UserService.save();
        }
    }
}