// Ścieżka: app/java/com/example/a404/ui/profile/ProfileFragment.java
package com.example.a404.ui.profile;

import android.content.Context; // Dodaj dla onAttach
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
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.FragmentProfileBinding;
import com.example.a404.ui.adapters.AchievementsAdapter;

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

    // Inicjalizacja ViewModelu w onAttach lub onCreate
    // onAttach jest wywoływane przed onCreate
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG_PROFILE_FRAG, "onAttach");
        // Inicjalizacja ViewModelu tutaj jest bezpieczna, bo mamy już kontekst
        // Możesz to też zostawić w onCreate, jeśli wolisz
        FirebaseSource firebaseSource = new FirebaseSource();
        UserRepository userRepository = new UserRepository(firebaseSource);
        // GamificationRepository może być potrzebny, jeśli ProfileViewModel go używa
        // GamificationRepository gamificationRepository = new GamificationRepository(firebaseSource, requireActivity().getApplication());

        // Użyj fabryki do stworzenia ViewModelu
        // Przekaż requireActivity().getApplication() jako Application context
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                    // Jeśli ProfileViewModel potrzebuje GamificationRepository, przekaż go
                    // W naszym ostatnim setupie, ProfileViewModel go nie potrzebował bezpośrednio dla osiągnięć
                    return (T) new ProfileViewModel(requireActivity().getApplication(), userRepository /*, gamificationRepository */);
                }
                throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
            }
        }).get(ProfileViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_PROFILE_FRAG, "onCreate");
        // Jeśli ViewModel nie został zainicjalizowany w onAttach, zrób to tutaj.
        // W naszym przypadku jest już w onAttach.
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
        Log.d(TAG_PROFILE_FRAG, "onViewCreated");

        setupRecyclerView();
        setupObservers();
        // viewModel.init(); // <<< USUNIĘTO STĄD
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG_PROFILE_FRAG, "onResume - Initializing/Refreshing data");
        // Wywołaj init() za każdym razem, gdy fragment staje się widoczny
        // To zapewni odświeżenie danych
        if (viewModel != null) {
            viewModel.init();
        }
    }

    private void setupRecyclerView() {
        achievementAdapter = new AchievementsAdapter(getContext(), new ArrayList<>(), new HashSet<>());
        binding.recyclerAchievements.setAdapter(achievementAdapter);
        binding.recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAchievements.setHasFixedSize(true);
    }

    private void setupObservers() {
        // Obserwatory pozostają bez zmian, będą reagować na nowe dane z ViewModelu
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            Log.d(TAG_PROFILE_FRAG, "UserProfile updated in observer.");
            updateProfileUIData(userProfile);
        });

        viewModel.getAllDisplayAchievements().observe(getViewLifecycleOwner(), displayAchievementsList -> {
            // ... (logika aktualizacji adaptera osiągnięć bez zmian) ...
            Log.d(TAG_PROFILE_FRAG, "All achievements updated in observer. Count: " + (displayAchievementsList != null ? displayAchievementsList.size() : 0));
            boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();

            if (displayAchievementsList != null) {
                List<Achievement> definitions = new ArrayList<>();
                Set<String> unlockedIds = new HashSet<>();
                for (DisplayAchievementProfile item : displayAchievementsList) {
                    definitions.add(item.achievement);
                    if (item.isUnlocked) {
                        unlockedIds.add(item.achievement.getId());
                    }
                }

                if (definitions.isEmpty() && !isLoading) {
                    binding.textNoAchievements.setText("Brak dostępnych osiągnięć");
                    binding.textNoAchievements.setVisibility(View.VISIBLE);
                    binding.recyclerAchievements.setVisibility(View.GONE);
                } else if(!definitions.isEmpty()) {
                    binding.textNoAchievements.setVisibility(View.GONE);
                    binding.recyclerAchievements.setVisibility(View.VISIBLE);
                    achievementAdapter.updateData(definitions, unlockedIds);
                } else {
                    binding.recyclerAchievements.setVisibility(View.GONE);
                    if (!isLoading) binding.textNoAchievements.setVisibility(View.VISIBLE);
                }
            } else if (!isLoading) {
                binding.textNoAchievements.setText("Nie udało się załadować osiągnięć");
                binding.textNoAchievements.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // ... (logika paska postępu bez zmian) ...
            Log.d(TAG_PROFILE_FRAG, "isLoading state changed: " + isLoading);
            binding.progressAchievements.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.textNoAchievements.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });
    }

    private void updateProfileUIData(UserProfile userProfile) {
        // ... (bez zmian) ...
        if (userProfile != null) {
            binding.textUsername.setText(userProfile.getUsername());
            binding.textPoints.setText(String.valueOf(userProfile.getPoints()));
            binding.textStreak.setText(String.format(Locale.getDefault(), "%d dni", userProfile.getCurrentStreak()));
            String languageName = mapLanguageCodeToName(userProfile.getSelectedLanguageCode());
            binding.textLanguage.setText(languageName);
        } else {
            binding.textUsername.setText("Brak danych");
            binding.textPoints.setText("0");
            binding.textStreak.setText("0 dni");
            binding.textLanguage.setText("Nie wybrano");
        }
    }

    private String mapLanguageCodeToName(String languageCode) {
        // ... (bez zmian) ...
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