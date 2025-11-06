package com.example.NinjaBux.dto;

import java.util.List;
import java.util.Map;


public class AnalyticsResponse {
    private StallDetectionMetrics stallDetection;
    private EconomyHealthMetrics economyHealth;
    private EngagementMetrics engagement;
    private ItemPopularityMetrics itemPopularity;

    public StallDetectionMetrics getStallDetection() {
        return stallDetection;
    }
    public void setStallDetection(StallDetectionMetrics stallDetection) {
        this.stallDetection = stallDetection;
    }

    public EconomyHealthMetrics getEconomyHealth() {
        return economyHealth;
    }
    public void setEconomyHealth(EconomyHealthMetrics economyHealth) {
        this.economyHealth = economyHealth;
    }

    public EngagementMetrics getEngagement() {
        return engagement;
    }
    public void setEngagement(EngagementMetrics engagement) {
        this.engagement = engagement;
    }

    public ItemPopularityMetrics getItemPopularity() {
        return itemPopularity;
    }
    public void setItemPopularity(ItemPopularityMetrics itemPopularity) {
        this.itemPopularity = itemPopularity;
    }

    public static class StallDetectionMetrics {
        private List<StallAlert> stalledNinjas;
        private Map<Long, Integer> daysSinceLastProgress;
        private Map<Long, Double> progressTrend; // ninjaId -> trend (positive = improving, negative = declining)

        public List<StallAlert> getStalledNinjas() {
            return stalledNinjas;
        }
        public void setStalledNinjas(List<StallAlert> stalledNinjas) {
            this.stalledNinjas = stalledNinjas;
        }

        public Map<Long, Integer> getDaysSinceLastProgress() {
            return daysSinceLastProgress;
        }
        public void setDaysSinceLastProgress(Map<Long, Integer> daysSinceLastProgress) {
            this.daysSinceLastProgress = daysSinceLastProgress;
        }

        public Map<Long, Double> getProgressTrend() {
            return progressTrend;
        }

        public void setProgressTrend(Map<Long, Double> progressTrend) {
            this.progressTrend = progressTrend;
        }
    }

    public static class StallAlert {
        private Long ninjaId;
        private String ninjaName;
        private Integer daysStalled;
        private String lastProgressDate;

        public Long getNinjaId() {
            return ninjaId;
        }
        public void setNinjaId(Long ninjaId) {
            this.ninjaId = ninjaId;
        }

        public String getNinjaName() {
            return ninjaName;
        }
        public void setNinjaName(String ninjaName) {
            this.ninjaName = ninjaName;
        }

        public Integer getDaysStalled() {
            return daysStalled;
        }
        public void setDaysStalled(Integer daysStalled) {
            this.daysStalled = daysStalled;
        }

        public String getLastProgressDate() {
            return lastProgressDate;
        }
        public void setLastProgressDate(String lastProgressDate) {
            this.lastProgressDate = lastProgressDate;
        }
    }

    public static class EconomyHealthMetrics {
        private double totalBuxInCirculation;
        private double totalBuxEarned;
        private double totalBuxSpent;
        private double spendEarnRatio;
        private BalanceDistribution balanceDistribution;
        private Map<String, Integer> balanceByBelt;

        public double getTotalBuxInCirculation() {
            return totalBuxInCirculation;
        }
        public void setTotalBuxInCirculation(double totalBuxInCirculation) {
            this.totalBuxInCirculation = totalBuxInCirculation;
        }

        public double getTotalBuxEarned() {
            return totalBuxEarned;
        }
        public void setTotalBuxEarned(double totalBuxEarned) {
            this.totalBuxEarned = totalBuxEarned;
        }

        public double getTotalBuxSpent() {
            return totalBuxSpent;
        }
        public void setTotalBuxSpent(double totalBuxSpent) {
            this.totalBuxSpent = totalBuxSpent;
        }

        public double getSpendEarnRatio() {
            return spendEarnRatio;
        }
        public void setSpendEarnRatio(double spendEarnRatio) {
            this.spendEarnRatio = spendEarnRatio;
        }

