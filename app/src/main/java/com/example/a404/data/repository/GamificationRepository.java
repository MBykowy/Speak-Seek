// Ścieżka: app/java/com/example/a404/data/repository/GamificationRepository.java
package com.example.a404.data.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer; // Dodaj ten import, jeśli będziesz usuwać observera

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UnlockedAchievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath; // Potrzebne dla whereIn na ID dokumentu

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger; // Do bezpiecznego zliczania

public class GamificationRepository {
    private static final String TAG = "GamificationRepository";
    private final FirebaseSource firebaseSource;
    private Context appContext;

    public GamificationRepository(FirebaseSource firebaseSource, Context applicationContext) {
        this.firebaseSource = firebaseSource;
        this.appContext = applicationContext;
    }

    public LiveData<List<Achievement>> getAchievementsByIds(List<String> achievementIds) {
        MutableLiveData<List<Achievement>> achievementsLiveData = new MutableLiveData<>();

        if (achievementIds == null || achievementIds.isEmpty()) {
            achievementsLiveData.setValue(new ArrayList<>());
            return achievementsLiveData;
        }

        Log.d(TAG, "Fetching achievements for IDs: " + achievementIds.toString());
        // Poprawione zapytanie używające FieldPath.documentId() dla whereIn
        // Zakłada, że 'achievementIds' zawiera faktyczne ID dokumentów
        // Uwaga: whereIn obsługuje maksymalnie 10 (lub 30 od niedawna, sprawdź dokumentację) argumentów w liście.
        // Jeśli lista jest dłuższa, trzeba ją podzielić na mniejsze części lub pobierać pojedynczo.
        // Dla prostoty zakładamy, że lista nie przekracza limitu Firestore.
        if (achievementIds.size() > 10) { // Firestore whereIn limit, może być 30
            Log.w(TAG, "Achievement ID list is larger than 10, whereIn query might fail or be inefficient. Consider batching.");
            // Dla bardzo dużych list, to podejście poniżej (pobieranie pojedynczo) jest bezpieczniejsze, choć mniej wydajne.
            // Poniższy kod jest przykładem pobierania pojedynczo, jeśli whereIn nie działa lub przekracza limit.
            List<Achievement> foundAchievements = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(achievementIds.size()); // Użyj AtomicInteger dla bezpieczeństwa wątków

            for (String id : achievementIds) {
                firebaseSource.getFirestore().collection("achievements").document(id).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Achievement achievement = documentSnapshot.toObject(Achievement.class);
                                if (achievement != null) {
                                    achievement.setId(documentSnapshot.getId());
                                    foundAchievements.add(achievement);
                                }
                            }
                            if (counter.decrementAndGet() == 0) {
                                achievementsLiveData.postValue(foundAchievements);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching achievement by ID: " + id, e);
                            if (counter.decrementAndGet() == 0) {
                                achievementsLiveData.postValue(foundAchievements); // Postuj to, co udało się zebrać
                            }
                        });
            }
            return achievementsLiveData;
        }

        // Jeśli lista jest krótka, spróbuj z whereIn(FieldPath.documentId(), ...)
        firebaseSource.getFirestore().collection("achievements")
                .whereIn(FieldPath.documentId(), achievementIds) // Użyj FieldPath.documentId()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Achievement> achievements = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Achievement achievement = document.toObject(Achievement.class);
                        if (achievement != null) {
                            achievement.setId(document.getId()); // Upewnij się, że ID jest ustawione
                            achievements.add(achievement);
                        }
                    }
                    achievementsLiveData.setValue(achievements);
                    Log.d(TAG, "Successfully fetched " + achievements.size() + " achievements by IDs.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania osiągnięć przez whereIn(documentId())", e);
                    achievementsLiveData.setValue(new ArrayList<>());
                });

        return achievementsLiveData;
    }


    public void updateStreak(String userId, UserProfile userProfile, FirebaseSource.FirestoreOperationCallback streakUpdateCallback) {
        if (userProfile == null || userId == null) {
            if (streakUpdateCallback != null) streakUpdateCallback.onCallback(false, new IllegalArgumentException("User or Profile is null"));
            return;
        }

        Timestamp lastActivity = userProfile.getLastActivityDate();
        Timestamp now = Timestamp.now();
        int currentStreak = userProfile.getCurrentStreak();

        if (lastActivity == null) {
            Log.d(TAG, "First activity for user: " + userId + ". Setting streak to 1.");
            updateStreakInFirestore(userId, 1, now, streakUpdateCallback);
            return;
        }

        Calendar lastActivityCal = Calendar.getInstance();
        lastActivityCal.setTime(lastActivity.toDate());
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now.toDate());

        resetTimeFields(lastActivityCal);
        resetTimeFields(nowCal);

        long diffInMillis = nowCal.getTimeInMillis() - lastActivityCal.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        int newStreak;

        if (diffInDays == 0) {
            Log.d(TAG, "Activity on the same day for user: " + userId + ". Streak remains: " + currentStreak);
            // Zaktualizuj tylko datę ostatniej aktywności, jeśli chcesz odnotować aktywność tego dnia.
            // Seria się nie zmienia.
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastActivityDate", now);
            firebaseSource.getFirestore().collection("users").document(userId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Updated lastActivityDate for same-day activity for user: " + userId);
                        if (streakUpdateCallback != null) streakUpdateCallback.onCallback(true, null); // Operacja "aktualizacji serii" (choć się nie zmieniła) zakończona
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update lastActivityDate for same-day activity for user: " + userId, e);
                        if (streakUpdateCallback != null) streakUpdateCallback.onCallback(false, e);
                    });
            return;
        } else if (diffInDays == 1) {
            newStreak = currentStreak + 1;
            Log.d(TAG, "Consecutive day activity for user: " + userId + ". New streak: " + newStreak);
        } else {
            newStreak = 1;
            Log.d(TAG, "Streak broken for user: " + userId + ". Resetting streak to 1.");
        }
        updateStreakInFirestore(userId, newStreak, now, streakUpdateCallback);
    }

    private void resetTimeFields(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void updateStreakInFirestore(String userId, int newStreak, Timestamp timestamp, FirebaseSource.FirestoreOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentStreak", newStreak);
        updates.put("lastActivityDate", timestamp);

        firebaseSource.getFirestore().collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Zaktualizowano serię do: " + newStreak + " dla użytkownika: " + userId);
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd aktualizacji serii dla użytkownika: " + userId, e);
                    if (callback != null) callback.onCallback(false, e);
                });
    }

    public void checkAndUnlockSpecificAchievements(String userId, UserProfile userProfile) {
        if (userProfile == null || userId == null) {
            Log.w(TAG, "Cannot check specific achievements, userProfile or userId is null.");
            return;
        }

        // Pobierz WSZYSTKIE definicje osiągnięć, aby dynamicznie sprawdzać warunki
        firebaseSource.getAllAchievementDefinitions((allDefinitions, defError) -> {
            if (defError != null || allDefinitions == null || allDefinitions.isEmpty()) {
                Log.e(TAG, "Could not fetch achievement definitions to check specific achievements.", defError);
                return;
            }

            // Pobierz już odblokowane osiągnięcia użytkownika
            firebaseSource.getUserUnlockedAchievements(userId, (unlockedList, unlError) -> {
                if (unlError != null) {
                    Log.e(TAG, "Error fetching unlocked achievements for specific check. User: " + userId, unlError);
                    return;
                }

                for (Achievement achievementDef : allDefinitions) {
                    if (!isAlreadyUnlocked(unlockedList, achievementDef.getId())) {
                        boolean shouldUnlockThis = false;
                        String achievementIdToUnlock = achievementDef.getId();

                        // Sprawdzanie warunków na podstawie triggerType
                        switch (achievementDef.getTriggerType()) {
                            case "STREAK_DAYS":
                                if (achievementDef.getTriggerValue() instanceof Number) {
                                    int requiredStreak = ((Number) achievementDef.getTriggerValue()).intValue();
                                    if (userProfile.getCurrentStreak() >= requiredStreak) {
                                        shouldUnlockThis = true;
                                    }
                                }
                                break;
                            case "POINTS_COLLECTED":
                                if (achievementDef.getTriggerValue() instanceof Number) {
                                    int requiredPoints = ((Number) achievementDef.getTriggerValue()).intValue();
                                    if (userProfile.getPoints() >= requiredPoints) {
                                        shouldUnlockThis = true;
                                    }
                                }
                                break;
                            // Możesz dodać inne specyficzne typy triggerów obsługiwane tutaj
                        }

                        if (shouldUnlockThis) {
                            Log.i(TAG, "User " + userId + " eligible for achievement: " + achievementDef.getName() + " (ID: " + achievementIdToUnlock + ")");
                            firebaseSource.markAchievementAsUnlocked(userId, achievementIdToUnlock, (success, error) -> {
                                if (success) {
                                    Log.i(TAG, "Achievement '" + achievementDef.getName() + "' unlocked for user " + userId);
                                    if (appContext != null) {
                                        Toast.makeText(appContext, "Osiągnięcie: " + achievementDef.getName() + "!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "Failed to unlock '" + achievementDef.getName() + "' for user " + userId, error);
                                }
                            });
                        }
                    }
                }
            });
        });
    }

    private boolean isAlreadyUnlocked(List<UnlockedAchievement> unlockedList, String achievementId) {
        if (unlockedList == null || achievementId == null) return false;
        for (UnlockedAchievement ua : unlockedList) {
            if (achievementId.equals(ua.getAchievementId())) {
                return true;
            }
        }
        return false;
    }
}