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
                            Log.d(TAG, "Znaleziono profil użytkownika: " + profile.getUsername() + ", język: " + profile.getSelectedLanguageCode());
                            // Upewnij się, że username nie jest null, jeśli stare dokumenty go nie mają
                            if (profile.getUsername() == null) {
                                profile.setUsername("User_" + userId.substring(0, 5)); // Przykładowa domyślna nazwa
                                saveUserProfile(userId, profile); // Zapisz zaktualizowany profil
                            }
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
                    // Rozważ inną obsługę błędu niż tworzenie profilu
                    // createDefaultProfile(userId, userProfileLiveData);
                    userProfileLiveData.setValue(null); // Lub ustaw null w przypadku błędu
                });

        return userProfileLiveData;
    }

    private void createDefaultProfile(String userId, MutableLiveData<UserProfile> liveData) {
        // Użyj części ID użytkownika lub innej logiki do stworzenia domyślnej nazwy
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        UserProfile newProfile = new UserProfile(userId, defaultUsername, 0, "en"); // Dodano defaultUsername
        saveUserProfile(userId, newProfile);
        liveData.setValue(newProfile);
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
                .document(userId)
                .set(profile) // Metoda set() zapisze cały obiekt, w tym nowe pole username
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profil zapisany pomyślnie dla użytkownika: " + profile.getUsername());
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
    // Metoda do jawnego tworzenia profilu użytkownika, np. po rejestracji
    public void createUserProfile(String userId, String username) {
        Log.d(TAG, "Tworzenie nowego profilu dla użytkownika: " + userId + " z nazwą: " + username);
        // Sprawdzenie czy nazwa użytkownika nie jest pusta, jeśli tak, użyj domyślnej
        if (username == null || username.trim().isEmpty()) {
            username = "User_" + userId.substring(0, Math.min(userId.length(), 5));
            Log.d(TAG, "Nazwa użytkownika pusta, używam domyślnej: " + username);
        }
        UserProfile newProfile = new UserProfile(userId, username, 0, "en"); // Domyślny język "en", 0 punktów
        saveUserProfile(userId, newProfile); // Użyj istniejącej metody zapisu
    }

    // Przeciążona wersja, jeśli dostępny jest tylko userId (generuje domyślną nazwę)
    public void createUserProfile(String userId) {
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        createUserProfile(userId, defaultUsername);
    }
    // Opcjonalnie: Metoda do aktualizacji samej nazwy użytkownika
    public void updateUsername(String userId, String newUsername) {
        Log.d(TAG, "Aktualizacja nazwy użytkownika na: " + newUsername + " dla użytkownika: " + userId);
        DocumentReference userRef = firebaseSource.getFirestore().collection("users").document(userId);
        userRef.update("username", newUsername)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nazwa użytkownika zaktualizowana pomyślnie."))
                .addOnFailureListener(e -> Log.e(TAG, "Błąd aktualizacji nazwy użytkownika: " + e.toString()));
    }
}