        public BalanceDistribution getBalanceDistribution() {
            return balanceDistribution;
        }
        public void setBalanceDistribution(BalanceDistribution balanceDistribution) {
            this.balanceDistribution = balanceDistribution;
        }

        public Map<String, Integer> getBalanceByBelt() {
            return balanceByBelt;
        }
        public void setBalanceByBelt(Map<String, Integer> balanceByBelt) {
            this.balanceByBelt = balanceByBelt;
        }
    }

    public static class BalanceDistribution {
        private int zeroBalance;
        private int lowBalance;
        private int mediumBalance;
        private int highBalance;
        private int veryHighBalance;

        public int getZeroBalance() {
            return zeroBalance;
        }
        public void setZeroBalance(int zeroBalance) {
            this.zeroBalance = zeroBalance;
        }

        public int getLowBalance() {
            return lowBalance;
        }
        public void setLowBalance(int lowBalance) {
            this.lowBalance = lowBalance;
        }

        public int getMediumBalance() {
            return mediumBalance;
        }
        public void setMediumBalance(int mediumBalance) {
            this.mediumBalance = mediumBalance;
        }

        public int getHighBalance() {
            return highBalance;
        }
        public void setHighBalance(int highBalance) {
            this.highBalance = highBalance;
        }

        public int getVeryHighBalance() {
            return veryHighBalance;
        }
        public void setVeryHighBalance(int veryHighBalance) {
            this.veryHighBalance = veryHighBalance;
        }
    }

    public static class EngagementMetrics {
        private QuizMetrics quizMetrics;
        private ShopMetrics shopMetrics;
        private LeaderboardMetrics leaderboardMetrics;
        private AchievementMetrics achievementMetrics;

        public QuizMetrics getQuizMetrics() {
            return quizMetrics;
        }
        public void setQuizMetrics(QuizMetrics quizMetrics) {
            this.quizMetrics = quizMetrics;
        }

        public ShopMetrics getShopMetrics() {
            return shopMetrics;
        }
        public void setShopMetrics(ShopMetrics shopMetrics) {
            this.shopMetrics = shopMetrics;
        }

        public LeaderboardMetrics getLeaderboardMetrics() {
            return leaderboardMetrics;
        }
        public void setLeaderboardMetrics(LeaderboardMetrics leaderboardMetrics) {
            this.leaderboardMetrics = leaderboardMetrics;
        }

        public AchievementMetrics getAchievementMetrics() {
            return achievementMetrics;
        }
        public void setAchievementMetrics(AchievementMetrics achievementMetrics) {
            this.achievementMetrics = achievementMetrics;
        }
    }

    public static class QuizMetrics {
        private int totalQuestions;
        private int totalAnswers;
        private double averageAccuracy;
        private int participantsThisWeek;
        private double participationRate;

        public int getTotalQuestions() {
            return totalQuestions;
        }
        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }
        public void setTotalAnswers(int totalAnswers) {
            this.totalAnswers = totalAnswers;
        }

        public double getAverageAccuracy() {
            return averageAccuracy;
        }
        public void setAverageAccuracy(double averageAccuracy) {
            this.averageAccuracy = averageAccuracy;
        }

        public int getParticipantsThisWeek() {
            return participantsThisWeek;
        }
        public void setParticipantsThisWeek(int participantsThisWeek) {
            this.participantsThisWeek = participantsThisWeek;
        }

