// Ścieżka: app/java/com/example/a404/service/AchievementHelper.java (lub util)
package com.example.a404.service; // lub com.example.a404.util

import android.content.Context;
import android.util.Log;
import android.widget.Toast; // Do wyświetlania powiadomień o odblokowaniu

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UnlockedAchievement;
import com.example.a404.data.model.UserProfile; // Będzie używane, gdy dostarczę UserProfile.java
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger; // Do bezpiecznego zliczania operacji asynchronicznych

public class AchievementHelper {

    private static final String TAG = "AchievementHelper";
    private FirebaseSource firebaseSource;
    private Context appContext; // Kontekst aplikacji do wyświetlania Toastów

    public interface AchievementCheckListener {
        void onAchievementsChecked(List<Achievement> newlyUnlockedAchievements, Exception e);
    }

    public AchievementHelper(Context context, FirebaseSource source) {
        this.appContext = context.getApplicationContext();
        this.firebaseSource = source; // Przekazujemy instancję FirebaseSource
    }

    /**
     * Główna metoda sprawdzająca i odblokowująca osiągnięcia dla użytkownika.
     * @param userProfile Aktualny profil użytkownika z danymi postępu.
     * @param listener Callback informujący o wyniku.
     */
    public void checkAndUnlockAchievements(UserProfile userProfile, AchievementCheckListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in. Cannot check achievements.");
            if (listener != null) listener.onAchievementsChecked(null, new IllegalStateException("User not logged in."));
            return;
        }
        if (userProfile == null) {
            Log.e(TAG, "UserProfile is null. Cannot determine progress for achievements.");
            // W tym miejscu można by spróbować pobrać UserProfile, jeśli go nie ma,
            // ale dla prostoty zakładamy, że jest przekazywany.
            if (listener != null) listener.onAchievementsChecked(null, new IllegalStateException("UserProfile is null."));
            return;
        }
        String userId = currentUser.getUid();

