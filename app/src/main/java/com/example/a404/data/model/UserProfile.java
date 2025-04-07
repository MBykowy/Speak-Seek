package com.example.a404.data.model;

public class UserProfile {
    private String userId;
    private int points;
    private String selectedLanguageCode;

    // Konstruktor domyślny wymagany przez Firebase
    public UserProfile() {}

    public UserProfile(String userId, int points, String selectedLanguageCode) {
        this.userId = userId;
        this.points = points;
        this.selectedLanguageCode = selectedLanguageCode;
    }

    // Gettery i settery
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getSelectedLanguageCode() { return selectedLanguageCode; }
    public void setSelectedLanguageCode(String selectedLanguageCode) { this.selectedLanguageCode = selectedLanguageCode; }
}