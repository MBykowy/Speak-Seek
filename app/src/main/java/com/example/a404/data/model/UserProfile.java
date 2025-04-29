package com.example.a404.data.model;

public class UserProfile {
    private String userId;
    private int points;
    private String selectedLanguageCode;
    private String name;

    // Wymagany pusty konstruktor dla Firestore
    public UserProfile() {}

    public UserProfile(String userId, String name, int points, String selectedLanguageCode) {
        this.userId = userId;
        this.points = points;
        this.name = name;
        this.selectedLanguageCode = selectedLanguageCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getName(){return name;}

    public void setName(){this.name = name;}

    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public void setSelectedLanguageCode(String selectedLanguageCode) {
        this.selectedLanguageCode = selectedLanguageCode;
    }
}