// Ścieżka: app/java/com/example/a404/ui/profile/ProfileFragment.java
package com.example.a404.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
// import com.example.a404.data.repository.GamificationRepository; // Usunięty, jeśli ProfileViewModel go nie używa
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.FragmentProfileBinding;
import com.example.a404.ui.adapters.AchievementsAdapter; // Upewnij się, że to poprawny adapter

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProfileFragment extends Fragment {
    private static final String TAG_PROFILE_FRAG = "ProfileFragment";

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementsAdapter achievementAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG_PROFILE_FRAG, "onAttach");

        // Inicjalizacja ViewModelu
        // ViewModelProvider zachowa instancję ViewModelu przez cykl życia fragmentu
        // (nawet jeśli widok jest niszczony i tworzony ponownie).
        FirebaseSource firebaseSource = new FirebaseSource();
        UserRepository userRepository = new UserRepository(firebaseSource);
        // Jeśli ProfileViewModel potrzebuje GamificationRepository, przekaż go tutaj
        // GamificationRepository gamificationRepository = new GamificationRepository(firebaseSource, requireActivity().getApplication());

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                    // Przekaż Application i UserRepository
                    return (T) new ProfileViewModel(requireActivity().getApplication(), userRepository /*, gamificationRepository - jeśli potrzebny */);
                }
                throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
            }
        }).get(ProfileViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_PROFILE_FRAG, "onCreate");
        // ViewModel jest już zainicjalizowany w onAttach
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG_PROFILE_FRAG, "onCreateView");
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_PROFILE_FRAG, "onViewCreated - setting up UI");
        setupRecyclerView();
        setupObservers();
        // Nie wywołujemy tutaj loadDataForCurrentUser(), robimy to w onResume
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG_PROFILE_FRAG, "onResume - Calling loadDataForCurrentUser() to refresh data");
        if (viewModel != null) {
            viewModel.loadDataForCurrentUser(); // Wywołaj metodę ładującą/odświeżającą dane
        }
    }

    private void setupRecyclerView() {
        // Użyj poprawnego adaptera (com.example.a404.ui.adapters.AchievementsAdapter)
        achievementAdapter = new AchievementsAdapter(getContext(), new ArrayList<>(), new HashSet<>());
        binding.recyclerAchievements.setAdapter(achievementAdapter);
        binding.recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAchievements.setHasFixedSize(true);
    }

    private void setupObservers() {
        // Obserwuj profil użytkownika
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            Log.d(TAG_PROFILE_FRAG, "UserProfile LiveData updated in observer.");
            updateProfileUIData(userProfile);
        });

        // Obserwuj listę osiągnięć do wyświetlenia
        viewModel.getAllDisplayAchievements().observe(getViewLifecycleOwner(), displayAchievementsList -> {
            Log.d(TAG_PROFILE_FRAG, "AllDisplayAchievements LiveData updated. Count: " + (displayAchievementsList != null ? displayAchievementsList.size() : "null"));
            boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();

            if (displayAchievementsList != null) {
                List<Achievement> definitions = new ArrayList<>();
                Set<String> unlockedIds = new HashSet<>();
                for (DisplayAchievementProfile item : displayAchievementsList) { // Użyj DisplayAchievementProfile z ViewModelu
                    definitions.add(item.achievement);
                    if (item.isUnlocked) {
                        unlockedIds.add(item.achievement.getId());
                    }
                }

                if (definitions.isEmpty() && !isLoading) {
                    binding.textNoAchievements.setText("Brak dostępnych osiągnięć");
                    binding.textNoAchievements.setVisibility(View.VISIBLE);
                    binding.recyclerAchievements.setVisibility(View.GONE);
                } else if (!definitions.isEmpty()) {
                    binding.textNoAchievements.setVisibility(View.GONE);
                    binding.recyclerAchievements.setVisibility(View.VISIBLE);
                    achievementAdapter.updateData(definitions, unlockedIds);
                } else { // definitions są puste, ale isLoading może być true
                    binding.recyclerAchievements.setVisibility(View.GONE);
                    if (!isLoading) binding.textNoAchievements.setVisibility(View.VISIBLE);
                }
            } else if (!isLoading) { // displayAchievementsList jest null i nie ładujemy
                binding.textNoAchievements.setText("Nie udało się załadować osiągnięć");
                binding.textNoAchievements.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });

        // Obserwuj stan ładowania
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG_PROFILE_FRAG, "isLoading LiveData changed: " + isLoading);
            binding.progressAchievements.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.textNoAchievements.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
            // Widoczność RecyclerView i textNoAchievements jest głównie zarządzana
            // w observerze getAllDisplayAchievements po zakończeniu ładowania.
        });
    }

    private void updateProfileUIData(UserProfile userProfile) {
        if (userProfile != null) {
            Log.d(TAG_PROFILE_FRAG, "Updating UI with UserProfile: " + userProfile.getUsername());
            binding.textUsername.setText(userProfile.getUsername());
            binding.textPoints.setText(String.valueOf(userProfile.getPoints()));
            // Upewnij się, że UserProfile.java ma metodę getCurrentStreak()
            binding.textStreak.setText(String.format(Locale.getDefault(), "%d dni", userProfile.getCurrentStreak()));
            String languageName = mapLanguageCodeToName(userProfile.getSelectedLanguageCode());
            binding.textLanguage.setText(languageName);
        } else {
            Log.w(TAG_PROFILE_FRAG, "Updating UI: UserProfile is null. Setting default texts.");
            binding.textUsername.setText("Brak danych");
            binding.textPoints.setText("0");
            binding.textStreak.setText("0 dni");
            binding.textLanguage.setText("Nie wybrano");
        }
    }

    private String mapLanguageCodeToName(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) return "Nie wybrano";
        switch (languageCode) {
            case "en": return "Angielski";
            case "de": return "Niemiecki";
            case "fr": return "Francuski";
            case "es": return "Hiszpański";
            case "pl": return "Polski";
            default: return languageCode;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG_PROFILE_FRAG, "onDestroyView");
    }
}