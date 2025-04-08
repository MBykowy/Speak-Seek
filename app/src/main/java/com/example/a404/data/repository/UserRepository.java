package com.example.a404.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseSource firebaseSource;

    public UserRepository(FirebaseSource firebaseSource) {
        this.firebaseSource = firebaseSource;
        Log.d(TAG, "UserRepository zainicjalizowany");
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        Log.d(TAG, "Pobieranie profilu użytkownika: " + userId);
        MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();

        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            Log.d(TAG, "Znaleziono profil użytkownika, język: " + profile.getSelectedLanguageCode());
                            userProfileLiveData.setValue(profile);
                        } else {
                            Log.d(TAG, "Nie udało się przekonwertować dokumentu na UserProfile");
                            createDefaultProfile(userId, userProfileLiveData);
                        }
                    } else {
                        Log.d(TAG, "Dokument użytkownika nie istnieje, tworzę nowy");
                        createDefaultProfile(userId, userProfileLiveData);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania profilu: " + e.toString());
                    createDefaultProfile(userId, userProfileLiveData);
                });

        return userProfileLiveData;
    }

    private void createDefaultProfile(String userId, MutableLiveData<UserProfile> liveData) {
        UserProfile newProfile = new UserProfile(userId, 0, "en");
        saveUserProfile(userId, newProfile);
        liveData.setValue(newProfile);
    }

    public void updateSelectedLanguage(String userId, String languageCode) {
        Log.d(TAG, "Aktualizacja języka: " + languageCode + " dla użytkownika: " + userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedLanguageCode", languageCode);

        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Język zaktualizowany pomyślnie: " + languageCode);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd aktualizacji języka: " + e.toString());
                });
    }

    private void saveUserProfile(String userId, UserProfile profile) {
        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profil zapisany pomyślnie");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd zapisu profilu: " + e.toString());
                });
    }
}