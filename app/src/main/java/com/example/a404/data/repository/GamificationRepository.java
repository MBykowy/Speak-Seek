package com.example.a404.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GamificationRepository {
    private static final String TAG = "GamificationRepository";
    private final FirebaseSource firebaseSource;

    public GamificationRepository(FirebaseSource firebaseSource) {
        this.firebaseSource = firebaseSource;
    }

    /**
     * Pobiera osiągnięcia na podstawie ich identyfikatorów
     */
    public LiveData<List<Achievement>> getAchievementsByIds(List<String> achievementIds) {
        MutableLiveData<List<Achievement>> achievementsLiveData = new MutableLiveData<>();

        if (achievementIds == null || achievementIds.isEmpty()) {
            achievementsLiveData.setValue(new ArrayList<>());
            return achievementsLiveData;
        }

        firebaseSource.getFirestore().collection("achievements")
                .whereIn("id", achievementIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Achievement> achievements = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Achievement achievement = document.toObject(Achievement.class);
                        if (achievement != null) {
                            // Upewniamy się, że ID jest ustawione
                            if (achievement.getId() == null) {
                                achievement.setId(document.getId());
                            }
                            achievements.add(achievement);
                        }
                    }
                    achievementsLiveData.setValue(achievements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania osiągnięć", e);
                    achievementsLiveData.setValue(new ArrayList<>());
                });

        return achievementsLiveData;
    }


    /**
     * Aktualizuje serię dni aktywności użytkownika
     */
    public void updateStreak(String userId, UserProfile userProfile) {
        if (userProfile == null || userId == null) {
            return;
        }

        Timestamp lastActivity = userProfile.getLastActivityDate();
        Timestamp now = Timestamp.now();
        int currentStreak = userProfile.getCurrentStreak();

        // Jeśli to pierwsza aktywność, po prostu ustawiamy serię na 1
        if (lastActivity == null) {
            updateStreakInFirestore(userId, 1, now);
            return;
        }

        Calendar lastActivityCal = Calendar.getInstance();
        lastActivityCal.setTime(lastActivity.toDate());
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now.toDate());

        // Resetuj godzinę, minuty i sekundy, aby porównać tylko daty
        resetTimeFields(lastActivityCal);
        resetTimeFields(nowCal);

        // Oblicz różnicę w dniach
        long diffInMillis = nowCal.getTimeInMillis() - lastActivityCal.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        int newStreak;

        if (diffInDays == 0) {
            // Ten sam dzień, seria się nie zmienia
            newStreak = currentStreak;
        } else if (diffInDays == 1) {
            // Kolejny dzień, seria rośnie o 1
            newStreak = currentStreak + 1;
        } else {
            // Przerwa w serii, zaczynamy od 1
            newStreak = 1;
        }

        updateStreakInFirestore(userId, newStreak, now);
    }

    private void resetTimeFields(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void updateStreakInFirestore(String userId, int newStreak, Timestamp timestamp) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentStreak", newStreak);
        updates.put("lastActivityDate", timestamp);

        firebaseSource.getFirestore().collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Zaktualizowano serię do: " + newStreak))
                .addOnFailureListener(e -> Log.e(TAG, "Błąd aktualizacji serii", e));
    }

    /**
     * Przyznaje osiągnięcie użytkownikowi
     */
    public void awardAchievement(String userId, String achievementId) {
        if (userId == null || achievementId == null) {
            return;
        }

        firebaseSource.getFirestore().collection("users")
                .document(userId)
                .update("earnedAchievementIds", com.google.firebase.firestore.FieldValue.arrayUnion(achievementId))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Przyznano osiągnięcie: " + achievementId))
                .addOnFailureListener(e -> Log.e(TAG, "Błąd przyznawania osiągnięcia", e));
    }

    /**
     * Sprawdza warunki osiągnięć i przyznaje je jeśli są spełnione
     */
    public void checkAchievements(String userId, UserProfile userProfile) {
        // Tu implementacja logiki sprawdzania warunków konkretnych osiągnięć
        // Na przykład sprawdzanie liczby punktów, długości serii, itp.

        // Przykład: Przyznanie osiągnięcia za pierwszą serię 3 dni
        if (userProfile.getCurrentStreak() == 3 &&
                (userProfile.getEarnedAchievementIds() == null ||
                        !userProfile.getEarnedAchievementIds().contains("achievement_first_streak"))) {
            awardAchievement(userId, "achievement_first_streak");
        }

        // Inne warunki osiągnięć...
    }
}