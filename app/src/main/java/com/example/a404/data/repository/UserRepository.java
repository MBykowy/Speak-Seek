package com.example.a404.data.repository;

import android.util.Log;

import androidx.annotation.NonNull; // <<< DODAJ TEN IMPORT
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
    private static final String TAG = "UserRepository"; // Użyj TAG zamiast TAG_REPO dla spójności z resztą pliku
    private final FirebaseSource firebaseSource;

    // === INTERFEJS CALLBACK DLA OPERACJI UŻYTKOWNIKA ===
    public interface UserOperationCallback {
        void onComplete(boolean success, @Nullable Exception e);
    }

    public UserRepository(FirebaseSource firebaseSource) {
        this.firebaseSource = firebaseSource;
        Log.d(TAG, "UserRepository zainicjalizowany");
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        Log.d(TAG, "Pobieranie profilu użytkownika: " + userId);
        MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Nieprawidłowe userId w getUserProfile: null lub puste.");
            userProfileLiveData.setValue(null); // Ustaw null, aby wskazać błąd lub brak danych
            return userProfileLiveData;
        }

        firebaseSource.getUserProfile(userId, (profile, e) -> { // Użyj metody z FirebaseSource, która ma callback
            if (e != null) {
                Log.e(TAG, "Błąd pobierania profilu dla userId: " + userId + " z FirebaseSource", e);
                userProfileLiveData.setValue(null);
                return;
            }
            if (profile != null) {
                // Upewnij się, że username nie jest null, jeśli stare dokumenty go nie mają
                if (profile.getUsername() == null || profile.getUsername().trim().isEmpty()) {
                    profile.setUsername("User_" + userId.substring(0, Math.min(userId.length(), 5)));
                    // Rozważ zapisanie zaktualizowanego profilu, jeśli nazwa użytkownika była null
                    // saveUserProfile(userId, profile); // To może spowodować pętlę, jeśli getUserProfile jest wywoływane po zapisie
                }
                Log.d(TAG, "Znaleziono profil użytkownika: " + profile.getUsername() + ", język: " + profile.getSelectedLanguageCode());
                userProfileLiveData.setValue(profile);
            } else {
                Log.d(TAG, "Profil użytkownika nie istnieje w FirebaseSource, tworzę nowy dla userId: " + userId);
                createDefaultProfile(userId, userProfileLiveData);
            }
        });
        return userProfileLiveData;
    }

    private void createDefaultProfile(String userId, MutableLiveData<UserProfile> liveDataToUpdate) {
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        UserProfile newProfile = new UserProfile(userId, defaultUsername, 0, "en");
        saveUserProfile(userId, newProfile, liveDataToUpdate); // Użyj saveUserProfile z LiveData
        Log.d(TAG, "Utworzono domyślny profil dla: " + userId);
    }

    private void saveUserProfile(String userId, UserProfile profile, @Nullable MutableLiveData<UserProfile> liveDataToUpdate) {
        // Użyj metody setDocument z FirebaseSource, która powinna mieć callback
        firebaseSource.setDocument("users", userId, profile,
                aVoid -> {
                    Log.d(TAG, "Profil zapisany pomyślnie dla: " + userId);
                    if (liveDataToUpdate != null) {
                        liveDataToUpdate.setValue(profile);
                    }
                },
                e -> {
                    Log.e(TAG, "Błąd zapisu profilu dla: " + userId, e);
                    if (liveDataToUpdate != null) {
                        liveDataToUpdate.setValue(null);
                    }
                });
    }

    private void saveUserProfile(String userId, UserProfile profile) {
        saveUserProfile(userId, profile, null);
    }

    // Zmodyfikuj, aby przyjmowała callback, jeśli WordGameActivity (lub inny fragment) tego potrzebuje
    public void updateSelectedLanguage(String userId, String languageCode, @Nullable UserOperationCallback callback) {
        Log.d(TAG, "Aktualizacja języka: " + languageCode + " dla użytkownika: " + userId);

        if (userId == null || userId.isEmpty() || languageCode == null) {
            Log.e(TAG, "Nieprawidłowe argumenty w updateSelectedLanguage");
            if (callback != null) callback.onComplete(false, new IllegalArgumentException("User ID or Language Code is null"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedLanguageCode", languageCode);

        // Użyj updateFields z FirebaseSource (lub updateField, jeśli tylko jedno pole)
        firebaseSource.updateFields("users", userId, updates,
                aVoid -> {
                    Log.d(TAG, "Język zaktualizowany pomyślnie: " + languageCode + " dla " + userId);
                    if (callback != null) callback.onComplete(true, null);
                },
                e -> {
                    Log.e(TAG, "Błąd aktualizacji języka dla " + userId, e);
                    if (callback != null) callback.onComplete(false, e);
                });
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
                            if (user != null) {
                                user.setUserId(document.getId());
                                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                                    user.setUsername("User_" + document.getId().substring(0, Math.min(document.getId().length(), 5)));
                                }
                                users.add(user);
                            }
                        }
                        Log.d(TAG, "Pomyślnie pobrano " + users.size() + " użytkowników do rankingu.");
                        rankedUsersLiveData.setValue(users);
                    } else {
                        rankedUsersLiveData.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd pobierania rankingu użytkowników", e);
                    rankedUsersLiveData.setValue(null);
                });
        return rankedUsersLiveData;
    }

    /**
     * Aktualizuje punkty użytkownika, dodając określoną wartość do bieżącej liczby punktów.
     * Używa metody updateUserPoints z FirebaseSource, która powinna obsługiwać FieldValue.increment().
     *
     * @param userId ID użytkownika, którego punkty mają być zaktualizowane
     * @param pointsToAdd liczba punktów do dodania (może być również ujemna)
     * @param callback Callback informujący o wyniku operacji
     */
    public void updatePoints(String userId, int pointsToAdd, @NonNull UserOperationCallback callback) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty in updatePoints.");
            // Użyj if (callback != null) dla bezpieczeństwa, chociaż jest @NonNull
            if (callback != null) callback.onComplete(false, new IllegalArgumentException("User ID cannot be null or empty"));
            return;
        }

        if (pointsToAdd == 0) {
            Log.d(TAG, "Points to add is 0, no Firestore update needed for user: " + userId);
            if (callback != null) callback.onComplete(true, null); // Operacja logicznie "udana"
            return;
        }

        Log.d(TAG, "Attempting to update points: " + (pointsToAdd > 0 ? "+" : "") + pointsToAdd + " for user: " + userId);

        // Wywołaj metodę z FirebaseSource, która już ma logikę z FieldValue.increment i callbackiem
        firebaseSource.updateUserPoints(userId, pointsToAdd, (success, e) -> {
            if (success) {
                Log.d(TAG, "FirebaseSource reported success updating points for user: " + userId);
                if (callback != null) callback.onComplete(true, null);
            } else {
                Log.e(TAG, "FirebaseSource reported failure updating points for user: " + userId, e);
                if (callback != null) callback.onComplete(false, e);
            }
        });
    }

    public void createUserProfile(String userId, String username, @Nullable UserOperationCallback callback) {
        Log.d(TAG, "Tworzenie nowego profilu dla użytkownika: " + userId + " z nazwą: " + username);
        if (username == null || username.trim().isEmpty()) {
            username = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        }
        UserProfile newProfile = new UserProfile(userId, username, 0, "en");
        // Użyj saveUserProfile z callbackiem, jeśli go ma, lub bezpośrednio FirebaseSource
        firebaseSource.setDocument("users", userId, newProfile,
                aVoid -> {
                    Log.d(TAG, "Profil utworzony pomyślnie dla: " + userId);
                    if (callback != null) callback.onComplete(true, null);
                },
                e -> {
                    Log.e(TAG, "Błąd tworzenia profilu dla: " + userId, e);
                    if (callback != null) callback.onComplete(false, e);
                });
    }

    // Przeciążona wersja createUserProfile dla prostoty, jeśli callback nie jest potrzebny bezpośrednio
    public void createUserProfile(String userId, String username) {
        createUserProfile(userId, username, null);
    }
    public void createUserProfile(String userId) {
        String defaultUsername = "User_" + userId.substring(0, Math.min(userId.length(), 5));
        createUserProfile(userId, defaultUsername, null);
    }


    public void updateUsername(String userId, String newUsername, @Nullable UserOperationCallback callback) {
        Log.d(TAG, "Aktualizacja nazwy użytkownika na: " + newUsername + " dla użytkownika: " + userId);
        if (userId == null || userId.isEmpty() || newUsername == null || newUsername.trim().isEmpty()) {
            Log.w(TAG, "Próba ustawienia pustej nazwy użytkownika lub nieprawidłowe userId.");
            if (callback != null) callback.onComplete(false, new IllegalArgumentException("User ID or new username is invalid"));
            return;
        }
        // Użyj metody updateField z FirebaseSource
        firebaseSource.updateField("users", userId, "username", newUsername, false,
                aVoid -> {
                    Log.d(TAG, "Nazwa użytkownika zaktualizowana pomyślnie dla: " + userId);
                    if (callback != null) callback.onComplete(true, null);
                },
                e -> {
                    Log.e(TAG, "Błąd aktualizacji nazwy użytkownika dla: " + userId, e);
                    if (callback != null) callback.onComplete(false, e);
                });
    }
}