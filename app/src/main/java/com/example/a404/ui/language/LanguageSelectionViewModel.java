package com.example.a404.ui.language;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.Language;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionViewModel extends ViewModel {

    private UserRepository userRepository;
    private final MutableLiveData<Boolean> languageUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Language>> availableLanguages = new MutableLiveData<>();
    private final MutableLiveData<String> currentSelectedLanguage = new MutableLiveData<>("en");

    // Konstruktor bezargumentowy dla fabryki ViewModeli
    public LanguageSelectionViewModel() {
        // Inicjalizacja zależności
        this.userRepository = new UserRepository(new FirebaseSource());
        loadAvailableLanguages();
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
        userRepository.updateSelectedLanguage(userId, languageCode);
        currentSelectedLanguage.setValue(languageCode);
        languageUpdateStatus.setValue(true);
    }

    public void loadUserLanguage(String userId) {
        userRepository.getUserProfile(userId).observeForever(userProfile -> {
            if (userProfile != null) {
                String languageCode = userProfile.getSelectedLanguageCode();
                currentSelectedLanguage.setValue(languageCode);
            }
        });
    }

    private void loadAvailableLanguages() {
        // W rzeczywistej aplikacji można by pobrać to z API lub z lokalnej bazy danych
        List<Language> languages = new ArrayList<>();
        languages.add(new Language("en", "Angielski"));
        languages.add(new Language("pl", "Polski"));
        languages.add(new Language("de", "Niemiecki"));
        languages.add(new Language("es", "Hiszpański"));
        languages.add(new Language("fr", "Francuski"));
        availableLanguages.setValue(languages);
    }
}