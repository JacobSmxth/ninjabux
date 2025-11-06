package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.enums.BeltType;
import java.util.List;

public class LeaderboardEntry {
    private Long ninjaId;
    private String firstName;
    private String lastName;
    private String username;
    private BeltType currentBeltType;
    private int totalBuxEarned;
    private int totalBuxSpent;
    private int rank;
    private boolean isTopEarner;
    private boolean isTopSpender;
    private List<AchievementProgressDTO> topAchievements; // Top 3 achievements for display
    private AchievementProgressDTO leaderboardBadge; // Single badge to display on leaderboard (highest rarity)

    public LeaderboardEntry() {}

    public LeaderboardEntry(Long ninjaId, String firstName, String lastName, String username,
                           BeltType currentBeltType, int totalBuxEarned, int totalBuxSpent, int rank) {
        this.ninjaId = ninjaId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.currentBeltType = currentBeltType;
        this.totalBuxEarned = totalBuxEarned;
        this.totalBuxSpent = totalBuxSpent;
        this.rank = rank;
        this.isTopEarner = false;
        this.isTopSpender = false;
    }

    public Long getNinjaId() {
        return ninjaId;
    }
    public void setNinjaId(Long ninjaId) {
        this.ninjaId = ninjaId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public BeltType getCurrentBeltType() {
        return currentBeltType;
    }
    public void setCurrentBeltType(BeltType currentBeltType) {
        this.currentBeltType = currentBeltType;
    }
    public int getTotalBuxEarned() {
        return totalBuxEarned;
    }
    public void setTotalBuxEarned(int totalBuxEarned) {
        this.totalBuxEarned = totalBuxEarned;
    }

    public int getTotalBuxSpent() {
        return totalBuxSpent;
    }
    public void setTotalBuxSpent(int totalBuxSpent) {
        this.totalBuxSpent = totalBuxSpent;
    }
    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isTopEarner() {
        return isTopEarner;
    }
    public void setTopEarner(boolean topEarner) {
        isTopEarner = topEarner;
    }
    public boolean isTopSpender() {
        return isTopSpender;
    }
    public void setTopSpender(boolean topSpender) {
        isTopSpender = topSpender;
    }

    public List<AchievementProgressDTO> getTopAchievements() {
        return topAchievements;
    }
    public void setTopAchievements(List<AchievementProgressDTO> topAchievements) {
        this.topAchievements = topAchievements;
    }
    public AchievementProgressDTO getLeaderboardBadge() {
        return leaderboardBadge;
    }
    public void setLeaderboardBadge(AchievementProgressDTO leaderboardBadge) {
        this.leaderboardBadge = leaderboardBadge;
    }
}
