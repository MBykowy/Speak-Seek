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

    // === NOWE POLA DLA OSIĄGNIĘĆ ===
    private int lessonsCompletedCount; // Liczba wszystkich ukończonych lekcji
    private List<String> languagesStartedIds; // Lista ID języków, których naukę użytkownik rozpoczął

    // Wymagany pusty konstruktor dla Firestore
    public UserProfile() {
        // Inicjalizuj listy, aby uniknąć NullPointerException
        this.languagesStartedIds = new ArrayList<>();
        this.lessonsCompletedCount = 0; // Domyślna wartość
        earnedAchievementIds = new ArrayList<>();
    }

    // Zaktualizowany konstruktor
    public UserProfile(String userId, String username, int points, String selectedLanguageCode) {
        this.userId = userId;
        this.username = username; // Ustawienie nazwy użytkownika
        this.points = points;
        this.selectedLanguageCode = selectedLanguageCode;
        // Inicjalizuj nowe pola również tutaj, jeśli tworzysz nowy profil z wartościami początkowymi
        this.lessonsCompletedCount = 0;
        this.languagesStartedIds = new ArrayList<>();
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

    public String getUsername() { return username; } // Getter dla username
    public void setUsername(String username) { this.username = username; } // Setter dla username

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getSelectedLanguageCode() { return selectedLanguageCode; }
    public void setSelectedLanguageCode(String selectedLanguageCode) { this.selectedLanguageCode = selectedLanguageCode; }

    // === GETTERY I SETTERY DLA NOWYCH PÓL ===
    public int getLessonsCompletedCount() {
        return lessonsCompletedCount;
    }

    public void setLessonsCompletedCount(int lessonsCompletedCount) {
        this.lessonsCompletedCount = lessonsCompletedCount;
    }

    public List<String> getLanguagesStartedIds() {
        // Zwróć kopię, aby uniknąć modyfikacji z zewnątrz, lub upewnij się, że jest inicjalizowana
        return languagesStartedIds != null ? languagesStartedIds : new ArrayList<>();
    }

    public void setLanguagesStartedIds(List<String> languagesStartedIds) {
        this.languagesStartedIds = languagesStartedIds;
    }
}