        public double getParticipationRate() {
            return participationRate;
        }
        public void setParticipationRate(double participationRate) {
            this.participationRate = participationRate;
        }
    }

    public static class ShopMetrics {
        private int totalPurchases;
        private int totalPurchasesThisWeek;
        private double repeatPurchaseRate;
        private int averagePurchaseValue;
        private int uniqueShoppersThisWeek;

        public int getTotalPurchases() {
            return totalPurchases;
        }
        public void setTotalPurchases(int totalPurchases) {
            this.totalPurchases = totalPurchases;
        }

        public int getTotalPurchasesThisWeek() {
            return totalPurchasesThisWeek;
        }
        public void setTotalPurchasesThisWeek(int totalPurchasesThisWeek) {
            this.totalPurchasesThisWeek = totalPurchasesThisWeek;
        }

        public double getRepeatPurchaseRate() {
            return repeatPurchaseRate;
        }
        public void setRepeatPurchaseRate(double repeatPurchaseRate) {
            this.repeatPurchaseRate = repeatPurchaseRate;
        }

        public int getAveragePurchaseValue() {
            return averagePurchaseValue;
        }
        public void setAveragePurchaseValue(int averagePurchaseValue) {
            this.averagePurchaseValue = averagePurchaseValue;
        }

        public int getUniqueShoppersThisWeek() {
            return uniqueShoppersThisWeek;
        }
        public void setUniqueShoppersThisWeek(int uniqueShoppersThisWeek) {
            this.uniqueShoppersThisWeek = uniqueShoppersThisWeek;
        }
    }

    public static class LeaderboardMetrics {
        private int totalViews;
        private int viewsThisWeek;
        private double averageViewsPerNinja;

        public int getTotalViews() {
            return totalViews;
        }
        public void setTotalViews(int totalViews) {
            this.totalViews = totalViews;
        }

        public int getViewsThisWeek() {
            return viewsThisWeek;
        }
        public void setViewsThisWeek(int viewsThisWeek) {
            this.viewsThisWeek = viewsThisWeek;
        }

        public double getAverageViewsPerNinja() {
            return averageViewsPerNinja;
        }
        public void setAverageViewsPerNinja(double averageViewsPerNinja) {
            this.averageViewsPerNinja = averageViewsPerNinja;
        }
    }

    public static class AchievementMetrics {
        private int totalUnlocked;
        private int unlockedThisWeek;
        private Map<String, Integer> unlockedByRarity; // rarity -> count
        private double averageAchievementsPerNinja;

        public int getTotalUnlocked() {
            return totalUnlocked;
        }
        public void setTotalUnlocked(int totalUnlocked) {
            this.totalUnlocked = totalUnlocked;
        }

        public int getUnlockedThisWeek() {
            return unlockedThisWeek;
        }
        public void setUnlockedThisWeek(int unlockedThisWeek) {
            this.unlockedThisWeek = unlockedThisWeek;
        }

        public Map<String, Integer> getUnlockedByRarity() {
            return unlockedByRarity;
        }
        public void setUnlockedByRarity(Map<String, Integer> unlockedByRarity) {
            this.unlockedByRarity = unlockedByRarity;
        }

        public double getAverageAchievementsPerNinja() {
            return averageAchievementsPerNinja;
        }
        public void setAverageAchievementsPerNinja(double averageAchievementsPerNinja) {
            this.averageAchievementsPerNinja = averageAchievementsPerNinja;
        }
    }

    public static class ItemPopularityMetrics {
        private List<PopularItem> mostPopularItems;
        private List<PopularItem> leastPopularItems;

        public List<PopularItem> getMostPopularItems() {
            return mostPopularItems;
        }
        public void setMostPopularItems(List<PopularItem> mostPopularItems) {
            this.mostPopularItems = mostPopularItems;
        }

        public List<PopularItem> getLeastPopularItems() {
            return leastPopularItems;
        }
        public void setLeastPopularItems(List<PopularItem> leastPopularItems) {
            this.leastPopularItems = leastPopularItems;
        }
    }

    public static class PopularItem {
        private Long itemId;
        private String itemName;
        private int purchaseCount;
        private int revenue;
        private double conversionRate;

        public Long getItemId() {
            return itemId;
        }
        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public int getPurchaseCount() {
            return purchaseCount;
        }
        public void setPurchaseCount(int purchaseCount) {
            this.purchaseCount = purchaseCount;
        }

        public int getRevenue() {
            return revenue;
        }
        public void setRevenue(int revenue) {
            this.revenue = revenue;
        }

        public double getConversionRate() {
            return conversionRate;
        }
        public void setConversionRate(double conversionRate) {
            this.conversionRate = conversionRate;
        }
    }
}

