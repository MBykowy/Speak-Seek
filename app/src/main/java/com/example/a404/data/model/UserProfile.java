package com.example.a404.data.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String userId;
    private String username;
    private int points;
    private String selectedLanguageCode;
    private int currentStreak;
    private Timestamp lastActivityDate;
    private List<String> earnedAchievementIds;

    // Wymagany pusty konstruktor dla Firestore
    public UserProfile() {
        earnedAchievementIds = new ArrayList<>();
    }

    // Konstruktor
    public UserProfile(String userId, String username, int points, String selectedLanguageCode) {
        this.userId = userId;
        this.username = username;
        this.points = points;
        this.selectedLanguageCode = selectedLanguageCode;
        this.currentStreak = 0;
        this.lastActivityDate = Timestamp.now();
        this.earnedAchievementIds = new ArrayList<>();
    }

    // Dodajemy gettery i settery dla nowych pól
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public Timestamp getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(Timestamp lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public List<String> getEarnedAchievementIds() { return earnedAchievementIds; }
    public void setEarnedAchievementIds(List<String> earnedAchievementIds) { this.earnedAchievementIds = earnedAchievementIds; }

    // Istniejące gettery i settery
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getSelectedLanguageCode() { return selectedLanguageCode; }
    public void setSelectedLanguageCode(String selectedLanguageCode) { this.selectedLanguageCode = selectedLanguageCode; }
}