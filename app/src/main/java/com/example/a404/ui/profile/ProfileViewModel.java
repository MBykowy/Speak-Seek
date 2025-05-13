// Ścieżka: app/java/com/example/a404/ui/profile/ProfileViewModel.java
package com.example.a404.ui.profile;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UnlockedAchievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.UserRepository;
// import com.example.a404.data.repository.GamificationRepository; // Usunięty, jeśli nie jest tu potrzebny
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Klasa DisplayAchievementProfile (taka sama jak w DashboardViewModel, z unlockedAt)
// ... (kod dla DisplayAchievementProfile bez zmian) ...
class DisplayAchievementProfile { // Upewnij się, że nazwa jest spójna lub użyj wspólnej definicji
    public final Achievement achievement;
    public final boolean isUnlocked;
    public final com.google.firebase.Timestamp unlockedAt;

    DisplayAchievementProfile(Achievement achievement, boolean isUnlocked, com.google.firebase.Timestamp unlockedAt) {
        this.achievement = achievement;
        this.isUnlocked = isUnlocked;
        this.unlockedAt = unlockedAt;
    }
}

public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG_PROFILE_VM = "ProfileViewModel";
    private final UserRepository userRepository;
    private final FirebaseSource firebaseSource;
    // GamificationRepository może być tu wstrzyknięty, jeśli np. chcesz wywołać updateStreak przy wejściu na profil

    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false); // Ogólne isLoading

    private final LiveData<UserProfile> userProfileLiveData;

    private final MediatorLiveData<List<DisplayAchievementProfile>> allDisplayAchievements = new MediatorLiveData<>();
    private final MutableLiveData<List<Achievement>> _allAchievementDefinitions = new MutableLiveData<>();
    private final MutableLiveData<List<UnlockedAchievement>> _userUnlockedAchievements = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application, UserRepository userRepository /*, GamificationRepository gamificationRepository - opcjonalnie */) {
        super(application);
        this.userRepository = userRepository;
        this.firebaseSource = new FirebaseSource();
        // this.gamificationRepository = gamificationRepository; // Jeśli potrzebny

        userProfileLiveData = Transformations.switchMap(userIdLiveData, id -> {
            isLoading.setValue(true);
            return userRepository.getUserProfile(id);
        });

        allDisplayAchievements.addSource(_allAchievementDefinitions, definitions -> {
            if (_userUnlockedAchievements.getValue() != null) isLoading.setValue(false);
            combineAllAchievementsData(definitions, _userUnlockedAchievements.getValue());
        });
        allDisplayAchievements.addSource(_userUnlockedAchievements, unlocked -> {
            if (_allAchievementDefinitions.getValue() != null) isLoading.setValue(false);
            combineAllAchievementsData(_allAchievementDefinitions.getValue(), unlocked);
        });
    }

    private void combineAllAchievementsData(List<Achievement> definitions, List<UnlockedAchievement> unlocked) {
        if (definitions == null) {
            allDisplayAchievements.setValue(new ArrayList<>());
            return;
        }
        // ... (logika combineAchievementData z sortowaniem - taka sama jak w DashboardViewModel) ...
        // ... tworzy listę DisplayAchievementProfile i sortuje ją (odblokowane pierwsze, potem data, potem nazwa) ...
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
        List<DisplayAchievementProfile> resultList = new ArrayList<>();
        for (Achievement def : definitions) {
            boolean isUnlockedStatus = unlockedIdsSet.contains(def.getId());
            com.google.firebase.Timestamp unlockedTime = isUnlockedStatus ? unlockedAtMap.get(def.getId()) : null;
            resultList.add(new DisplayAchievementProfile(def, isUnlockedStatus, unlockedTime));
        }
        // Sortuj: odblokowane najpierw, potem po dacie (najnowsze pierwsze), potem alfabetycznie
        Collections.sort(resultList, (o1, o2) -> {
            if (o1.isUnlocked && !o2.isUnlocked) return -1;
            if (!o1.isUnlocked && o2.isUnlocked) return 1;
            if (o1.isUnlocked && o2.isUnlocked) {
                if (o1.unlockedAt != null && o2.unlockedAt != null) return o2.unlockedAt.compareTo(o1.unlockedAt);
                else if (o1.unlockedAt != null) return -1;
                else if (o2.unlockedAt != null) return 1;
            }
            return o1.achievement.getName().compareToIgnoreCase(o2.achievement.getName());
        });
        allDisplayAchievements.setValue(resultList); // Ustawia pełną, posortowaną listę
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
            // ... (obsługa braku użytkownika)
        }
    }

    private void loadAllDefinitionsAndUserUnlocked(String currentUserId) {
        // Taka sama jak w DashboardViewModel
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

    // Gettery
    public LiveData<UserProfile> getUserProfile() { return userProfileLiveData; }
    public LiveData<List<DisplayAchievementProfile>> getAllDisplayAchievements() { return allDisplayAchievements; } // Zmieniona nazwa
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    // public void setUserIdToDisplay(String newUserId) { ... } // Jeśli potrzebne
}