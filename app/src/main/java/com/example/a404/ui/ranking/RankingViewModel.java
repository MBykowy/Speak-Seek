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

    public RankingViewModel() {
        userRepository = new UserRepository(new FirebaseSource());
        loadRanking(50);
    }

    public void loadRanking(int limit) {
        rankedUsers = userRepository.getRankedUsers(limit);
    }

    public LiveData<List<UserProfile>> getRankedUsers() {
        return rankedUsers;
    }
}
