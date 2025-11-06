package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "achievement_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ninja_id", "achievement_id"}))
public class AchievementProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ninja_id", nullable = false)
    private Ninja ninja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private boolean unlocked = false;

    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private int progressValue = 0;

    @Column(nullable = false)
    private boolean seen = false;

    @Column(nullable = false)
    private boolean manuallyAwarded = false;

    private String awardedBy;

    @Column(nullable = false)
    private boolean isLeaderboardBadge = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AchievementProgress() {
    }

    public AchievementProgress(Ninja ninja, Achievement achievement) {
        this.ninja = ninja;
        this.achievement = achievement;
        this.unlocked = false;
        this.progressValue = 0;
        this.seen = false;
        this.manuallyAwarded = false;
    }

    public void unlock(boolean manuallyAwarded, String awardedBy) {
        this.unlocked = true;
        this.unlockedAt = LocalDateTime.now();
        this.manuallyAwarded = manuallyAwarded;
        this.awardedBy = awardedBy;
        this.seen = false; // New unlock should show notification
    }

    // AI made doing this quicker
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Ninja getNinja() {
        return ninja;
    }
    public void setNinja(Ninja ninja) {
        this.ninja = ninja;
    }
    public Achievement getAchievement() {
        return achievement;
    }
    public void setAchievement(Achievement achievement) {
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

    @Override
    public String toString() {
        return "AchievementProgress{" +
                "id=" + id +
                ", ninjaId=" + (ninja != null ? ninja.getId() : null) +
                ", achievementId=" + (achievement != null ? achievement.getId() : null) +
                ", unlocked=" + unlocked +
                ", progressValue=" + progressValue +
                '}';
    }
}
