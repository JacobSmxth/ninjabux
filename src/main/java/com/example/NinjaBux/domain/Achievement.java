package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.domain.enums.BadgeRarity;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeRarity rarity;


    @Column(nullable = false)
    private String icon;


    @Column(nullable = false)
    private int buxReward;


    @Column(nullable = false)
    private boolean manualOnly;


    @Column(length = 2000)
    private String unlockCriteria;


    @Column(nullable = false)
    private boolean active = true;


    @Column(nullable = false)
    private boolean hidden = false;

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

    public Achievement() {
    }

    public Achievement(String name, String description, AchievementCategory category,
                      BadgeRarity rarity, String icon, int buxReward) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.rarity = rarity;
        this.icon = icon;
        this.buxReward = buxReward;
        this.manualOnly = false;
        this.active = true;
        this.hidden = false;
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

    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", rarity=" + rarity +
                ", buxReward=" + buxReward +
                ", active=" + active +
                '}';
    }
}
