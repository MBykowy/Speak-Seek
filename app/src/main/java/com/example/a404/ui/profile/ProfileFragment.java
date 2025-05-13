package com.example.a404.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.FragmentProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementAdapter achievementAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicjalizacja repozytoriów
        FirebaseSource firebaseSource = new FirebaseSource();
        UserRepository userRepository = new UserRepository(firebaseSource);
        GamificationRepository gamificationRepository = new GamificationRepository(firebaseSource);

        // Inicjalizacja ViewModel z niestandardową fabryką
        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProfileViewModel(userRepository, gamificationRepository);
                    }
                }
        ).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupObservers();

        // Inicjalizacja ViewModel
        viewModel.init();
    }

    private void setupRecyclerView() {
        achievementAdapter = new AchievementAdapter(new ArrayList<>());
        binding.recyclerAchievements.setAdapter(achievementAdapter);
        binding.recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupObservers() {
        // Obserwuj profil użytkownika
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateProfileUI);

        // Obserwuj osiągnięcia
        viewModel.getUserAchievements().observe(getViewLifecycleOwner(), this::updateAchievementsUI);

        // Obserwuj stan ładowania
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressAchievements.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void updateProfileUI(UserProfile userProfile) {
        if (userProfile != null) {
            binding.textUsername.setText(userProfile.getUsername());
            binding.textPoints.setText(String.valueOf(userProfile.getPoints()));
            binding.textStreak.setText(String.format(Locale.getDefault(), "%d dni", userProfile.getCurrentStreak()));

            // Mapowanie kodu języka na nazwę
            String languageName = mapLanguageCodeToName(userProfile.getSelectedLanguageCode());
            binding.textLanguage.setText(languageName);
        }
    }

    private void updateAchievementsUI(List<Achievement> achievements) {
        if (achievements != null) {
            if (achievements.isEmpty()) {
                binding.textNoAchievements.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            } else {
                binding.textNoAchievements.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.VISIBLE);
                achievementAdapter.setAchievements(achievements);
            }
        }
    }

    private String mapLanguageCodeToName(String languageCode) {
        // Prosta mapa kodów języków na nazwy
        switch (languageCode) {
            case "en": return "angielski";
            case "de": return "niemiecki";
            case "fr": return "francuski";
            case "es": return "hiszpański";
            case "it": return "włoski";
            case "ru": return "rosyjski";
            case "pl": return "polski";
            default: return languageCode;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}