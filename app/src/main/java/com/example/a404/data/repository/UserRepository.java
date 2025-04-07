package com.example.a404.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserRepository {
    private final FirebaseSource firebaseSource;

    public UserRepository(FirebaseSource firebaseSource) {
        this.firebaseSource = firebaseSource;
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();

        firebaseSource.getDocument("users", userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserProfile userProfile = document.toObject(UserProfile.class);
                    userProfileLiveData.setValue(userProfile);
                } else {
                    // Stwórz domyślny profil, jeśli nie istnieje
                    UserProfile defaultProfile = new UserProfile(userId, 0, "en");
                    updateUserProfile(defaultProfile);
                    userProfileLiveData.setValue(defaultProfile);
                }
            } else {
                userProfileLiveData.setValue(null);
            }
        });

        return userProfileLiveData;
    }

    public void updateUserProfile(UserProfile userProfile) {
        firebaseSource.setDocument("users", userProfile.getUserId(), userProfile);
    }

    public void updatePoints(String userId, int pointsToAdd) {
        firebaseSource.updateField("users", userId, "points", pointsToAdd, true);
    }

    public void updateSelectedLanguage(String userId, String languageCode) {
        firebaseSource.updateField("users", userId, "selectedLanguageCode", languageCode, false);
    }
}