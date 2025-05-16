// Ścieżka: app/java/com/example/a404/ui/dashboard/DashboardViewModel.java
package com.example.a404.ui.dashboard;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.UnlockedAchievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.VocabularyItem;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.repository.VocabularyRepository; // Upewnij się, że to repozytorium istnieje i ma potrzebne metody
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.service.AchievementHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Klasa DisplayDashboardAchievement (bez zmian)
class DisplayDashboardAchievement {
    public final Achievement achievement;
    public final boolean isUnlocked;
    public final com.google.firebase.Timestamp unlockedAt;

    DisplayDashboardAchievement(Achievement achievement, boolean isUnlocked, com.google.firebase.Timestamp unlockedAt) {
        this.achievement = achievement;
        this.isUnlocked = isUnlocked;
        this.unlockedAt = unlockedAt;
    }
}

public class DashboardViewModel extends AndroidViewModel {
    private static final String TAG_DASH_VM = "DashboardViewModel";

    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository; // Do kursów i słówek
    private final GamificationRepository gamificationRepository;
    private final FirebaseSource firebaseSource;
    private final AchievementHelper achievementHelper;

    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final LiveData<UserProfile> userProfileLiveData;
    private final MutableLiveData<List<Course>> recentCoursesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<VocabularyItem>> reviewWordsLiveData = new MutableLiveData<>();

