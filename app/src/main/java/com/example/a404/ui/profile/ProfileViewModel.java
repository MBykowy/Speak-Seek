package com.example.a404.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final GamificationRepository gamificationRepository;
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final LiveData<UserProfile> userProfile;
    private final MediatorLiveData<List<Achievement>> userAchievements = new MediatorLiveData<>();

    public ProfileViewModel(UserRepository userRepository, GamificationRepository gamificationRepository) {
        this.userRepository = userRepository;
        this.gamificationRepository = gamificationRepository;

        // Transformacja userId na profil użytkownika
        userProfile = Transformations.switchMap(userId, id -> {
            isLoading.setValue(true);
            return userRepository.getUserProfile(id);
        });

        // Pobieranie osiągnięć użytkownika na podstawie jego profilu
        userAchievements.addSource(userProfile, profile -> {
            if (profile != null && profile.getEarnedAchievementIds() != null && !profile.getEarnedAchievementIds().isEmpty()) {
                loadAchievementsForUser(profile.getEarnedAchievementIds());
            } else {
                userAchievements.setValue(new ArrayList<>());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Inicjalizuje ViewModel z aktualnie zalogowanym użytkownikiem
     */
    public void init() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId.setValue(currentUser.getUid());
        }
    }

    /**
     * Ładuje szczegółowe informacje o osiągnięciach na podstawie ich ID
     */
    private void loadAchievementsForUser(List<String> achievementIds) {
        isLoading.setValue(true);
        gamificationRepository.getAchievementsByIds(achievementIds)
                .observeForever(achievements -> {
                    userAchievements.setValue(achievements);
                    isLoading.setValue(false);
                });
    }

    /**
     * Zwraca LiveData z profilem użytkownika
     */
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    /**
     * Zwraca LiveData z osiągnięciami użytkownika
     */
    public LiveData<List<Achievement>> getUserAchievements() {
        return userAchievements;
    }

    /**
     * Zwraca LiveData z informacją o stanie ładowania
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Ustawia ID użytkownika, dla którego chcemy wyświetlić profil
     * (przydatne, gdy chcemy wyświetlić profil innego użytkownika)
     */
    public void setUserId(String userId) {
        this.userId.setValue(userId);
    }
}