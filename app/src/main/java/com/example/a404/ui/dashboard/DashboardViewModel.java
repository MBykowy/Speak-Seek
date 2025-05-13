package com.example.a404.ui.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.Achievement;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.VocabularyItem;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.repository.VocabularyRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DashboardViewModel extends ViewModel {
    private static final String TAG = "DashboardViewModel";

    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GamificationRepository gamificationRepository;

    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final LiveData<UserProfile> userProfile;


    private final MediatorLiveData<List<Achievement>> recentAchievements = new MediatorLiveData<>();

    public DashboardViewModel(UserRepository userRepository,
                              VocabularyRepository vocabularyRepository,
                              GamificationRepository gamificationRepository) {
        this.userRepository = userRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.gamificationRepository = gamificationRepository;

        // Transformacja userId na profil użytkownika
        userProfile = Transformations.switchMap(userId, id -> {
            isLoading.setValue(true);
            return userRepository.getUserProfile(id);
        });




        // Pobieranie ostatnich osiągnięć
        recentAchievements.addSource(userProfile, profile -> {
            if (profile != null && profile.getEarnedAchievementIds() != null &&
                    !profile.getEarnedAchievementIds().isEmpty()) {
                // Pobierz tylko 3 ostatnie osiągnięcia
                int achievementsCount = profile.getEarnedAchievementIds().size();
                int startIndex = Math.max(0, achievementsCount - 3);
                List<String> recentAchievementIds = profile.getEarnedAchievementIds()
                        .subList(startIndex, achievementsCount);

                gamificationRepository.getAchievementsByIds(recentAchievementIds)
                        .observeForever(achievements -> {
                            recentAchievements.setValue(achievements);
                            isLoading.setValue(false);
                        });
            } else {
                recentAchievements.setValue(null);
                isLoading.setValue(false);
            }
        });
    }

    public void init() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId.setValue(currentUser.getUid());
        } else {
            Log.e(TAG, "Nie znaleziono zalogowanego użytkownika");
        }
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }





    public LiveData<List<Achievement>> getRecentAchievements() {
        return recentAchievements;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}