package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.*;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.dto.AnalyticsResponse;
import com.example.NinjaBux.repository.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

  private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

  @Autowired private NinjaRepository ninjaRepository;

  @Autowired private ProgressHistoryRepository progressHistoryRepository;

  @Autowired private PurchaseRepository purchaseRepository;

  @Autowired private ShopItemRepository shopItemRepository;

  @Autowired(required = false)
  private AchievementProgressRepository achievementProgressRepository;

  @Autowired private LedgerService ledgerService;


  public AnalyticsResponse getAnalytics() {
    AnalyticsResponse response = new AnalyticsResponse();

    try {
      response.setStallDetection(calculateStallDetection());
    } catch (Exception e) {
      logger.error("Error calculating stall detection: {}", e.getMessage(), e);
      response.setStallDetection(new AnalyticsResponse.StallDetectionMetrics());
    }

    try {
      response.setEconomyHealth(calculateEconomyHealth());
    } catch (Exception e) {
      logger.error("Error calculating economy health: {}", e.getMessage(), e);
      response.setEconomyHealth(new AnalyticsResponse.EconomyHealthMetrics());
    }

    try {
      response.setEngagement(calculateEngagement());
    } catch (Exception e) {
      logger.error("Error calculating engagement: {}", e.getMessage(), e);
      response.setEngagement(new AnalyticsResponse.EngagementMetrics());
    }

    try {
      response.setItemPopularity(calculateItemPopularity());
    } catch (Exception e) {
      logger.error("Error calculating item popularity: {}", e.getMessage(), e);
      response.setItemPopularity(new AnalyticsResponse.ItemPopularityMetrics());
    }

    return response;
  }

  private AnalyticsResponse.StallDetectionMetrics calculateStallDetection() {
    AnalyticsResponse.StallDetectionMetrics metrics = new AnalyticsResponse.StallDetectionMetrics();

    List<AnalyticsResponse.StallAlert> stalledNinjas = new ArrayList<>();
    Map<Long, Integer> daysSinceLastProgress = new HashMap<>();
    Map<Long, Double> progressTrend = new HashMap<>();

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime twoWeeksAgo = now.minusWeeks(2);
    LocalDateTime oneWeekAgo = now.minusWeeks(1);

    List<Ninja> allNinjas = ninjaRepository.findAll();

    for (Ninja ninja : allNinjas) {
      List<ProgressHistory> recentHistory =
          progressHistoryRepository.findByNinjaOrderByTimestampDesc(ninja);

      if (recentHistory.isEmpty()) {
        LocalDateTime referenceDate = ninja.getLastProgressUpdate();
        if (referenceDate == null) {
          referenceDate = ninja.getCreatedAt();
        }
        if (referenceDate != null) {
          long daysSinceCreated = ChronoUnit.DAYS.between(referenceDate, now);
          if (daysSinceCreated > 7) {
            AnalyticsResponse.StallAlert alert = new AnalyticsResponse.StallAlert();
            alert.setNinjaId(ninja.getId());
            alert.setNinjaName(ninja.getFirstName() + " " + ninja.getLastName());
            alert.setDaysStalled((int) daysSinceCreated);
            alert.setLastProgressDate(referenceDate.toString());
            stalledNinjas.add(alert);
          }
          daysSinceLastProgress.put(ninja.getId(), (int) daysSinceCreated);
        } else {
          daysSinceLastProgress.put(ninja.getId(), 0);
        }
        continue;
      }

      LocalDateTime lastProgress = recentHistory.get(0).getTimestamp();
      long daysSince = ChronoUnit.DAYS.between(lastProgress, now);
      daysSinceLastProgress.put(ninja.getId(), (int) daysSince);

      if (daysSince >= 7) {
        AnalyticsResponse.StallAlert alert = new AnalyticsResponse.StallAlert();
        alert.setNinjaId(ninja.getId());
        alert.setNinjaName(ninja.getFirstName() + " " + ninja.getLastName());
        alert.setDaysStalled((int) daysSince);
        alert.setLastProgressDate(lastProgress.toString());
        stalledNinjas.add(alert);
      }

      int lessonsLastTwoWeeks =
          (int)
              recentHistory.stream()
                  .filter(h -> h.getTimestamp().isAfter(twoWeeksAgo))
                  .filter(h -> h.getEarningType() == ProgressHistory.EarningType.LEVEL_UP)
                  .count();

      int lessonsPreviousTwoWeeks =
          (int)
              recentHistory.stream()
                  .filter(h -> h.getTimestamp().isAfter(twoWeeksAgo.minusWeeks(2)))
                  .filter(h -> h.getTimestamp().isBefore(twoWeeksAgo))
                  .filter(h -> h.getEarningType() == ProgressHistory.EarningType.LEVEL_UP)
                  .count();

      double trend =
          lessonsPreviousTwoWeeks > 0
              ? ((double) lessonsLastTwoWeeks - lessonsPreviousTwoWeeks)
                  / lessonsPreviousTwoWeeks
                  * 100
              : lessonsLastTwoWeeks > 0 ? 100.0 : 0.0;

      progressTrend.put(ninja.getId(), trend);
    }

    stalledNinjas.sort((a, b) -> b.getDaysStalled().compareTo(a.getDaysStalled()));

    metrics.setStalledNinjas(stalledNinjas);
    metrics.setDaysSinceLastProgress(daysSinceLastProgress);
    metrics.setProgressTrend(progressTrend);

    return metrics;
  }

  private AnalyticsResponse.EconomyHealthMetrics calculateEconomyHealth() {
    AnalyticsResponse.EconomyHealthMetrics metrics = new AnalyticsResponse.EconomyHealthMetrics();

    List<Ninja> allNinjas = ninjaRepository.findAll();
    Map<Long, Integer> balances = ledgerService.getBuxBalances(allNinjas);

    int totalBuxInCirculation = balances.values().stream().mapToInt(Integer::intValue).sum();
    int totalBuxEarned = ledgerService.getTotalEarnedGlobal();
    int totalBuxSpent = ledgerService.getTotalSpentGlobal();

    double spendEarnRatio = totalBuxEarned > 0 ? (double) totalBuxSpent / totalBuxEarned : 0.0;

    AnalyticsResponse.BalanceDistribution distribution =
        new AnalyticsResponse.BalanceDistribution();
    for (int balance : balances.values()) {
      if (balance == 0) {
        distribution.setZeroBalance(distribution.getZeroBalance() + 1);
      } else if (balance <= 50) {
        distribution.setLowBalance(distribution.getLowBalance() + 1);
      } else if (balance <= 200) {
        distribution.setMediumBalance(distribution.getMediumBalance() + 1);
      } else if (balance <= 500) {
        distribution.setHighBalance(distribution.getHighBalance() + 1);
      } else {
        distribution.setVeryHighBalance(distribution.getVeryHighBalance() + 1);
      }
    }

    Map<String, Integer> balanceByBelt = new HashMap<>();
    Map<String, List<Integer>> balancesByBelt = new HashMap<>();
    for (Ninja ninja : allNinjas) {
      String belt = ninja.getCurrentBeltType().toString();
      int balance = balances.getOrDefault(ninja.getId(), 0);
      balancesByBelt.computeIfAbsent(belt, k -> new ArrayList<>()).add(balance);
    }

    for (Map.Entry<String, List<Integer>> entry : balancesByBelt.entrySet()) {
      double average = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
      balanceByBelt.put(entry.getKey(), (int) average);
    }

    metrics.setTotalBuxInCirculation(totalBuxInCirculation);
    metrics.setTotalBuxEarned(totalBuxEarned);
    metrics.setTotalBuxSpent(totalBuxSpent);
    metrics.setSpendEarnRatio(spendEarnRatio);
    metrics.setBalanceDistribution(distribution);
    metrics.setBalanceByBelt(balanceByBelt);

    return metrics;
  }

  private AnalyticsResponse.EngagementMetrics calculateEngagement() {
    AnalyticsResponse.EngagementMetrics metrics = new AnalyticsResponse.EngagementMetrics();

    metrics.setShopMetrics(calculateShopMetrics());
    metrics.setLeaderboardMetrics(calculateLeaderboardMetrics());
    metrics.setAchievementMetrics(calculateAchievementMetrics());

    return metrics;
  }

  private AnalyticsResponse.ShopMetrics calculateShopMetrics() {
    AnalyticsResponse.ShopMetrics metrics = new AnalyticsResponse.ShopMetrics();

    List<Purchase> allPurchases = purchaseRepository.findAll();
    LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

    int totalPurchases = allPurchases.size();

    int totalPurchasesThisWeek =
        (int) allPurchases.stream().filter(p -> p.getPurchaseDate().isAfter(weekAgo)).count();

    Map<Long, Long> purchaseCountsByNinja =
        allPurchases.stream()
            .collect(Collectors.groupingBy(p -> p.getNinja().getId(), Collectors.counting()));

    long repeatPurchasers =
        purchaseCountsByNinja.values().stream().filter(count -> count > 1).count();

    double repeatPurchaseRate =
        purchaseCountsByNinja.size() > 0
            ? (double) repeatPurchasers / purchaseCountsByNinja.size() * 100
            : 0.0;

    double averagePurchaseValue =
        totalPurchases > 0
            ? allPurchases.stream().mapToInt(Purchase::getPricePaid).average().orElse(0.0)
            : 0.0;

    int uniqueShoppersThisWeek =
        (int)
            allPurchases.stream()
                .filter(p -> p.getPurchaseDate().isAfter(weekAgo))
                .map(p -> p.getNinja().getId())
                .distinct()
                .count();

    metrics.setTotalPurchases(totalPurchases);
    metrics.setTotalPurchasesThisWeek(totalPurchasesThisWeek);
    metrics.setRepeatPurchaseRate(repeatPurchaseRate);
    metrics.setAveragePurchaseValue((int) averagePurchaseValue);
    metrics.setUniqueShoppersThisWeek(uniqueShoppersThisWeek);

    return metrics;
  }

  private AnalyticsResponse.LeaderboardMetrics calculateLeaderboardMetrics() {
    AnalyticsResponse.LeaderboardMetrics metrics = new AnalyticsResponse.LeaderboardMetrics();
    metrics.setTotalViews(0);
    metrics.setViewsThisWeek(0);
    metrics.setAverageViewsPerNinja(0.0);
    return metrics;
  }

  private AnalyticsResponse.AchievementMetrics calculateAchievementMetrics() {
    AnalyticsResponse.AchievementMetrics metrics = new AnalyticsResponse.AchievementMetrics();

    if (achievementProgressRepository == null) {
      metrics.setTotalUnlocked(0);
      metrics.setUnlockedThisWeek(0);
      metrics.setUnlockedByRarity(new HashMap<>());
      metrics.setAverageAchievementsPerNinja(0.0);
      return metrics;
    }

    List<AchievementProgress> allProgress = achievementProgressRepository.findAll();
    LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

    int totalUnlocked = (int) allProgress.stream().filter(AchievementProgress::isUnlocked).count();

    int unlockedThisWeek =
        (int)
            allProgress.stream()
                .filter(AchievementProgress::isUnlocked)
                .filter(p -> p.getUnlockedAt() != null && p.getUnlockedAt().isAfter(weekAgo))
                .count();

    Map<String, Integer> unlockedByRarity =
        allProgress.stream()
            .filter(AchievementProgress::isUnlocked)
            .collect(
                Collectors.groupingBy(
                    p -> p.getAchievement().getRarity().toString(),
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    int totalNinjas = (int) ninjaRepository.count();
    double averageAchievementsPerNinja =
        totalNinjas > 0 ? (double) totalUnlocked / totalNinjas : 0.0;

    metrics.setTotalUnlocked(totalUnlocked);
    metrics.setUnlockedThisWeek(unlockedThisWeek);
    metrics.setUnlockedByRarity(unlockedByRarity);
    metrics.setAverageAchievementsPerNinja(averageAchievementsPerNinja);

    return metrics;
  }

  private AnalyticsResponse.ItemPopularityMetrics calculateItemPopularity() {
    AnalyticsResponse.ItemPopularityMetrics metrics = new AnalyticsResponse.ItemPopularityMetrics();

    List<ShopItem> allItems = shopItemRepository.findAll();
    List<Purchase> allPurchases = purchaseRepository.findAll();

    List<AnalyticsResponse.PopularItem> popularItems = new ArrayList<>();

    for (ShopItem item : allItems) {
      List<Purchase> itemPurchases =
          allPurchases.stream()
              .filter(p -> p.getShopItem().getId().equals(item.getId()))
              .collect(Collectors.toList());

      AnalyticsResponse.PopularItem popularItem = new AnalyticsResponse.PopularItem();
      popularItem.setItemId(item.getId());
      popularItem.setItemName(item.getName());
      popularItem.setPurchaseCount(itemPurchases.size());
      popularItem.setRevenue(itemPurchases.stream().mapToInt(Purchase::getPricePaid).sum());
      popularItem.setConversionRate(0.0);

      popularItems.add(popularItem);
    }

    popularItems.sort((a, b) -> b.getPurchaseCount() - a.getPurchaseCount());

    List<AnalyticsResponse.PopularItem> mostPopular =
        popularItems.stream().limit(10).collect(Collectors.toList());

    List<AnalyticsResponse.PopularItem> leastPopular =
        popularItems.stream()
            .skip(Math.max(0, popularItems.size() - 10))
            .collect(Collectors.toList());

    metrics.setMostPopularItems(mostPopular);
    metrics.setLeastPopularItems(leastPopular);

    return metrics;
  }
}
