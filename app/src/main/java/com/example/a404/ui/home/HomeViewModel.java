package com.example.a404.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData; // Zmień import
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private final UserRepository userRepository;
    // Zmień typ na MediatorLiveData
    private final MediatorLiveData<UserProfile> userProfileLiveData = new MediatorLiveData<>();
    private final LiveData<String> selectedLanguageCode;

    public HomeViewModel() {
        this.userRepository = new UserRepository(new FirebaseSource());
        Log.d(TAG, "HomeViewModel zainicjalizowany");

        // Użyj Transformations.map do wyodrębnienia kodu języka z profilu
        selectedLanguageCode = Transformations.map(userProfileLiveData, profile -> {
            if (profile != null) {
                Log.d(TAG, "Transformacja: Uzyskano kod języka: " + profile.getSelectedLanguageCode());
                return profile.getSelectedLanguageCode();
            } else {
                Log.d(TAG, "Transformacja: Profil jest null, zwracam null dla języka");
                return null; // Lub domyślny język, jeśli preferowane
            }
        });
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfileLiveData;
    }

    public LiveData<String> getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public void loadUserProfile(String userId) {
        Log.d(TAG, "Rozpoczynam ładowanie profilu użytkownika: " + userId);
        // Pobierz LiveData z repozytorium
        LiveData<UserProfile> source = userRepository.getUserProfile(userId);

        // Dodaj źródło do userProfileLiveData, aby ViewModel mógł je obserwować
        userProfileLiveData.addSource(source, profile -> {
            if (profile != null) {
                Log.d(TAG, "Otrzymano aktualizację profilu w ViewModel, język: " + profile.getSelectedLanguageCode());
                userProfileLiveData.setValue(profile);
            } else {
                Log.w(TAG, "Otrzymano null jako profil użytkownika z repozytorium");
                // Możesz zdecydować, czy ustawić userProfileLiveData na null,
                // czy zachować poprzednią wartość, w zależności od logiki aplikacji.
                // userProfileLiveData.setValue(null);
            }
            // Opcjonalnie: usuń źródło po pierwszym załadowaniu, jeśli nie potrzebujesz dalszych aktualizacji
            // userProfileLiveData.removeSource(source);
        });
    }
}