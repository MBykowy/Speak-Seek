// Ścieżka: app/java/com/example/a404/ui/dashboard/DashboardFragment.java
package com.example.a404.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Achievement;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.VocabularyItem;
import com.example.a404.data.repository.GamificationRepository;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.repository.VocabularyRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.FragmentDashboardBinding;
import com.example.a404.ui.adapters.AchievementsAdapter;
import com.example.a404.ui.adapters.CourseAdapter;
import com.example.a404.ui.home.WordGameActivity;
import com.example.a404.ui.words.WordsActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DashboardFragment extends Fragment implements CourseAdapter.OnCourseClickListener {
    private static final String TAG_DASH_FRAG = "DashboardFragment";

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private CourseAdapter courseAdapter;
    private AchievementsAdapter achievementAdapterDashboard;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_DASH_FRAG, "onCreate");

        FirebaseSource firebaseSource = new FirebaseSource();
        UserRepository userRepository = new UserRepository(firebaseSource);
        VocabularyRepository vocabularyRepository = new VocabularyRepository(firebaseSource);
        GamificationRepository gamificationRepository = new GamificationRepository(firebaseSource, requireActivity().getApplication());

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
                            return (T) new DashboardViewModel(
                                    requireActivity().getApplication(),
                                    userRepository,
                                    vocabularyRepository,
                                    gamificationRepository
                            );
                        }
                        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
                    }
                }
        ).get(DashboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG_DASH_FRAG, "onCreateView");
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_DASH_FRAG, "onViewCreated");

        setupRecyclerViews();
        setupObservers();
        setupButtonListeners();

        viewModel.init();
    }

    private void setupRecyclerViews() {
        // Adapter dla kursów/kategorii
        courseAdapter = new CourseAdapter(new ArrayList<>(), this);
        binding.recyclerCategories.setAdapter(courseAdapter);
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCategories.setHasFixedSize(true);

        // Adapter dla osiągnięć
        achievementAdapterDashboard = new AchievementsAdapter(getContext(), new ArrayList<>(), new HashSet<>());
        binding.recyclerAchievements.setAdapter(achievementAdapterDashboard);
        binding.recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerAchievements.setHasFixedSize(true);
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            Log.d(TAG_DASH_FRAG, "UserProfile updated in observer.");
            updateProfileUI(userProfile);
        });

        viewModel.getRecentCourses().observe(getViewLifecycleOwner(), courses -> {
            Log.d(TAG_DASH_FRAG, "Recent courses updated in observer. Count: " + (courses != null ? courses.size() : 0));
            if (courses != null) {
                courseAdapter.updateCourses(courses);
                binding.textEmptyCategories.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE); // <<< POPRAWKA ID
                binding.recyclerCategories.setVisibility(courses.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                courseAdapter.updateCourses(new ArrayList<>());
                binding.textEmptyCategories.setVisibility(View.VISIBLE); // <<< POPRAWKA ID
                binding.recyclerCategories.setVisibility(View.GONE);
            }
        });

        viewModel.getReviewWords().observe(getViewLifecycleOwner(), vocabularyItems -> {
            Log.d(TAG_DASH_FRAG, "Review words updated in observer. Count: " + (vocabularyItems != null ? vocabularyItems.size() : 0));
            updateReviewWordsUI(vocabularyItems);
        });

        viewModel.getRecentDisplayAchievements().observe(getViewLifecycleOwner(), displayAchievementsList -> {
            Log.d(TAG_DASH_FRAG, "Recent achievements updated in observer. Count: " + (displayAchievementsList != null ? displayAchievementsList.size() : 0));
            boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();

            if (displayAchievementsList != null) {
                List<Achievement> definitions = new ArrayList<>();
                Set<String> unlockedIds = new HashSet<>();
                for (DisplayDashboardAchievement item : displayAchievementsList) {
                    definitions.add(item.achievement);
                    if (item.isUnlocked) {
                        unlockedIds.add(item.achievement.getId());
                    }
                }

                if (definitions.isEmpty() && !isLoading) {
                    binding.textNoAchievements.setText("Brak osiągnięć do wyświetlenia");
                    binding.textNoAchievements.setVisibility(View.VISIBLE);
                    binding.recyclerAchievements.setVisibility(View.GONE);
                } else if(!definitions.isEmpty()){
                    binding.textNoAchievements.setVisibility(View.GONE);
                    binding.recyclerAchievements.setVisibility(View.VISIBLE);
                    achievementAdapterDashboard.updateData(definitions, unlockedIds);
                } else {
                    binding.recyclerAchievements.setVisibility(View.GONE);
                    if(!isLoading) binding.textNoAchievements.setVisibility(View.VISIBLE);
                }
            } else if (!isLoading) {
                binding.textNoAchievements.setText("Nie udało się załadować osiągnięć");
                binding.textNoAchievements.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG_DASH_FRAG, "isLoading state changed: " + isLoading);
            binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.recyclerCategories.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.GONE);
                binding.textEmptyCategories.setVisibility(View.GONE); // <<< POPRAWKA ID
                binding.textNoAchievements.setVisibility(View.GONE);
            }
        });
    }

    private void setupButtonListeners() {
        binding.buttonChangeLanguage.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_to_languageSelectionFragment));

        binding.buttonViewProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_to_navigation_profile));

        binding.buttonAllCategories.setOnClickListener(v -> {
            Log.d(TAG_DASH_FRAG, "Button All Categories clicked");
            // TODO: Zaimplementuj nawigację do pełnej listy kategorii/kursów
        });

        binding.buttonStartReview.setOnClickListener(v -> {
            Log.d(TAG_DASH_FRAG, "Button Start Review clicked");
            // TODO: Zaimplementuj nawigację do ekranu powtórki
        });
    }

    private void updateProfileUI(UserProfile userProfile) {
        if (userProfile != null) {
            binding.textWelcome.setText(String.format("Witaj, %s!", userProfile.getUsername()));
            binding.textPoints.setText(String.valueOf(userProfile.getPoints()));
            binding.textStreak.setText(String.format(Locale.getDefault(), "%d dni", userProfile.getCurrentStreak()));
            String languageName = mapLanguageCodeToName(userProfile.getSelectedLanguageCode());
            binding.textLanguage.setText(languageName);
        } else {
            binding.textWelcome.setText("Witaj!");
            binding.textPoints.setText("0");
            binding.textStreak.setText("0 dni");
            binding.textLanguage.setText("Nie wybrano");
        }
    }

    private void updateReviewWordsUI(List<VocabularyItem> wordsForReview) {
        if (wordsForReview != null) {
            int reviewCount = wordsForReview.size();
            binding.textReviewCount.setText(
                    String.format(Locale.getDefault(), "%d słów czeka na powtórkę", reviewCount));
            binding.buttonStartReview.setEnabled(reviewCount > 0);
        } else {
            binding.textReviewCount.setText("0 słów czeka na powtórkę");
            binding.buttonStartReview.setEnabled(false);
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
        Log.d(TAG_DASH_FRAG, "onDestroyView");
    }

    // === IMPLEMENTACJA METOD Z OnCourseClickListener ===
    @Override
    public void onCourseClicked(Course course) {
        Log.d(TAG_DASH_FRAG, "Course card clicked: " + course.getName() + " with ID: " + course.getId());
        Intent intent = new Intent(requireContext(), WordGameActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }

    @Override
    public void onWordsIconClicked(Course course) {
        Log.d(TAG_DASH_FRAG, "Words icon clicked for course: " + course.getName() + " with ID: " + course.getId());
        Intent intent = new Intent(requireContext(), WordsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }
}