        // Krok 1: Pobierz wszystkie definicje osiągnięć
        firebaseSource.getAllAchievementDefinitions((allDefinitions, e1) -> {
            if (e1 != null || allDefinitions == null) {
                Log.e(TAG, "Failed to get achievement definitions.", e1);
                if (listener != null) listener.onAchievementsChecked(null, e1);
                return;
            }

            // Krok 2: Pobierz już odblokowane osiągnięcia użytkownika
            firebaseSource.getUserUnlockedAchievements(userId, (userUnlockedList, e2) -> {
                if (e2 != null) { // Nawet jeśli lista jest null, ale jest błąd, obsłuż go
                    Log.e(TAG, "Failed to get user's unlocked achievements.", e2);
                    // Możemy zdecydować, czy kontynuować, zakładając brak odblokowanych, czy przerwać
                    // Dla bezpieczeństwa, przerwijmy, jeśli nie możemy pobrać odblokowanych.
                    if (listener != null) listener.onAchievementsChecked(null, e2);
                    return;
                }

                Set<String> unlockedIdsSet = new HashSet<>();
                if (userUnlockedList != null) {
                    for (UnlockedAchievement unlocked : userUnlockedList) {
                        if (unlocked.getAchievementId() != null) {
                            unlockedIdsSet.add(unlocked.getAchievementId());
                        }
                    }
                }

                List<Achievement> achievementsToPotentiallyUnlock = new ArrayList<>();
                for (Achievement definition : allDefinitions) {
                    if (!unlockedIdsSet.contains(definition.getId())) { // Sprawdź tylko te jeszcze nieodblokowane
                        if (shouldUnlockAchievement(definition, userProfile)) {
                            achievementsToPotentiallyUnlock.add(definition);
                        }
                    }
                }

                if (achievementsToPotentiallyUnlock.isEmpty()) {
                    // Brak nowych osiągnięć do odblokowania
                    if (listener != null) listener.onAchievementsChecked(new ArrayList<>(), null); // Pusta lista, brak błędu
                    return;
                }

                // Krok 3: Odblokuj te, które spełniają warunki
                unlockAchievementsList(userId, achievementsToPotentiallyUnlock, listener);
            });
        });
    }

    /**
     * Odblokowuje listę osiągnięć i wywołuje listener po zakończeniu wszystkich operacji.
     */
    private void unlockAchievementsList(String userId, List<Achievement> achievementsToUnlock, AchievementCheckListener listener) {
        List<Achievement> successfullyUnlockedThisSession = new ArrayList<>();
        AtomicInteger operationsCounter = new AtomicInteger(achievementsToUnlock.size()); // Licznik operacji asynchronicznych

        if (achievementsToUnlock.isEmpty() && listener != null) {
            listener.onAchievementsChecked(successfullyUnlockedThisSession, null);
            return;
        }

        for (Achievement achievement : achievementsToUnlock) {
            firebaseSource.markAchievementAsUnlocked(userId, achievement.getId(), (success, e) -> {
                if (success) {
                    Log.i(TAG, "Achievement unlocked: " + achievement.getName());
                    successfullyUnlockedThisSession.add(achievement);
                    // Możesz tutaj wyświetlić Toast dla każdego odblokowanego osiągnięcia
                    // Toast.makeText(appContext, "Osiągnięcie odblokowane: " + achievement.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to mark achievement as unlocked: " + achievement.getName(), e);
                }

                // Zmniejsz licznik i sprawdź, czy wszystkie operacje się zakończyły
                if (operationsCounter.decrementAndGet() == 0) {
                    if (listener != null) {
                        listener.onAchievementsChecked(successfullyUnlockedThisSession, null); // Zwróć listę faktycznie odblokowanych
                    }
                }
            });
        }
    }

    /**
     * Sprawdza, czy dane osiągnięcie powinno zostać odblokowane na podstawie profilu użytkownika.
     * Ta metoda będzie wymagała dostosowania po otrzymaniu kodu UserProfile.java.
     */
    private boolean shouldUnlockAchievement(Achievement achievement, UserProfile userProfile) {
        if (userProfile == null) return false;

        try {
            switch (achievement.getTriggerType()) {
                case "LESSONS_COMPLETED":
                    if (achievement.getTriggerValue() instanceof Number) {
                        long requiredLessons = ((Number) achievement.getTriggerValue()).longValue();
                        return userProfile.getLessonsCompletedCount() >= requiredLessons;
                    } else {
                        Log.e(TAG, "Invalid triggerValue type for LESSONS_COMPLETED: " + achievement.getTriggerValue());
                        return false;
                    }

                case "LANGUAGES_STARTED":
                    if (achievement.getTriggerValue() instanceof Number) {
                        long requiredLanguages = ((Number) achievement.getTriggerValue()).longValue();
                        return userProfile.getLanguagesStartedIds() != null &&
                                userProfile.getLanguagesStartedIds().size() >= requiredLanguages;
                    } else {
                        Log.e(TAG, "Invalid triggerValue type for LANGUAGES_STARTED: " + achievement.getTriggerValue());
                        return false;
                    }

                case "SPECIFIC_LANGUAGE_STARTED":
                    if (achievement.getTriggerValue() instanceof String) {
                        String requiredLanguageId = (String) achievement.getTriggerValue();
                        return userProfile.getLanguagesStartedIds() != null &&
                                userProfile.getLanguagesStartedIds().contains(requiredLanguageId);
                    } else {
                        Log.e(TAG, "Invalid triggerValue type for SPECIFIC_LANGUAGE_STARTED: " + achievement.getTriggerValue());
                        return false;
                    }

                    // === NOWY TRIGGER ===
                case "FIRST_LOGIN":
                    // Ten trigger jest specyficzny. Zakładamy, że jeśli ta metoda jest wywoływana
                    // po zalogowaniu i userProfile istnieje, to warunek jest spełniony.
                    // UserProfile musi być już utworzony w Firestore.
                    // Można by dodać pole w UserProfile np. 'isFirstLoginAchievementGranted'
                    // i sprawdzać je tutaj, ale dla prostoty na razie tak:
                    return true; // Jeśli UserProfile istnieje, a to osiągnięcie jeszcze nie jest odblokowane.

                default:
                    Log.w(TAG, "Unknown trigger type: " + achievement.getTriggerType() + " for achievement: " + achievement.getName());
                    return false;
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "ClassCastException while processing trigger for achievement " + achievement.getName(), e);
            return false;
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException while processing trigger for " + achievement.getName(), e);
            return false;
        }
    }
}