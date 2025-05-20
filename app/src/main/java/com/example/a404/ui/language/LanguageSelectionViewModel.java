// Ścieżka: app/java/com/example/a404/ui/language/LanguageSelectionViewModel.java
package com.example.a404.ui.language;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.R; // <<< DODAJ IMPORT DLA R.drawable
import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.Language;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.service.AchievementHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionViewModel extends AndroidViewModel {
    private static final String TAG = "LanguageVM";
    private UserRepository userRepository;
    private FirebaseSource firebaseSource;
    private AchievementHelper achievementHelper;

    private final MutableLiveData<Boolean> languageUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Language>> availableLanguages = new MutableLiveData<>();
    private final MutableLiveData<String> currentSelectedLanguage = new MutableLiveData<>();
    private final MutableLiveData<UserProfile> _currentUserProfile = new MutableLiveData<>();

    public LanguageSelectionViewModel(@NonNull Application application) {
        super(application);
        this.firebaseSource = new FirebaseSource();
        this.userRepository = new UserRepository(firebaseSource);
        this.achievementHelper = new AchievementHelper(application.getApplicationContext(), firebaseSource);
        loadAvailableLanguages(); // Ta metoda zostanie zaktualizowana
        Log.d(TAG, "ViewModel utworzony.");
    }

    public LiveData<Boolean> getLanguageUpdateStatus() {
        return languageUpdateStatus;
    }

    public LiveData<List<Language>> getAvailableLanguages() {
        return availableLanguages;
    }

    public LiveData<String> getCurrentSelectedLanguage() {
        return currentSelectedLanguage;
    }

    public void selectLanguage(String userId, String languageCode) {
        Log.d(TAG, "Zapisuję język dla użytkownika: " + userId + ", język: " + languageCode);
        userRepository.updateSelectedLanguage(userId, languageCode, new UserRepository.UserOperationCallback() {
            @Override
            public void onComplete(boolean success, @Nullable Exception e) {
                if (success) {
                    Log.d(TAG, "Selected language updated in Firestore successfully.");
                    currentSelectedLanguage.postValue(languageCode);
                    languageUpdateStatus.postValue(true);
                    firebaseSource.addUserStartedLanguage(userId, languageCode, (opSuccess, opError) -> {
                        if (opSuccess) {
                            Log.d(TAG, "Language " + languageCode + " successfully added/ensured in languagesStartedIds.");
                        } else {
                            Log.e(TAG, "Failed to add language " + languageCode + " to languagesStartedIds.", opError);
                        }
                        fetchProfileAndCheckAchievements(userId, "Po próbie wyboru języka");
                    });
                } else {
                    Log.e(TAG, "Failed to update selected language in Firestore.", e);
                    languageUpdateStatus.postValue(false);
                    Toast.makeText(getApplication(), "Błąd zapisu wybranego języka.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchProfileAndCheckAchievements(String userId, String contextMessage) {
        firebaseSource.getUserProfile(userId, (profile, e) -> {
            if (e != null || profile == null) {
                Log.e(TAG, contextMessage + " - Error fetching user profile to check achievements.", e);
                return;
            }
            _currentUserProfile.postValue(profile);
            Log.d(TAG, contextMessage + " - Checking achievements for user: " + profile.getUsername());
            achievementHelper.checkAndUnlockAchievements(profile, (newlyUnlocked, error) -> {
                if (error != null) {
                    Log.e(TAG, contextMessage + " - Error checking achievements", error);
                    return;
                }
                if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
                    for (Achievement ach : newlyUnlocked) {
                        if ("LANGUAGES_STARTED".equals(ach.getTriggerType()) &&
                                ach.getTriggerValue() instanceof Number &&
                                ((Number) ach.getTriggerValue()).intValue() == 1) {
                            Log.i(TAG, "Achievement UNLOCKED: " + ach.getName());
                            Toast.makeText(getApplication(), "Osiągnięcie: " + ach.getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.d(TAG, contextMessage + " - No new achievements unlocked.");
                }
            });
        });
    }

    public void loadUserLanguage(String userId) {
        Log.d(TAG, "Próba wczytania profilu i języka dla użytkownika: " + userId);
        firebaseSource.getUserProfile(userId, (profile, e) -> {
            if (e != null) {
                Log.e(TAG, "Error loading user profile on start.", e);
                currentSelectedLanguage.postValue("en"); // Domyślny język
                _currentUserProfile.postValue(null);
                return;
            }
            if (profile != null) {
                _currentUserProfile.postValue(profile);
                String languageCode = profile.getSelectedLanguageCode();
                Log.d(TAG, "Wczytany profil, język: " + (languageCode != null ? languageCode : "null"));
                if (languageCode != null && !languageCode.isEmpty()) {
                    currentSelectedLanguage.postValue(languageCode);
                } else {
                    currentSelectedLanguage.postValue("en"); // Domyślny język, jeśli brak
                    Log.d(TAG, "User has no selected language, defaulting to 'en'.");
                }
            } else {
                Log.d(TAG, "Brak profilu użytkownika przy starcie, UID: " + userId);
                currentSelectedLanguage.postValue("en"); // Domyślny język
                _currentUserProfile.postValue(null);
            }
        });
    }

    private void loadAvailableLanguages() {
        List<Language> languages = new ArrayList<>();
        // <<< ZAKTUALIZOWANA SEKCJA - DODAJEMY ID ZASOBÓW FLAG >>>
        languages.add(new Language("en", "Angielski", R.drawable.ic_flag_uk)); // Zakładając, że masz ic_flag_uk
        languages.add(new Language("pl", "Polski", R.drawable.ic_flag_poland));
        languages.add(new Language("de", "Niemiecki", R.drawable.ic_flag_germany));
        languages.add(new Language("es", "Hiszpański", R.drawable.ic_flag_spain));
        languages.add(new Language("fr", "Francuski", R.drawable.ic_flag_france));
        // Dodaj więcej języków z odpowiednimi flagami, jeśli są dostępne
        // Jeśli jakaś flaga nie istnieje, możesz przekazać 0 lub ID placeholdera
        // np. languages.add(new Language("it", "Włoski", R.drawable.ic_flag_placeholder));
        availableLanguages.setValue(languages);
        Log.d(TAG, "Załadowano " + languages.size() + " języków do nauki");
    }
}