    private final MediatorLiveData<List<DisplayDashboardAchievement>> recentDisplayAchievements = new MediatorLiveData<>();
    private final MutableLiveData<List<Achievement>> _allAchievementDefinitions = new MutableLiveData<>();
    private final MutableLiveData<List<UnlockedAchievement>> _userUnlockedAchievements = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application, UserRepository userRepository,
                              VocabularyRepository vocabularyRepository, // Dodano do konstruktora
                              GamificationRepository gamificationRepository) {
        super(application);
        this.userRepository = userRepository;
        this.vocabularyRepository = vocabularyRepository; // Przypisz
        this.gamificationRepository = gamificationRepository;
        this.firebaseSource = new FirebaseSource();
        this.achievementHelper = new AchievementHelper(application.getApplicationContext(), firebaseSource);

        userProfileLiveData = Transformations.switchMap(userIdLiveData, id -> {
            isLoading.setValue(true);
            return userRepository.getUserProfile(id);
        });

        recentDisplayAchievements.addSource(_allAchievementDefinitions, definitions -> {
            if (_userUnlockedAchievements.getValue() != null) isLoading.setValue(false);
            combineAndPrepareRecentAchievements(definitions, _userUnlockedAchievements.getValue());
        });
        recentDisplayAchievements.addSource(_userUnlockedAchievements, unlocked -> {
            if (_allAchievementDefinitions.getValue() != null) isLoading.setValue(false);
            combineAndPrepareRecentAchievements(_allAchievementDefinitions.getValue(), unlocked);
        });

        userProfileLiveData.observeForever(profile -> {
            if (profile != null && userIdLiveData.getValue() != null) {
                final String currentUserId = userIdLiveData.getValue(); // Przechowaj, aby uniknąć problemów z wartością null w lambda
                Log.d(TAG_DASH_VM, "Profile loaded for dashboard, updating streak and checking achievements for UID: " + currentUserId);

                gamificationRepository.updateStreak(currentUserId, profile, (streakSuccess, streakError) -> {
                    if (streakSuccess) {
                        Log.d(TAG_DASH_VM, "Streak updated successfully for UID: " + currentUserId);
                        firebaseSource.getUserProfile(currentUserId, (updatedProfile, profileError) -> {
                            if (profileError == null && updatedProfile != null) {
                                gamificationRepository.checkAndUnlockSpecificAchievements(currentUserId, updatedProfile);
                            } else {
                                Log.e(TAG_DASH_VM, "Error fetching updated profile after streak update for UID: " + currentUserId, profileError);
                            }
                        });
                    } else {
                        Log.e(TAG_DASH_VM, "Failed to update streak for UID: " + currentUserId, streakError);
                    }
                });
                // === WYWOŁANIE METOD ŁADUJĄCYCH ===
                loadRecentCourses(profile.getSelectedLanguageCode(), currentUserId); // Przekaż też userId, jeśli potrzebne
                loadReviewWords(currentUserId);
            }
        });
    }

    private void combineAndPrepareRecentAchievements(List<Achievement> definitions, List<UnlockedAchievement> unlocked) {
        // ... (kod tej metody bez zmian) ...
        if (definitions == null) {
            recentDisplayAchievements.setValue(new ArrayList<>());
            return;
        }
        Set<String> unlockedIdsSet = new HashSet<>();
        java.util.Map<String, com.google.firebase.Timestamp> unlockedAtMap = new java.util.HashMap<>();
        if (unlocked != null) {
            for (UnlockedAchievement ua : unlocked) {
                if (ua.getAchievementId() != null) {
                    unlockedIdsSet.add(ua.getAchievementId());
                    unlockedAtMap.put(ua.getAchievementId(), ua.getUnlockedAt());
                }
            }
        }
        List<DisplayDashboardAchievement> fullList = new ArrayList<>();
        for (Achievement def : definitions) {
            boolean isUnlockedStatus = unlockedIdsSet.contains(def.getId());
            com.google.firebase.Timestamp unlockedTime = isUnlockedStatus ? unlockedAtMap.get(def.getId()) : null;
            fullList.add(new DisplayDashboardAchievement(def, isUnlockedStatus, unlockedTime));
        }
        Collections.sort(fullList, (o1, o2) -> {
            if (o1.isUnlocked && !o2.isUnlocked) return -1;
            if (!o1.isUnlocked && o2.isUnlocked) return 1;
            if (o1.isUnlocked && o2.isUnlocked) {
                if (o1.unlockedAt != null && o2.unlockedAt != null) return o2.unlockedAt.compareTo(o1.unlockedAt);
                else if (o1.unlockedAt != null) return -1;
                else if (o2.unlockedAt != null) return 1;
            }
            return o1.achievement.getName().compareToIgnoreCase(o2.achievement.getName());
        });
        List<DisplayDashboardAchievement> recentList = new ArrayList<>();
        int count = 0;
        int limit = 3;
        for (DisplayDashboardAchievement item : fullList) {
            if (item.isUnlocked) {
                recentList.add(item);
                count++;
                if (count >= limit) break;
            }
        }
        if (count < limit) {
            for (DisplayDashboardAchievement item : fullList) {
                if (!item.isUnlocked) {
                    recentList.add(item);
                    count++;
                    if (count >= limit) break;
                }
                if (recentList.size() >= limit) break;
            }
        }
        recentDisplayAchievements.setValue(recentList);
    }

    public void init() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            if (!currentUserId.equals(userIdLiveData.getValue())) {
                userIdLiveData.setValue(currentUserId);
                loadAllDefinitionsAndUserUnlocked(currentUserId);
            }
        } else {
            Log.w(TAG_DASH_VM, "No current user to initialize dashboard data.");
            isLoading.setValue(false);
            userProfileLiveData.getValue(); // Aby uniknąć potencjalnego problemu z Transformations.switchMap
            recentCoursesLiveData.setValue(new ArrayList<>());
            reviewWordsLiveData.setValue(new ArrayList<>());
            recentDisplayAchievements.setValue(new ArrayList<>());
        }
    }

    private void loadAllDefinitionsAndUserUnlocked(String currentUserId) {
        // ... (kod tej metody bez zmian) ...
        isLoading.setValue(true);
        _allAchievementDefinitions.setValue(null);
        _userUnlockedAchievements.setValue(null);
        firebaseSource.getAllAchievementDefinitions((definitions, e1) -> {
            _allAchievementDefinitions.postValue(definitions != null ? definitions : new ArrayList<>());
        });
        firebaseSource.getUserUnlockedAchievements(currentUserId, (unlockedList, e2) -> {
            _userUnlockedAchievements.postValue(unlockedList != null ? unlockedList : new ArrayList<>());
        });
    }

    // === IMPLEMENTACJA METOD ŁADUJĄCYCH (PRZYKŁADOWA) ===
    private void loadRecentCourses(String languageCode, String userId) {
        // isLoading.setValue(true); // Jeśli ładowanie kursów jest osobnym procesem
        // Użyj vocabularyRepository do pobrania kursów
        // Przykład, jeśli vocabularyRepository ma taką metodę:
        /*
        vocabularyRepository.getRecentCoursesForUser(userId, languageCode, new VocabularyRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courses) {
                recentCoursesLiveData.postValue(courses);
                // if (_userUnlockedAchievements.getValue() != null) isLoading.setValue(false); // Zakończ ładowanie
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG_DASH_VM, "Error loading recent courses", e);
                recentCoursesLiveData.postValue(new ArrayList<>());
                // if (_userUnlockedAchievements.getValue() != null) isLoading.setValue(false);
            }
        });
        */
        Log.d(TAG_DASH_VM, "Placeholder: ładowanie kursów dla języka: " + languageCode + " i użytkownika: " + userId);
        recentCoursesLiveData.postValue(new ArrayList<>()); // Tymczasowy placeholder
    }

    private void loadReviewWords(String userId) {
        // isLoading.setValue(true); // Jeśli ładowanie słówek jest osobnym procesem
        // Użyj vocabularyRepository do pobrania słówek
        // Przykład, jeśli vocabularyRepository ma taką metodę:
        /*
        vocabularyRepository.getWordsForReview(userId, new VocabularyRepository.ReviewWordsCallback() {
            @Override
            public void onReviewWordsLoaded(List<VocabularyItem> words) {
                reviewWordsLiveData.postValue(words);
                // if (_userUnlockedAchievements.getValue() != null && _allAchievementDefinitions.getValue() != null && recentCoursesLiveData.getValue() != null) isLoading.setValue(false);
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG_DASH_VM, "Error loading review words", e);
                reviewWordsLiveData.postValue(new ArrayList<>());
                // if (_userUnlockedAchievements.getValue() != null && _allAchievementDefinitions.getValue() != null && recentCoursesLiveData.getValue() != null) isLoading.setValue(false);
            }
        });
        */
        Log.d(TAG_DASH_VM, "Placeholder: ładowanie słówek do powtórki dla użytkownika: " + userId);
        reviewWordsLiveData.postValue(new ArrayList<>()); // Tymczasowy placeholder
    }

    // Gettery
    public LiveData<UserProfile> getUserProfile() { return userProfileLiveData; }
    public LiveData<List<Course>> getRecentCourses() { return recentCoursesLiveData; }
    public LiveData<List<VocabularyItem>> getReviewWords() { return reviewWordsLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<DisplayDashboardAchievement>> getRecentDisplayAchievements() { return recentDisplayAchievements; }
}