package com.example.a404.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.a404.R;
import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.VocabularyItem;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.repository.VocabularyRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.FragmentDashboardBinding;
import com.example.a404.ui.adapters.CourseAdapter;
import com.example.a404.ui.profile.AchievementAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private CourseAdapter categoryAdapter;
    private AchievementAdapter achievementAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicjalizacja repozytoriów
        FirebaseSource firebaseSource = new FirebaseSource();
        UserRepository userRepository = new UserRepository(firebaseSource);
        VocabularyRepository vocabularyRepository = new VocabularyRepository(firebaseSource);
        GamificationRepository gamificationRepository = new GamificationRepository(firebaseSource);

        // Inicjalizacja ViewModel z niestandardową fabryką
        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new DashboardViewModel(
                                userRepository,
                                vocabularyRepository,
                                gamificationRepository);
                    }
                }
        ).get(DashboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupObservers();
        setupButtonListeners();

        // Inicjalizacja ViewModel
        viewModel.init();
    }

    private void setupRecyclerViews() {

        binding.recyclerCategories.setAdapter(categoryAdapter);
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Konfiguracja adaptera dla osiągnięć
        achievementAdapter = new AchievementAdapter(new ArrayList<>());
        binding.recyclerAchievements.setAdapter(achievementAdapter);
        binding.recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupObservers() {
        // Obserwuj profil użytkownika
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateProfileUI);


        // Obserwuj osiągnięcia
        viewModel.getRecentAchievements().observe(getViewLifecycleOwner(), this::updateAchievementsUI);

        // Obserwuj stan ładowania
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupButtonListeners() {
        // Przycisk zmiany języka
        binding.buttonChangeLanguage.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_to_languageSelectionFragment);
        });

        // Przycisk profilu
        binding.buttonViewProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_to_navigation_profile);
        });

        // Przycisk wszystkich kategorii
        binding.buttonAllCategories.setOnClickListener(v -> {
            // TODO: Dodać akcję nawigacji do pełnej listy kategorii
        });

        // Przycisk rozpoczęcia powtórki
        binding.buttonStartReview.setOnClickListener(v -> {
            // TODO: Dodać akcję nawigacji do ekranu powtórki
        });
    }

    private void updateProfileUI(UserProfile userProfile) {
        if (userProfile != null) {
            binding.textWelcome.setText(String.format("Witaj, %s!", userProfile.getUsername()));
            binding.textPoints.setText(String.valueOf(userProfile.getPoints()));
            binding.textStreak.setText(String.format(Locale.getDefault(), "%d dni", userProfile.getCurrentStreak()));

            // Mapowanie kodu języka na nazwę
            String languageName = mapLanguageCodeToName(userProfile.getSelectedLanguageCode());
            binding.textLanguage.setText(languageName);
        }
    }


    private void updateReviewWordsUI(List<VocabularyItem> wordsForReview) {
        if (wordsForReview != null) {
            int reviewCount = wordsForReview.size();
            binding.textReviewCount.setText(
                    String.format(Locale.getDefault(), "%d słów oczekuje na powtórkę", reviewCount));

            // Włącz/wyłącz przycisk powtórki w zależności od liczby słówek
            binding.buttonStartReview.setEnabled(reviewCount > 0);
        } else {
            binding.textReviewCount.setText("0 słów oczekuje na powtórkę");
            binding.buttonStartReview.setEnabled(false);
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
        } else {
            binding.textNoAchievements.setVisibility(View.VISIBLE);
            binding.recyclerAchievements.setVisibility(View.GONE);
        }
    }

    private String mapLanguageCodeToName(String languageCode) {
        // Prosta mapa kodów języków na nazwy
        if (languageCode == null) return "nie wybrano";

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