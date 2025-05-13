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
                            // Ustaw userId, ponieważ nie jest on częścią danych dokumentu
                            profile.setUserId(documentSnapshot.getId());
                            Log.d(TAG, "Znaleziono profil użytkownika: " + profile.getUsername() + ", język: " + profile.getSelectedLanguageCode());
                            // Upewnij się, że username nie jest null, jeśli stare dokumenty go nie mają
                            if (profile.getUsername() == null) {
                                profile.setUsername("User_" + userId.substring(0, Math.min(userId.length(), 5))); // Przykładowa domyślna nazwa
                                saveUserProfile(userId, profile); // Zapisz zaktualizowany profil
                            }
                            userProfileLiveData.setValue(profile);
                        } else {
                            Log.d(TAG, "Nie udało się przekonwertować dokumentu na UserProfile dla userId: " + userId);
                            createDefaultProfile(userId, userProfileLiveData);
                        }
                    } else {
                        Log.d(TAG, "Dokument użytkownika nie istnieje, tworzę nowy dla userId: " + userId);
                        createDefaultProfile(userId, userProfileLiveData);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania profilu dla userId: " + userId, e);
                    userProfileLiveData.setValue(null); // Ustaw null w przypadku błędu
                });

        return userProfileLiveData;
    }

    private void createDefaultProfile(String userId, MutableLiveData<UserProfile> liveData) {
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        UserProfile newProfile = new UserProfile(userId, defaultUsername, 0, "en"); // Dodano defaultUsername
        // Używamy saveUserProfile z liveData, aby zaktualizować obserwatora po utworzeniu
        saveUserProfile(userId, newProfile, liveData);
        Log.d(TAG, "Utworzono domyślny profil dla: " + userId);
    }

    // Zapisuje profil i opcjonalnie aktualizuje LiveData
    private void saveUserProfile(String userId, UserProfile profile, @Nullable MutableLiveData<UserProfile> liveDataToUpdate) {
        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .set(profile) // Użyj set() do zapisania całego obiektu
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profil zapisany pomyślnie dla: " + userId);
                    if (liveDataToUpdate != null) {
                        liveDataToUpdate.setValue(profile);
                        Log.d(TAG, "LiveData zaktualizowane po zapisie profilu dla: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd zapisu profilu dla: " + userId, e);
                    // Rozważ powiadomienie o błędzie, jeśli liveDataToUpdate nie jest null
                    if (liveDataToUpdate != null) {
                        liveDataToUpdate.setValue(null); // Wskazuje błąd
                    }
                });
    }

    // Wersja saveUserProfile bez aktualizacji LiveData (używana np. przy aktualizacji username)
    private void saveUserProfile(String userId, UserProfile profile) {
        saveUserProfile(userId, profile, null);
    }


    public void updateSelectedLanguage(String userId, String languageCode) {
        Log.d(TAG, "Aktualizacja języka: " + languageCode + " dla użytkownika: " + userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedLanguageCode", languageCode);

        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .set(updates, SetOptions.merge()) // Użyj merge, aby zaktualizować tylko to pole
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Język zaktualizowany pomyślnie: " + languageCode + " dla " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd aktualizacji języka dla " + userId, e);
                });
    }

    public LiveData<List<UserProfile>> getRankedUsers(int limit) {
        Log.d(TAG, "Pobieranie rankingu użytkowników, limit: " + limit);
        MutableLiveData<List<UserProfile>> rankedUsersLiveData = new MutableLiveData<>();

        firebaseSource.getFirestore()
                .collection("users")
                .orderBy("points", Query.Direction.DESCENDING) // Sortuj po punktach malejąco
                .limit(limit) // Ogranicz liczbę wyników
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> users = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) { // Poprawna pętla for-each
                            UserProfile user = document.toObject(UserProfile.class);
                            if (user != null) {
                                user.setUserId(document.getId()); // Ustaw ID użytkownika z dokumentu
                                // Upewnij się, że nazwa użytkownika istnieje
                                if (user.getUsername() == null) {
                                    user.setUsername("User_" + document.getId().substring(0, Math.min(document.getId().length(), 5)));
                                }
                                users.add(user);
                            } else {
                                Log.w(TAG, "Nie udało się przekonwertować dokumentu na UserProfile w rankingu: " + document.getId());
                            }
                        }
                        Log.d(TAG, "Pomyślnie pobrano " + users.size() + " użytkowników do rankingu.");
                        rankedUsersLiveData.setValue(users);
                    } else {
                        Log.d(TAG, "QuerySnapshot był null podczas pobierania rankingu.");
                        rankedUsersLiveData.setValue(new ArrayList<>()); // Ustaw pustą listę
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania rankingu użytkowników", e);
                    rankedUsersLiveData.setValue(null); // Wskazuje stan błędu
                });

        return rankedUsersLiveData;
    }

    /**
     * Aktualizuje punkty użytkownika, dodając określoną wartość do bieżącej liczby punktów.
     * Używa FieldValue.increment() dla atomowej operacji na serwerze Firestore.
     *
     * @param userId ID użytkownika, którego punkty mają być zaktualizowane
     * @param pointsToAdd liczba punktów do dodania (może być również ujemna)
     */
    public void updatePoints(String userId, int pointsToAdd) {
        Log.d(TAG, "Aktualizacja punktów: +" + pointsToAdd + " dla użytkownika: " + userId);

        firebaseSource.getFirestore()
                .collection("users")
                .document(userId)
                .update("points", com.google.firebase.firestore.FieldValue.increment(pointsToAdd))
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Punkty zaktualizowane pomyślnie o: " + pointsToAdd))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Błąd aktualizacji punktów dla " + userId, e));
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
        saveUserProfile(userId, newProfile); // Użyj istniejącej metody zapisu (bez aktualizacji LiveData)
    }

    // Przeciążona wersja, jeśli dostępny jest tylko userId (generuje domyślną nazwę)
    public void createUserProfile(String userId) {
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        createUserProfile(userId, defaultUsername);
    }

    // Metoda do aktualizacji samej nazwy użytkownika
    public void updateUsername(String userId, String newUsername) {
        Log.d(TAG, "Aktualizacja nazwy użytkownika na: " + newUsername + " dla użytkownika: " + userId);
        if (newUsername == null || newUsername.trim().isEmpty()) {
            Log.w(TAG, "Próba ustawienia pustej nazwy użytkownika dla: " + userId);
            return; // Nie aktualizuj, jeśli nowa nazwa jest pusta
        }
        DocumentReference userRef = firebaseSource.getFirestore().collection("users").document(userId);
        userRef.update("username", newUsername) // Aktualizuj tylko pole 'username'
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nazwa użytkownika zaktualizowana pomyślnie dla: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Błąd aktualizacji nazwy użytkownika dla: " + userId, e));
    }
}