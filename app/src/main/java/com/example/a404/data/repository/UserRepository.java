package com.example.a404.data.repository;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        UserProfile newProfile = new UserProfile(userId, null, 0, "en");
        saveUserProfile(userId, newProfile, liveData);
    }


    private void saveUserProfile(String userId, UserProfile profile, @Nullable MutableLiveData<UserProfile> liveDataToUpdate) {
        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profil zapisany pomyślnie dla: " + userId);
                    if (liveDataToUpdate != null) {
                        // Aktualizuj LiveData TYLKO po sukcesie i jeśli zostało przekazane
                        liveDataToUpdate.setValue(profile);
                        Log.d(TAG, "LiveData zaktualizowane po zapisie profilu dla: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd zapisu profilu dla: " + userId, e);
                });
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
        saveUserProfile(userId, profile, null);
    }

    public LiveData<List<UserProfile>> getRankedUsers(int limit) {
        Log.d(TAG, "Pobieranie rankingu użytkowników, limit: " + limit);
        MutableLiveData<List<UserProfile>> rankedUsersLiveData = new MutableLiveData<>();

        firebaseSource.getFirestore()
                .collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> users = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            UserProfile user = document.toObject(UserProfile.class);
                            user.setUserId(document.getId());
                            users.add(user);
                        }
                        Log.d(TAG, "Pomyślnie pobrano " + users.size() + " użytkowników do rankingu.");
                        rankedUsersLiveData.setValue(users);
                    } else {
                        Log.d(TAG, "QuerySnapshot był null podczas pobierania rankingu.");
                        rankedUsersLiveData.setValue(new ArrayList<>()); // Set empty list
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania rankingu użytkowników", e);
                    rankedUsersLiveData.setValue(null); // Indicate error state
                });

        return rankedUsersLiveData;
    }
}