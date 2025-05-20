package com.example.a404.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.ui.adapters.CourseAdapter;
import com.example.a404.ui.words.WordsActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements CourseAdapter.OnCourseClickListener {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CourseAdapter courseAdapter;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView wywołane");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        dbHelper = new WordDbHelper(requireContext());
        courseDao = new CourseDao(dbHelper);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated wywołane");

        setupRecyclerView();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Użytkownik zalogowany, ID: " + userId);
            homeViewModel.loadUserProfile(userId);
            showLoadingState(true);
        } else {
            Log.w(TAG, "User is not logged in");
            showErrorMessage("Error: User is not logged in.");
        }

        homeViewModel.getSelectedLanguageCode().observe(getViewLifecycleOwner(), languageCode -> {
            if (languageCode != null && !languageCode.isEmpty()) {
                Log.d(TAG, "Obserwator: Wybrany język zmieniony na: " + languageCode);
                loadCoursesAndUpdateAdapter(languageCode);
            } else {
                Log.d(TAG, "Obserwator: Kod języka jest null lub pusty");
                showErrorMessage("No language selected.");
                if (courseAdapter != null) {
                    courseAdapter.updateCourses(new ArrayList<>());
                }
            }
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView wywołane");
        if (binding == null) {
            Log.e(TAG, "Binding jest null w setupRecyclerView, nie można skonfigurować RecyclerView.");
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerViewCourses.setLayoutManager(layoutManager);
        binding.recyclerViewCourses.setHasFixedSize(true);
        courseAdapter = new CourseAdapter(new ArrayList<>(), this);
        binding.recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void loadCoursesAndUpdateAdapter(String languageCode) {
        Log.d(TAG, "Rozpoczynanie ładowania kursów dla języka: " + languageCode);
        if (binding == null) {
            Log.e(TAG, "Binding jest null w loadCoursesAndUpdateAdapter, przerwanie ładowania kursów.");
            return;
        }
        if (courseDao == null) {
            Log.e(TAG, "courseDao nie został zainicjalizowany!");
            showErrorMessage("Error: Cannot load course database.");
            return;
        }

        showLoadingState(true);

        executorService.execute(() -> {
            try {
                final List<Course> courses = courseDao.getAllCourses(languageCode);
                Log.d(TAG, "Wątek tła: Pobrano " + courses.size() + " kursów dla języka: " + languageCode);

                for (Course course : courses) {
                    List<Word> words = courseDao.getWordsForCourse(course.getId());
                    course.setWords(words);
                    Log.d(TAG, "Kurs " + course.getName() + " ma " + words.size() + " słów");
                }

                mainThreadHandler.post(() -> {
                    if (binding == null || getContext() == null) {
                        Log.w(TAG, "Binding lub Context jest null po operacji w tle, nie można zaktualizować UI.");
                        return;
                    }

                    if (courseAdapter != null) {
                        courseAdapter.updateCourses(courses);
                    }

                    showLoadingState(false);

                    if (courses.isEmpty()) {
                        Log.d(TAG, "Brak kursów dla języka: " + languageCode + ". Wyświetlanie komunikatu.");
                        showErrorMessage("No courses available for language: " + languageCode);
                    } else {
                        binding.textViewNoCourses.setVisibility(View.GONE);
                        binding.recyclerViewCourses.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Błąd podczas ładowania kursów", e);
                mainThreadHandler.post(() -> {
                    showLoadingState(false);
                    showErrorMessage("Error while loading courses: " + e.getMessage());
                });
            }
        });
    }

    private void showLoadingState(boolean isLoading) {
        if (binding != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.recyclerViewCourses.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            binding.textViewNoCourses.setVisibility(View.GONE);
        }
    }

    private void showErrorMessage(String message) {
        if (binding != null) {
            binding.textViewNoCourses.setText(message);
            binding.textViewNoCourses.setVisibility(View.VISIBLE);
            binding.recyclerViewCourses.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView wywołane");
        binding = null;
    }

    @Override
    public void onCourseClicked(Course course) {
        Log.d(TAG, "Course card clicked: " + course.getName() + " with ID: " + course.getId());
        if (getContext() == null) {
            Log.e(TAG, "Context is null in onCourseClicked, cannot show AlertDialog.");
            return;
        }

        final CharSequence[] items = {"Word Game", "Sentence Game"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose game type")
                .setItems(items, (dialog, which) -> {
                    Intent intent;
                    if (which == 0) {
                        intent = new Intent(requireContext(), WordGameActivity.class);
                        Log.d(TAG, "Wybrano 'Gra słowami'");
                    } else {
                        intent = new Intent(requireContext(), SentenceGameActivity.class);
                        Log.d(TAG, "Wybrano 'Gra zdań'");
                    }
                    intent.putExtra("COURSE_ID", course.getId());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onWordsIconClicked(Course course) {
        Log.d(TAG, "Words icon clicked for course: " + course.getName() + " with ID: " + course.getId());
        if (getContext() == null) {
            Log.e(TAG, "Context is null in onWordsIconClicked, cannot start WordsActivity.");
            return;
        }
        Intent intent = new Intent(requireContext(), WordsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }
}
