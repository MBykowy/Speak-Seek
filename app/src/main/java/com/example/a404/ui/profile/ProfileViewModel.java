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

    private final LiveData<UserProfile> userProfileLiveData; // Zmieniono nazwę dla spójności

    private final MediatorLiveData<List<DisplayAchievementProfile>> allDisplayAchievements = new MediatorLiveData<>();
    private final MutableLiveData<List<Achievement>> _allAchievementDefinitions = new MutableLiveData<>();
    private final MutableLiveData<List<UnlockedAchievement>> _userUnlockedAchievements = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
        this.firebaseSource = new FirebaseSource();

        userProfileLiveData = Transformations.switchMap(userIdLiveData, id -> {
            Log.d(TAG_PROFILE_VM, "SwitchMap triggered for UserProfile with id: " + id);
            // isLoading.setValue(true); // isLoading jest teraz zarządzane w load... metodach
            if (id == null) { // Obsługa przypadku, gdy ID jest null (np. po wylogowaniu)
                MutableLiveData<UserProfile> emptyProfile = new MutableLiveData<>();
                emptyProfile.setValue(null);
                isLoading.setValue(false); // Upewnij się, że isLoading jest false
                return emptyProfile;
            }
            return userRepository.getUserProfile(id); // Zakładamy, że to zwraca LiveData
        });

        allDisplayAchievements.addSource(_allAchievementDefinitions, definitions -> {
            Log.d(TAG_PROFILE_VM, "Achievement definitions updated. Count: " + (definitions != null ? definitions.size() : "null"));
            if (_userUnlockedAchievements.getValue() != null && definitions != null) { // Sprawdź też definicje
                isLoading.setValue(false);
            }
            combineAllAchievementsData(definitions, _userUnlockedAchievements.getValue());
        });

        allDisplayAchievements.addSource(_userUnlockedAchievements, unlocked -> {
            Log.d(TAG_PROFILE_VM, "User unlocked achievements updated. Count: " + (unlocked != null ? unlocked.size() : "null"));
            if (_allAchievementDefinitions.getValue() != null && unlocked != null) { // Sprawdź też odblokowane
                isLoading.setValue(false);
            }
            combineAllAchievementsData(_allAchievementDefinitions.getValue(), unlocked);
        });
    }

    private void combineAllAchievementsData(List<Achievement> definitions, List<UnlockedAchievement> unlocked) {
        // ... (kod tej metody bez zmian - sortowanie itd.)
        if (definitions == null) {
            Log.d(TAG_PROFILE_VM, "combineAllAchievementsData: Definitions are null, setting empty list.");
            allDisplayAchievements.setValue(new ArrayList<>());
            return;
        }
        Log.d(TAG_PROFILE_VM, "combineAllAchievementsData: Combining " + definitions.size() + " definitions with " + (unlocked != null ? unlocked.size() : "null") + " unlocked achievements.");
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

    public void init() {
        Log.d(TAG_PROFILE_VM, "init() called.");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            // Ustaw userIdLiveData tylko jeśli jest null lub się zmieniło,
            // aby uniknąć niepotrzebnego triggerowania switchMap, jeśli refreshData sobie z tym poradzi.
            if (userIdLiveData.getValue() == null || !userIdLiveData.getValue().equals(currentUserId)) {
                userIdLiveData.setValue(currentUserId);
            }
            // Odświeżenie danych jest teraz głównie w refreshData,
            // ale init może załadować je po raz pierwszy, jeśli userIdLiveData był null.
            if (_allAchievementDefinitions.getValue() == null || _userUnlockedAchievements.getValue() == null) {
                loadAllDefinitionsAndUserUnlocked(currentUserId);
            }
        } else {
            Log.w(TAG_PROFILE_VM, "init: No current user.");
            userIdLiveData.setValue(null); // Informuje switchMap, że nie ma użytkownika
            _allAchievementDefinitions.postValue(new ArrayList<>());
            _userUnlockedAchievements.postValue(new ArrayList<>());
            isLoading.setValue(false);
        }
    }

    // === NOWA METODA DO WYMUSZENIA ODŚWIEŻENIA ===
    public void refreshData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            Log.d(TAG_PROFILE_VM, "refreshData() called for user: " + currentUserId);

            // Wymuś ponowne załadowanie UserProfile przez chwilowe ustawienie ID na null,
            // a potem z powrotem na właściwe ID. To powinno "obudzić" switchMap.
            // Ta technika jest trochę "hackiem", ale często działa.
            String previousUserId = userIdLiveData.getValue();
            if (currentUserId.equals(previousUserId)) { // Jeśli ID jest to samo
                userIdLiveData.setValue(null); // Triggeruj zmianę
            }
            userIdLiveData.setValue(currentUserId); // Ustaw właściwe ID, co uruchomi switchMap

            // Zawsze ładuj ponownie definicje osiągnięć i odblokowane przez użytkownika
            loadAllDefinitionsAndUserUnlocked(currentUserId);
        } else {
            Log.w(TAG_PROFILE_VM, "refreshData: No current user.");
            userIdLiveData.setValue(null);
            _allAchievementDefinitions.postValue(new ArrayList<>());
            _userUnlockedAchievements.postValue(new ArrayList<>());
            isLoading.setValue(false);
        }
    }

    private void loadAllDefinitionsAndUserUnlocked(String currentUserId) {
        if (currentUserId == null) {
            Log.w(TAG_PROFILE_VM, "loadAllDefinitionsAndUserUnlocked: currentUserId is null, aborting load.");
            _allAchievementDefinitions.postValue(new ArrayList<>());
            _userUnlockedAchievements.postValue(new ArrayList<>());
            isLoading.setValue(false);
            return;
        }
        Log.d(TAG_PROFILE_VM, "Executing loadAllDefinitionsAndUserUnlocked for " + currentUserId);
        isLoading.setValue(true);
        _allAchievementDefinitions.setValue(null); // Wyczyść, aby MediatorLiveData wiedziało, że dane są ładowane
        _userUnlockedAchievements.setValue(null);

        firebaseSource.getAllAchievementDefinitions((definitions, e1) -> {
            if (e1 != null || definitions == null) {
                Log.e(TAG_PROFILE_VM, "Error loading achievement definitions", e1);
                _allAchievementDefinitions.postValue(new ArrayList<>());
            } else {
                Log.d(TAG_PROFILE_VM, "Definitions loaded: " + definitions.size());
                _allAchievementDefinitions.postValue(definitions);
            }
        });

        firebaseSource.getUserUnlockedAchievements(currentUserId, (unlockedList, e2) -> {
            if (e2 != null) {
                Log.e(TAG_PROFILE_VM, "Error loading user's unlocked achievements for " + currentUserId, e2);
                _userUnlockedAchievements.postValue(new ArrayList<>());
            } else {
                Log.d(TAG_PROFILE_VM, "Unlocked achievements loaded: " + (unlockedList != null ? unlockedList.size() : "null"));
                _userUnlockedAchievements.postValue(unlockedList != null ? unlockedList : new ArrayList<>());
            }
            // isLoading zostanie ustawione na false przez MediatorLiveData, gdy oba źródła będą gotowe
        });
    }

    // Gettery
    public LiveData<UserProfile> getUserProfile() { return userProfileLiveData; }
    public LiveData<List<DisplayAchievementProfile>> getAllDisplayAchievements() { return allDisplayAchievements; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    // public void setUserIdToDisplay(String newUserId) { ... } // Jeśli potrzebne
}