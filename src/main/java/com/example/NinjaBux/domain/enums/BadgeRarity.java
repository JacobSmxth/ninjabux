package com.example.NinjaBux.domain.enums;

// found out I can store values IN the enum basically
public enum BadgeRarity {
    COMMON("Common", "#9CA3AF", "#6B7280"),
    RARE("Rare", "#3B82F6", "#2563EB"),
    EPIC("Epic", "#A855F7", "#9333EA"),
    LEGENDARY("Legendary", "#F59E0B", "#D97706");

    private final String displayName;
    private final String primaryColor;
    private final String secondaryColor;

    BadgeRarity(String displayName, String primaryColor, String secondaryColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getPrimaryColor() {
        return primaryColor;
    }
    public String getSecondaryColor() {
        return secondaryColor;
    }

    // for effects, may remove or do differently (claude had this idea)
    public String getGlowEffect() {
        return switch (this) {
            case COMMON -> "0 0 5px " + primaryColor;
            case RARE -> "0 0 10px " + primaryColor + ", 0 0 20px " + secondaryColor;
            case EPIC -> "0 0 15px " + primaryColor + ", 0 0 30px " + secondaryColor + ", 0 0 45px " + primaryColor;
            case LEGENDARY -> "0 0 20px " + primaryColor + ", 0 0 40px " + secondaryColor + ", 0 0 60px " + primaryColor + ", 0 0 80px " + secondaryColor;
        };
    }
}
