package com.example.a404.ui.ranking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import java.util.List;

public class RankingViewModel extends ViewModel {

    private final UserRepository userRepository;
    private LiveData<List<UserProfile>> rankedUsers;
    private static final int DEFAULT_RANKING_LIMIT = 50; // Możesz użyć stałej

    public RankingViewModel() {
        userRepository = new UserRepository(new FirebaseSource());
        // Nie ładujemy tutaj celowo
    }

    // Ta metoda nadal jest publiczna, aby można było wymusić odświeżenie z onResume
    public void loadRanking(int limit) {
        // Pobieramy świeże LiveData z repozytorium
        rankedUsers = userRepository.getRankedUsers(limit);
    }

    public LiveData<List<UserProfile>> getRankedUsers() {
        // --- KLUCZOWA ZMIANA ---
        // Jeśli rankedUsers jest null (np. przy pierwszym wywołaniu z observeRankingData),
        // zainicjuj ładowanie z domyślnym limitem.
        if (rankedUsers == null) {
            loadRanking(DEFAULT_RANKING_LIMIT);
        }
        // Teraz mamy pewność, że rankedUsers nie jest null
        return rankedUsers;
    }
}