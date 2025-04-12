package com.example.a404.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private UserRepository userRepository;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        // W rzeczywistej aplikacji userRepository powinien być wstrzykiwany przez DI
        userRepository = new UserRepository(new FirebaseSource());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        return userRepository.getUserProfile(userId);
    }
}