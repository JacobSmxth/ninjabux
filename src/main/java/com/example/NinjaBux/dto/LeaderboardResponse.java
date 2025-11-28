package com.example.NinjaBux.dto;

import java.util.List;

public class LeaderboardResponse {
    private List<LeaderboardEntry> topEarners;
    private List<LeaderboardEntry> topSpenders;
    private List<LeaderboardEntry> mostImproved; // Weekly lessons completed
    private List<LeaderboardEntry> streakLeaders; // Consecutive sessions
    private String message; // Optional message for empty states

    public LeaderboardResponse() {}

    public LeaderboardResponse(List<LeaderboardEntry> topEarners, List<LeaderboardEntry> topSpenders) {
        this.topEarners = topEarners;
        this.topSpenders = topSpenders;
    }

    public LeaderboardResponse(List<LeaderboardEntry> topEarners, List<LeaderboardEntry> topSpenders,
                               List<LeaderboardEntry> mostImproved, List<LeaderboardEntry> streakLeaders) {
        this.topEarners = topEarners;
        this.topSpenders = topSpenders;
        this.mostImproved = mostImproved;
        this.streakLeaders = streakLeaders;
    }

    public List<LeaderboardEntry> getTopEarners() {
        return topEarners;
    }
    public void setTopEarners(List<LeaderboardEntry> topEarners) {
        this.topEarners = topEarners;
    }
    public List<LeaderboardEntry> getTopSpenders() {
        return topSpenders;
    }
    public void setTopSpenders(List<LeaderboardEntry> topSpenders) {
        this.topSpenders = topSpenders;
    }

    public List<LeaderboardEntry> getMostImproved() {
        return mostImproved;
    }
    public void setMostImproved(List<LeaderboardEntry> mostImproved) {
        this.mostImproved = mostImproved;
    }

    public List<LeaderboardEntry> getStreakLeaders() {
        return streakLeaders;
    }
    public void setStreakLeaders(List<LeaderboardEntry> streakLeaders) {
        this.streakLeaders = streakLeaders;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
