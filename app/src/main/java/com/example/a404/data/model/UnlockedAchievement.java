// Ścieżka: app/java/com/example/a404/data/model/UnlockedAchievement.java
package com.example.a404.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp; // Ważny import!

public class UnlockedAchievement {
    private String achievementId; // ID odblokowanego osiągnięcia (powinno pasować do Achievement.id)

    @ServerTimestamp // Ta adnotacja sprawi, że Firebase ustawi czas serwera przy tworzeniu dokumentu
    private Timestamp unlockedAt; // Data i czas odblokowania

    // Konstruktor bezargumentowy wymagany przez Firestore
    public UnlockedAchievement() {}

    // Konstruktor do tworzenia instancji przed zapisem do Firestore
    public UnlockedAchievement(String achievementId) {
        this.achievementId = achievementId;
        // unlockedAt zostanie ustawione przez serwer
    }

    // Gettery
    public String getAchievementId() { return achievementId; }
    public Timestamp getUnlockedAt() { return unlockedAt; }

    // Settery (głównie dla Firestore)
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }
}