// Ścieżka: app/java/com/example/a404/ui/language/LanguageSelectionViewModel.java
package com.example.a404.ui.language;

import android.app.Application; // <<< ZMIEŃ IMPORT
import android.util.Log;
import android.widget.Toast;    // <<< DODAJ IMPORT

import androidx.annotation.NonNull; // <<< DODAJ IMPORT
import androidx.lifecycle.AndroidViewModel; // <<< ZMIEŃ NA AndroidViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel; // <<< USUŃ TEN IMPORT

import com.example.a404.data.model.Achievement; // <<< DODAJ IMPORT
import com.example.a404.data.model.Language;
import com.example.a404.data.model.UserProfile;   // <<< DODAJ IMPORT
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.service.AchievementHelper; // <<< DODAJ IMPORT
import com.google.firebase.auth.FirebaseAuth;       // <<< DODAJ IMPORT
import com.google.firebase.auth.FirebaseUser;       // <<< DODAJ IMPORT


import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionViewModel extends AndroidViewModel { // <<< ZMIEŃ NA AndroidViewModel
    private static final String TAG = "LanguageVM";
    private UserRepository userRepository;
    private FirebaseSource firebaseSource;        // <<< DODAJ POLE
    private AchievementHelper achievementHelper;  // <<< DODAJ POLE

    private final MutableLiveData<Boolean> languageUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Language>> availableLanguages = new MutableLiveData<>();
    private final MutableLiveData<String> currentSelectedLanguage = new MutableLiveData<>(); // Zmiana nazwy na currentSelectedLanguage
    private final MutableLiveData<UserProfile> _currentUserProfile = new MutableLiveData<>(); // Do przechowywania profilu


    public LanguageSelectionViewModel(@NonNull Application application) { // <<< ZMIEŃ KONSTRUKTOR
        super(application);
        this.firebaseSource = new FirebaseSource(); // Inicjalizacja FirebaseSource
        this.userRepository = new UserRepository(firebaseSource); // Przekaż instancję firebaseSource
        this.achievementHelper = new AchievementHelper(application.getApplicationContext(), firebaseSource); // Inicjalizacja AchievementHelper
        loadAvailableLanguages();
        Log.d(TAG, "ViewModel utworzony.");
        // Wywołaj wczytywanie języka użytkownika tutaj, jeśli fragment tego nie robi, lub upewnij się, że fragment to robi
        // loadUserLanguageOnStart(); // Rozważ wywołanie tego w konstruktorze lub z fragmentu
    }

    public LiveData<Boolean> getLanguageUpdateStatus() {
        return languageUpdateStatus;
    }

    public LiveData<List<Language>> getAvailableLanguages() {
        return availableLanguages;
    }

    public LiveData<String> getCurrentSelectedLanguage() { // Zmieniono nazwę gettera
        return currentSelectedLanguage;
    }

    public void selectLanguage(String userId, String languageCode) {
        Log.d(TAG, "Zapisuję język dla użytkownika: " + userId + ", język: " + languageCode);

        // Krok 1: Wywołaj aktualizację w UserRepository (bezpośredniego callbacka o sukcesie zapisu nie mamy)
        userRepository.updateSelectedLanguage(userId, languageCode);

        // Krok 2: Lokalnie zaktualizuj wybrany język i status
        currentSelectedLanguage.setValue(languageCode);
        languageUpdateStatus.setValue(true); // Zakładamy, że się udało, bo nie mamy bezpośredniego feedbacku

        // Krok 3: Dodaj ten język do listy 'languagesStartedIds' w Firestore
        // Ta operacja jest ważna dla triggera 'LANGUAGES_STARTED'
        firebaseSource.addUserStartedLanguage(userId, languageCode, (opSuccess, opError) -> {
            if (opSuccess) {
                Log.d(TAG, "Language " + languageCode + " successfully added/ensured in languagesStartedIds.");
            } else {
                Log.e(TAG, "Failed to add language " + languageCode + " to languagesStartedIds.", opError);
            }
            // Niezależnie od wyniku addUserStartedLanguage (choć idealnie byłoby po sukcesie),
            // pobierz profil i sprawdź osiągnięcia.
            // Jeśli addUserStartedLanguage się nie powiedzie, profil może nie być w 100% aktualny
            // dla tego konkretnego triggera, ale spróbujmy.
            fetchProfileAndCheckAchievements(userId, "Po próbie wyboru języka");
        });
    }

    // Metoda pomocnicza do pobierania profilu i sprawdzania osiągnięć
    private void fetchProfileAndCheckAchievements(String userId, String contextMessage) {
        firebaseSource.getUserProfile(userId, (profile, e) -> {
            if (e != null || profile == null) {
                Log.e(TAG, contextMessage + " - Error fetching user profile to check achievements.", e);
                // Można by tu ustawić jakiś status błędu dla UI, jeśli potrzebne
                return;
            }
            _currentUserProfile.postValue(profile); // Zaktualizuj LiveData profilu

            Log.d(TAG, contextMessage + " - Checking achievements for user: " + profile.getUsername());
            achievementHelper.checkAndUnlockAchievements(profile, (newlyUnlocked, error) -> {
                if (error != null) {
                    Log.e(TAG, contextMessage + " - Error checking achievements", error);
                    return;
                }
                if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
                    for (Achievement ach : newlyUnlocked) {
                        // Sprawdź, czy to osiągnięcie "first_language_choice"
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


    public void loadUserLanguage(String userId) { // Nazwa zmieniona na loadUserLanguage, aby była bardziej ogólna
        Log.d(TAG, "Próba wczytania profilu i języka dla użytkownika: " + userId);
        // Nadal używamy userRepository.getUserProfile, które zwraca LiveData.
        // Aby uzyskać pojedynczą wartość UserProfile, musimy go obserwować.
        // Alternatywnie, FirebaseSource.getUserProfile z callbackiem byłby lepszy tutaj,
        // aby uniknąć wielokrotnych wywołań observeForever.
        // Dla minimalnych zmian, zostawmy observeForever, ale fragment powinien nim zarządzać.
        // LUB, jeśli fragment już obserwuje, to _currentUserProfile powinien być aktualizowany tam.

        // Lepsze podejście: użyj FirebaseSource.getUserProfile bezpośrednio tutaj, jeśli chcesz uniknąć observeForever
        firebaseSource.getUserProfile(userId, (profile, e) -> {
            if (e != null) {
                Log.e(TAG, "Error loading user profile on start.", e);
                currentSelectedLanguage.postValue("en"); // Domyślny w razie błędu
                _currentUserProfile.postValue(null);
                return;
            }
            if (profile != null) {
                _currentUserProfile.postValue(profile); // Zapisz cały profil
                String languageCode = profile.getSelectedLanguageCode();
                Log.d(TAG, "Wczytany profil, język: " + (languageCode != null ? languageCode : "null"));
                if (languageCode != null && !languageCode.isEmpty()) {
                    currentSelectedLanguage.postValue(languageCode);
                } else {
                    currentSelectedLanguage.postValue("en"); // Domyślny, jeśli brak zapisanego
                    Log.d(TAG, "User has no selected language, defaulting to 'en'.");
                }
            } else {
                Log.d(TAG, "Brak profilu użytkownika przy starcie, UID: " + userId);
                currentSelectedLanguage.postValue("en"); // Domyślny, jeśli brak profilu
                _currentUserProfile.postValue(null);
            }
        });
    }

    private void loadAvailableLanguages() {
        List<Language> languages = new ArrayList<>();
        languages.add(new Language("en", "Angielski"));
        languages.add(new Language("pl", "Polski"));
        languages.add(new Language("de", "Niemiecki"));
        languages.add(new Language("es", "Hiszpański"));
        languages.add(new Language("fr", "Francuski"));
        availableLanguages.setValue(languages);
        Log.d(TAG, "Załadowano " + languages.size() + " języków do nauki");
    }
}