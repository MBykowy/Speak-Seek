package com.example.a404.ui.language;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.Language;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionViewModel extends ViewModel {
    private static final String TAG = "LanguageVM";
    private UserRepository userRepository;
    private final MutableLiveData<Boolean> languageUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Language>> availableLanguages = new MutableLiveData<>();
    private final MutableLiveData<String> currentSelectedLanguage = new MutableLiveData<>("en");

    public LanguageSelectionViewModel() {
        this.userRepository = new UserRepository(new FirebaseSource());
        loadAvailableLanguages();
        Log.d(TAG, "ViewModel utworzony, domyślny język: " + currentSelectedLanguage.getValue());
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
        userRepository.updateSelectedLanguage(userId, languageCode);
        currentSelectedLanguage.setValue(languageCode);
        languageUpdateStatus.setValue(true);
    }

    public void loadUserLanguage(String userId) {
        Log.d(TAG, "Próba wczytania języka dla użytkownika: " + userId);
        userRepository.getUserProfile(userId).observeForever(userProfile -> {
            if (userProfile != null) {
                String languageCode = userProfile.getSelectedLanguageCode();
                Log.d(TAG, "Wczytany język: " + languageCode);
                currentSelectedLanguage.setValue(languageCode);
            } else {
                Log.d(TAG, "Brak profilu użytkownika");
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