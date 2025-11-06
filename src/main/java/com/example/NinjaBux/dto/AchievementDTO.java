package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.Achievement;
import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.domain.enums.BadgeRarity;

import java.time.LocalDateTime;

public class AchievementDTO {
    private Long id;
    private String name;
    private String description;
    private AchievementCategory category;
    private BadgeRarity rarity;
    private String icon;
    private int buxReward;
    private boolean manualOnly;
    private String unlockCriteria;
    private boolean active;
    private boolean hidden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AchievementDTO() {
    }

    public AchievementDTO(Achievement achievement) {
        this.id = achievement.getId();
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.category = achievement.getCategory();
        this.rarity = achievement.getRarity();
        this.icon = achievement.getIcon();
        this.buxReward = achievement.getBuxReward();
        this.manualOnly = achievement.isManualOnly();
        this.unlockCriteria = achievement.getUnlockCriteria();
        this.active = achievement.isActive();
        this.hidden = achievement.isHidden();
        this.createdAt = achievement.getCreatedAt();
        this.updatedAt = achievement.getUpdatedAt();
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public AchievementCategory getCategory() {
        return category;
    }
    public void setCategory(AchievementCategory category) {
        this.category = category;
    }

    public BadgeRarity getRarity() {
        return rarity;
    }
    public void setRarity(BadgeRarity rarity) {
        this.rarity = rarity;
    }

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getBuxReward() {
        return buxReward;
    }
    public void setBuxReward(int buxReward) {
        this.buxReward = buxReward;
    }

    public boolean isManualOnly() {
        return manualOnly;
    }
    public void setManualOnly(boolean manualOnly) {
        this.manualOnly = manualOnly;
    }

    public String getUnlockCriteria() {
        return unlockCriteria;
    }
    public void setUnlockCriteria(String unlockCriteria) {
        this.unlockCriteria = unlockCriteria;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
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
