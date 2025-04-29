package com.example.a404.data.model;

import java.util.List; // Upewnij się, że ten import jest obecny, jeśli używasz listy osiągnięć

public class UserProfile {
    private String userId;
    private String username; // Dodane pole
    private int points;
    private String selectedLanguageCode;

    // Wymagany pusty konstruktor dla Firestore
    public UserProfile() {}

    // Zaktualizowany konstruktor
    public UserProfile(String userId, String username, int points, String selectedLanguageCode) {
        this.userId = userId;
        this.username = username; // Ustawienie nazwy użytkownika
        this.points = points;
        this.selectedLanguageCode = selectedLanguageCode;
    }

    // Gettery i Settery
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; } // Getter dla username
    public void setUsername(String username) { this.username = username; } // Setter dla username

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getSelectedLanguageCode() { return selectedLanguageCode; }
    public void setSelectedLanguageCode(String selectedLanguageCode) { this.selectedLanguageCode = selectedLanguageCode; }

    // Gettery i settery dla innych pól, jeśli istnieją
}