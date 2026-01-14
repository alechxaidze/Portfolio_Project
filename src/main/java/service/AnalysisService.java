package service;

import model.*;
import java.time.LocalDate;
import java.util.*;
public class AnalysisService {

    private final ApiService apiService;

    public AnalysisService(ApiService apiService) {
        this.apiService = apiService;
    }

    public double getTotalProfitLoss(User user) {
        return user.getPortfolios().stream()
                .mapToDouble(Portfolio::getProfitLoss)
                .sum();
    }

    public double getOverallReturnPercent(User user) {
        double totalCost = user.getPortfolios().stream()
                .mapToDouble(Portfolio::getTotalCost)
                .sum();
        double totalValue = user.getPortfolios().stream()
                .mapToDouble(Portfolio::getTotalValue)
                .sum();

        return totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
    }
    public Map<AssetType, Double> getAssetAllocation(Portfolio portfolio) {
        Map<AssetType, Double> allocation = new HashMap<>();
        double totalValue = portfolio.getTotalValue();

        if (totalValue == 0)
            return allocation;

        for (Asset asset : portfolio.getAssets()) {
            AssetType type = asset.getType();
            double value = asset.getValue();
            allocation.merge(type, value / totalValue * 100, Double::sum);
        }

        return allocation;
    }
    public List<Asset> getTopPerformers(Portfolio portfolio, int limit) {
        List<Asset> assets = new ArrayList<>(portfolio.getAssets());
        assets.sort((a, b) -> Double.compare(b.getProfitLoss(), a.getProfitLoss()));
        return assets.subList(0, Math.min(limit, assets.size()));
    }

    public List<Asset> getWorstPerformers(Portfolio portfolio, int limit) {
        List<Asset> assets = new ArrayList<>(portfolio.getAssets());
        assets.sort(Comparator.comparingDouble(Asset::getProfitLoss));
        return assets.subList(0, Math.min(limit, assets.size()));
    }
    public double estimateCapitalGainsTax(Portfolio portfolio) {
        double profitLoss = portfolio.getProfitLoss();
        if (profitLoss <= 0)
            return 0;

        return profitLoss * 0.15;
    }
    public boolean wasProfitableForPeriod(Portfolio portfolio, LocalDate start, LocalDate end) {
        List<PortfolioSnapshot> snapshots = portfolio.getSnapshots();

        PortfolioSnapshot startSnapshot = null;
        PortfolioSnapshot endSnapshot = null;

        for (PortfolioSnapshot snap : snapshots) {
            if (!snap.getDate().isBefore(start)
                    && (startSnapshot == null || snap.getDate().isBefore(startSnapshot.getDate()))) {
                startSnapshot = snap;
            }
            if (!snap.getDate().isAfter(end)
                    && (endSnapshot == null || snap.getDate().isAfter(endSnapshot.getDate()))) {
                endSnapshot = snap;
            }
        }

        if (startSnapshot == null || endSnapshot == null) {
            return portfolio.getProfitLoss() > 0;
        }

        return endSnapshot.getTotalValue() > startSnapshot.getTotalValue();
    }

    public Map<String, Integer> getProfitabilityStats(Portfolio portfolio) {
        Map<String, Integer> stats = new HashMap<>();
        int profitable = 0;
        int deficit = 0;

        List<PortfolioSnapshot> snapshots = portfolio.getSnapshots();
        double previousValue = 0;

        for (PortfolioSnapshot snap : snapshots) {
            if (previousValue > 0) {
                if (snap.getTotalValue() > previousValue) {
                    profitable++;
                } else if (snap.getTotalValue() < previousValue) {
                    deficit++;
                }
            }
            previousValue = snap.getTotalValue();
        }

        stats.put("profitable", profitable);
        stats.put("deficit", deficit);
        stats.put("neutral", Math.max(0, snapshots.size() - profitable - deficit - 1));

        return stats;
    }
    public double calculateVolatility(Portfolio portfolio) {
        List<PortfolioSnapshot> snapshots = portfolio.getSnapshots();
        if (snapshots.size() < 2)
            return 0;

        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < snapshots.size(); i++) {
            double prev = snapshots.get(i - 1).getTotalValue();
            double curr = snapshots.get(i).getTotalValue();
            if (prev > 0) {
                returns.add((curr - prev) / prev);
            }
        }

        if (returns.isEmpty())
            return 0;

        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance) * 100; // Return as percentage
    }
    public Map<String, Object> getPortfolioSummary(Portfolio portfolio) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalValue", portfolio.getTotalValue());
        summary.put("totalCost", portfolio.getTotalCost());
        summary.put("profitLoss", portfolio.getProfitLoss());
        summary.put("profitLossPercent", portfolio.getProfitLossPercent());
        summary.put("assetCount", portfolio.getAssets().size());
        summary.put("estimatedTax", estimateCapitalGainsTax(portfolio));
        summary.put("allocation", getAssetAllocation(portfolio));

        if (!portfolio.getAssets().isEmpty()) {
            summary.put("topPerformer", getTopPerformers(portfolio, 1).get(0).getSymbol());
        }

        return summary;
    }
}
