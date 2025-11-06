package com.example.NinjaBux.domain.enums;

public enum AchievementCategory {
    PROGRESS("Progress", "Achievements for advancing through the curriculum"),
    QUIZ("Quiz Champion", "Achievements for quiz performance"),
    PURCHASE("Shop Master", "Achievements for shop activity"),
    STREAK("Consistency", "Achievements for regular participation"),
    SOCIAL("Social", "Achievements for community participation"),
    SPECIAL("Special", "Unique and rare achievements"),
    VETERAN("Veteran", "Achievements for legacy milestones"),
    CONSISTENCY("Consistency", "Achievements for consistent attendance"),
    PROJECT_MILESTONE("Project Milestone", "Achievements for dojo-approved tasks"),
    BELT_GATED("Belt Gated", "Achievements requiring specific belt levels"),
    FOCUS_STREAK("Focus Streak", "Achievements for maintaining focus");

    private final String displayName;
    private final String description;

    AchievementCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }
}
