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
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Klasa DisplayAchievementProfile (bez zmian)
class DisplayAchievementProfile {
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

    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final LiveData<UserProfile> userProfileLiveData;

    private final MediatorLiveData<List<DisplayAchievementProfile>> allDisplayAchievements = new MediatorLiveData<>();
    private final MutableLiveData<List<Achievement>> _allAchievementDefinitions = new MutableLiveData<>();
    private final MutableLiveData<List<UnlockedAchievement>> _userUnlockedAchievements = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
        this.firebaseSource = new FirebaseSource();

        userProfileLiveData = Transformations.switchMap(userIdLiveData, id -> {
            Log.d(TAG_PROFILE_VM, "SwitchMap for UserProfile triggered with id: " + id);
            isLoading.setValue(true); // Rozpocznij ładowanie
            if (id == null) {
                MutableLiveData<UserProfile> emptyProfile = new MutableLiveData<>();
                emptyProfile.setValue(null);
                // isLoading.setValue(false); // Zarządzane przez addSource
                return emptyProfile;
            }
            return userRepository.getUserProfile(id); // To powinno zawsze robić zapytanie lub zwracać aktualne LiveData
        });

        allDisplayAchievements.addSource(_allAchievementDefinitions, definitions -> {
            Log.d(TAG_PROFILE_VM, "Definitions updated. Unlocked: " + (_userUnlockedAchievements.getValue() != null));
            if (_userUnlockedAchievements.getValue() != null && definitions != null) {
                isLoading.setValue(false);
            }
            combineAllAchievementsData(definitions, _userUnlockedAchievements.getValue());
        });
        allDisplayAchievements.addSource(_userUnlockedAchievements, unlocked -> {
            Log.d(TAG_PROFILE_VM, "Unlocked updated. Definitions: " + (_allAchievementDefinitions.getValue() != null));
            if (_allAchievementDefinitions.getValue() != null && unlocked != null) {
                isLoading.setValue(false);
            }
            combineAllAchievementsData(_allAchievementDefinitions.getValue(), unlocked);
        });
    }

    private void combineAllAchievementsData(List<Achievement> definitions, List<UnlockedAchievement> unlocked) {
        // ... (kod tej metody bez zmian - sortowanie itd.)
        if (definitions == null) {
            allDisplayAchievements.setValue(new ArrayList<>());
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
        List<DisplayAchievementProfile> resultList = new ArrayList<>();
        for (Achievement def : definitions) {
            boolean isUnlockedStatus = unlockedIdsSet.contains(def.getId());
            com.google.firebase.Timestamp unlockedTime = isUnlockedStatus ? unlockedAtMap.get(def.getId()) : null;
            resultList.add(new DisplayAchievementProfile(def, isUnlockedStatus, unlockedTime));
        }
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
        allDisplayAchievements.setValue(resultList);
    }

    // Zmieniamy nazwę z init() na loadDataForCurrentUser() lub zostawiamy init()
    // i upewniamy się, że zawsze ładuje dane
    public void loadDataForCurrentUser() {
        Log.i(TAG_PROFILE_VM, "loadDataForCurrentUser() called. Forcing data refresh.");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            isLoading.setValue(true); // Pokaż wskaźnik ładowania

            // Zawsze ustawiaj userIdLiveData, aby potencjalnie odświeżyć profil,
            // jeśli implementacja repozytorium na to pozwala przy ponownym ustawieniu tej samej wartości
            // lub jeśli switchMap zawsze reaguje.
            // Jeśli nie, to UserProfile może nie zostać odświeżony, ale osiągnięcia tak.
            userIdLiveData.setValue(currentUserId);

            // Zawsze ładuj ponownie definicje osiągnięć i odblokowane przez użytkownika
            loadAllDefinitionsAndUserUnlocked(currentUserId);
        } else {
            Log.w(TAG_PROFILE_VM, "loadDataForCurrentUser: No current user. Clearing data.");
            userIdLiveData.setValue(null); // To triggeruje switchMap do ustawienia profilu na null
            _allAchievementDefinitions.postValue(new ArrayList<>());
            _userUnlockedAchievements.postValue(new ArrayList<>());
            isLoading.setValue(false);
        }
    }

    private void loadAllDefinitionsAndUserUnlocked(String currentUserId) {
        if (currentUserId == null) {
            Log.w(TAG_PROFILE_VM, "loadAllDefinitionsAndUserUnlocked: currentUserId is null.");
            _allAchievementDefinitions.postValue(new ArrayList<>());
            _userUnlockedAchievements.postValue(new ArrayList<>());
            // isLoading.setValue(false); // isLoading jest zarządzane przez MediatorLiveData
            return;
        }
        Log.d(TAG_PROFILE_VM, "Executing loadAllDefinitionsAndUserUnlocked for " + currentUserId);
        // isLoading.setValue(true); // Ustawiane w loadDataForCurrentUser

        // Resetuj LiveData, aby MediatorLiveData wiedziało, że dane są przeładowywane
        _allAchievementDefinitions.setValue(null);
        _userUnlockedAchievements.setValue(null);

        firebaseSource.getAllAchievementDefinitions((definitions, e1) -> {
            if (e1 != null || definitions == null) {
                Log.e(TAG_PROFILE_VM, "Error loading achievement definitions", e1);
                _allAchievementDefinitions.postValue(new ArrayList<>());
            } else {
                _allAchievementDefinitions.postValue(definitions);
            }
        });

        firebaseSource.getUserUnlockedAchievements(currentUserId, (unlockedList, e2) -> {
            if (e2 != null) {
                Log.e(TAG_PROFILE_VM, "Error loading user's unlocked achievements for " + currentUserId, e2);
                _userUnlockedAchievements.postValue(new ArrayList<>());
            } else {
                _userUnlockedAchievements.postValue(unlockedList != null ? unlockedList : new ArrayList<>());
            }
        });
    }

    // Gettery
    public LiveData<UserProfile> getUserProfile() { return userProfileLiveData; }
    public LiveData<List<DisplayAchievementProfile>> getAllDisplayAchievements() { return allDisplayAchievements; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}