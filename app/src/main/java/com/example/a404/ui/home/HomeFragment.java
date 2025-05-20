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
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.ui.adapters.CourseAdapter;
import com.example.a404.ui.words.WordsActivity;
// import com.example.a404.ui.home.SentenceGameActivity; // Jeśli SentenceGameActivity jest w tym samym pakiecie, ten import jest OK
// Jeśli jest w innym pakiecie, np. com.example.a404.ui.games, zmień import:
import com.example.a404.ui.home.SentenceGameActivity; // Zakładając, że jest w tym pakiecie

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
        } else {
            Log.w(TAG, "Użytkownik nie jest zalogowany");
            // TODO: Obsłuż przekierowanie do logowania
        }

        homeViewModel.getSelectedLanguageCode().observe(getViewLifecycleOwner(), languageCode -> {
            if (languageCode != null && !languageCode.isEmpty()) {
                Log.d(TAG, "Obserwator: Wybrany język zmieniony na: " + languageCode);
                loadCoursesAndUpdateAdapter(languageCode);
            } else {
                Log.d(TAG, "Obserwator: Kod języka jest null lub pusty");
                if (binding != null) { // Dodano sprawdzenie binding != null
                    binding.textViewNoCourses.setVisibility(View.VISIBLE);
                    binding.textViewNoCourses.setText(R.string.language_not_selected);
                    binding.recyclerViewCourses.setVisibility(View.GONE);
                }
                if (courseAdapter != null) {
                    courseAdapter.updateCourses(new ArrayList<>());
                }
            }
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView wywołane");
        // Dodano sprawdzenie binding != null
        if (binding == null) {
            Log.e(TAG, "Binding jest null w setupRecyclerView, nie można skonfigurować RecyclerView.");
            return;
        }
        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCourses.setHasFixedSize(true);
        courseAdapter = new CourseAdapter(new ArrayList<>(), this);
        binding.recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void loadCoursesAndUpdateAdapter(String languageCode) {
        Log.d(TAG, "Rozpoczynanie ładowania kursów dla języka: " + languageCode);
        // Dodano sprawdzenie binding != null
        if (binding == null) {
            Log.e(TAG, "Binding jest null w loadCoursesAndUpdateAdapter, przerwanie ładowania kursów.");
            return;
        }
        if (courseDao == null) {
            Log.e(TAG, "courseDao nie został zainicjalizowany!");
            binding.textViewNoCourses.setVisibility(View.VISIBLE);
            binding.textViewNoCourses.setText("Błąd: Nie można załadować DAO.");
            binding.recyclerViewCourses.setVisibility(View.GONE);
            return;
        }

        binding.recyclerViewCourses.setVisibility(View.GONE);
        binding.textViewNoCourses.setVisibility(View.GONE);

        executorService.execute(() -> {
            final List<Course> courses = courseDao.getAllCourses(languageCode);
            Log.d(TAG, "Wątek tła: Pobrano " + courses.size() + " kursów dla języka: " + languageCode);

            mainThreadHandler.post(() -> {
                if (binding == null || getContext() == null) { // Dodano sprawdzenie getContext()
                    Log.w(TAG, "Binding lub Context jest null po operacji w tle, nie można zaktualizować UI.");
                    return;
                }

                if (courseAdapter != null) {
                    courseAdapter.updateCourses(courses);
                }

                if (courses.isEmpty()) {
                    Log.d(TAG, "Brak kursów dla języka: " + languageCode + ". Wyświetlanie komunikatu.");
                    binding.textViewNoCourses.setText(getString(R.string.no_courses_available, languageCode));
                    binding.textViewNoCourses.setVisibility(View.VISIBLE);
                    binding.recyclerViewCourses.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Znaleziono kursy. Ukrywanie komunikatu.");
                    binding.textViewNoCourses.setVisibility(View.GONE);
                    binding.recyclerViewCourses.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView wywołane");
        binding = null; // Kluczowe dla uniknięcia wycieków pamięci z ViewBinding
    }

    @Override
    public void onCourseClicked(Course course) {
        Log.d(TAG, "Course card clicked: " + course.getName() + " with ID: " + course.getId());
        if (getContext() == null) { // Sprawdzenie kontekstu przed użyciem
            Log.e(TAG, "Context is null in onCourseClicked, cannot show AlertDialog.");
            return;
        }

        final CharSequence[] items = {"Gra słowami", "Gra zdań"};
        new AlertDialog.Builder(requireContext()) // requireContext() powinno być bezpieczne, jeśli getContext() != null
                .setTitle("Wybierz rodzaj gry")
                .setItems(items, (dialog, which) -> {
                    Intent intent;
                    if (which == 0) { // Gra słowami
                        intent = new Intent(requireContext(), WordGameActivity.class);
                        Log.d(TAG, "Wybrano 'Gra słowami'");
                    } else { // Gra zdań
                        intent = new Intent(requireContext(), SentenceGameActivity.class); // POPRAWIONO
                        Log.d(TAG, "Wybrano 'Gra zdań'");
                    }
                    intent.putExtra("COURSE_ID", course.getId());
                    startActivity(intent);
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    @Override
    public void onWordsIconClicked(Course course) {
        Log.d(TAG, "Words icon clicked for course: " + course.getName() + " with ID: " + course.getId());
        if (getContext() == null) { // Sprawdzenie kontekstu
            Log.e(TAG, "Context is null in onWordsIconClicked, cannot start WordsActivity.");
            return;
        }
        Intent intent = new Intent(requireContext(), WordsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }
}