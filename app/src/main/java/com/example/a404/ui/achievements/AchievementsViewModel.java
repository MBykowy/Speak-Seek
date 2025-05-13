// Ścieżka: app/java/com/example/a404/ui/achievements/AchievementsViewModel.java
package com.example.a404.ui.achievements;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UnlockedAchievement;
import com.example.a404.data.source.FirebaseSource; // Załóżmy, że masz go w tej ścieżce
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementsViewModel extends AndroidViewModel {
    private static final String TAG = "AchievementsViewModel";
    private FirebaseSource firebaseSource;

    private MutableLiveData<List<Achievement>> _allAchievementDefinitions = new MutableLiveData<>();
    public LiveData<List<Achievement>> allAchievementDefinitions = _allAchievementDefinitions;

    private MutableLiveData<Set<String>> _unlockedAchievementIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> unlockedAchievementIds = _unlockedAchievementIds;

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public AchievementsViewModel(@NonNull Application application) {
        super(application);
        // Załóżmy, że FirebaseSource ma konstruktor bezargumentowy lub jest singletonem
        // Jeśli FirebaseSource wymaga kontekstu, przekaż go: new FirebaseSource(application);
        this.firebaseSource = new FirebaseSource();
    }

    public void loadAchievementsData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            _errorMessage.postValue("Użytkownik nie jest zalogowany.");
            _isLoading.postValue(false);
            return;
        }
        String userId = currentUser.getUid();
        _isLoading.postValue(true);
        _errorMessage.setValue(null); // Wyczyść poprzedni błąd

        // Krok 1: Pobierz wszystkie definicje osiągnięć
        firebaseSource.getAllAchievementDefinitions((definitions, e1) -> {
            if (e1 != null || definitions == null) {
                Log.e(TAG, "Error loading achievement definitions", e1);
                _errorMessage.postValue("Nie udało się załadować listy osiągnięć. Spróbuj ponownie.");
                _allAchievementDefinitions.postValue(new ArrayList<>()); // Ustaw pustą listę w razie błędu
                _isLoading.postValue(false); // Zakończ ładowanie nawet przy błędzie
                return;
            }
            _allAchievementDefinitions.postValue(definitions);

            // Krok 2: Pobierz odblokowane osiągnięcia użytkownika
            firebaseSource.getUserUnlockedAchievements(userId, (unlockedList, e2) -> {
                _isLoading.postValue(false); // Zakończ ładowanie po obu operacjach
                if (e2 != null) {
                    Log.e(TAG, "Error loading user's unlocked achievements", e2);
                    _errorMessage.postValue("Nie udało się załadować Twoich postępów. Osiągnięcia mogą być nieaktualne.");
                    // Mimo błędu, pozwalamy na wyświetlenie definicji, ale z pustą listą odblokowanych
                    _unlockedAchievementIds.postValue(new HashSet<>());
                    return;
                }

                Set<String> ids = new HashSet<>();
                if (unlockedList != null) {
                    for (UnlockedAchievement unlocked : unlockedList) {
                        if (unlocked.getAchievementId() != null) {
                            ids.add(unlocked.getAchievementId());
                        }
                    }
                }
                _unlockedAchievementIds.postValue(ids);
            });
        });
    }
}