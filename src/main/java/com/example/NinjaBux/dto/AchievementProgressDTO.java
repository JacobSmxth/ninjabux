package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.AchievementProgress;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


public class AchievementProgressDTO {
    private Long id;
    private Long ninjaId;
    private AchievementDTO achievement;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private int progressValue;
    private boolean seen;
    private boolean manuallyAwarded;
    private String awardedBy;
    private boolean isLeaderboardBadge;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AchievementProgressDTO() {
    }

    public AchievementProgressDTO(AchievementProgress progress) {
        this.id = progress.getId();
        this.ninjaId = progress.getNinja().getId();
        this.achievement = new AchievementDTO(progress.getAchievement());
        this.unlocked = progress.isUnlocked();
        this.unlockedAt = progress.getUnlockedAt();
        this.progressValue = progress.getProgressValue();
        this.seen = progress.isSeen();
        this.manuallyAwarded = progress.isManuallyAwarded();
        this.awardedBy = progress.getAwardedBy();
        this.isLeaderboardBadge = progress.isLeaderboardBadge();
        this.createdAt = progress.getCreatedAt();
        this.updatedAt = progress.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getNinjaId() {
        return ninjaId;
    }
    public void setNinjaId(Long ninjaId) {
        this.ninjaId = ninjaId;
    }

    public AchievementDTO getAchievement() {
        return achievement;
    }
    public void setAchievement(AchievementDTO achievement) {
        this.achievement = achievement;
    }
    public boolean isUnlocked() {
        return unlocked;
    }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }
    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
    public int getProgressValue() {
        return progressValue;
    }
    public void setProgressValue(int progressValue) {
        this.progressValue = progressValue;
    }

    public boolean isSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }
    public boolean isManuallyAwarded() {
        return manuallyAwarded;
    }
    public void setManuallyAwarded(boolean manuallyAwarded) {
        this.manuallyAwarded = manuallyAwarded;
    }

    public String getAwardedBy() {
        return awardedBy;
    }
    public void setAwardedBy(String awardedBy) {
        this.awardedBy = awardedBy;
    }
    @JsonProperty("isLeaderboardBadge")
    public boolean isLeaderboardBadge() {
        return isLeaderboardBadge;
    }
    public void setLeaderboardBadge(boolean leaderboardBadge) {
        isLeaderboardBadge = leaderboardBadge;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
