// Ścieżka: app/java/com/example/a404/ui/home/HomeFragment.java
package com.example.a404.ui.home;

import android.content.Intent; // <<< DODAJ IMPORT
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.TextView; // Już niepotrzebne bezpośrednio

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
// import androidx.navigation.NavController; // Już niepotrzebne bezpośrednio
// import androidx.navigation.Navigation; // Już niepotrzebne bezpośrednio
import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView; // Już niepotrzebne bezpośrednio

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.ui.adapters.CourseAdapter; // Upewnij się, że to poprawny import
import com.example.a404.ui.words.WordsActivity; // <<< DODAJ IMPORT
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList; // Dodaj, jeśli tworzysz pustą listę
import java.util.List;

public class HomeFragment extends Fragment implements CourseAdapter.OnCourseClickListener { // <<< IMPLEMENTUJ INTERFEJS

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CourseAdapter courseAdapter;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView wywołane");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        dbHelper = new WordDbHelper(requireContext()); // Inicjalizacja dbHelper
        courseDao = new CourseDao(dbHelper); // Inicjalizacja courseDao
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated wywołane");

        setupRecyclerView();
        // setupNavigation(); // Możesz usunąć, jeśli nie jest używane

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Użytkownik zalogowany, ID: " + userId);
            homeViewModel.loadUserProfile(userId);
        } else {
            Log.w(TAG, "Użytkownik nie jest zalogowany");
            // Obsłuż przekierowanie do logowania, jeśli to konieczne
        }

        homeViewModel.getSelectedLanguageCode().observe(getViewLifecycleOwner(), languageCode -> {
            if (languageCode != null && !languageCode.isEmpty()) {
                Log.d(TAG, "Obserwator: Wybrany język zmieniony na: " + languageCode);
                loadCoursesAndUpdateAdapter(languageCode); // Zmieniono nazwę metody
            } else {
                Log.d(TAG, "Obserwator: Kod języka jest null lub pusty");
                binding.textViewNoCourses.setVisibility(View.VISIBLE);
                binding.textViewNoCourses.setText(R.string.language_not_selected);
                binding.recyclerViewCourses.setVisibility(View.GONE);
                if (courseAdapter != null) {
                    courseAdapter.updateCourses(new ArrayList<>()); // Wyczyść adapter
                }
            }
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView wywołane");
        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCourses.setHasFixedSize(true);
        // Inicjalizuj adapter z pustą listą i 'this' jako listenerem
        courseAdapter = new CourseAdapter(new ArrayList<>(), this); // <<< POPRAWIONA INICJALIZACJA
        binding.recyclerViewCourses.setAdapter(courseAdapter);
    }

    // Połączono loadCourses z aktualizacją adaptera
    private void loadCoursesAndUpdateAdapter(String languageCode) {
        Log.d(TAG, "loadCoursesAndUpdateAdapter wywołane dla języka: " + languageCode);
        if (courseDao == null) {
            Log.e(TAG, "courseDao nie został zainicjalizowany!");
            binding.textViewNoCourses.setVisibility(View.VISIBLE);
            binding.textViewNoCourses.setText("Błąd ładowania kursów."); // Lub inny komunikat
            binding.recyclerViewCourses.setVisibility(View.GONE);
            return;
        }

        List<Course> courses = courseDao.getAllCourses(languageCode);
        Log.d(TAG, "Pobrano " + courses.size() + " kursów dla języka: " + languageCode);

        if (courseAdapter != null) {
            courseAdapter.updateCourses(courses); // Użyj metody updateCourses adaptera
        }

        if (courses.isEmpty()) {
            Log.d(TAG, "Brak kursów dla języka: " + languageCode + ". Wyświetlanie komunikatu.");
            binding.textViewNoCourses.setVisibility(View.VISIBLE);
            binding.textViewNoCourses.setText(getString(R.string.no_courses_available, languageCode));
            binding.recyclerViewCourses.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Znaleziono kursy. Ukrywanie komunikatu.");
            binding.textViewNoCourses.setVisibility(View.GONE);
            binding.recyclerViewCourses.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView wywołane");
        binding = null;
    }

    // === IMPLEMENTACJA METOD Z CourseAdapter.OnCourseClickListener ===
    @Override
    public void onCourseClicked(Course course) {
        Log.d(TAG, "Course card clicked: " + course.getName() + " with ID: " + course.getId());
        Intent intent = new Intent(requireContext(), WordGameActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        // Możesz dodać więcej danych, jeśli WordGameActivity ich potrzebuje
        // intent.putExtra("COURSE_NAME", course.getName());
        startActivity(intent);
    }

    @Override
    public void onWordsIconClicked(Course course) {
        Log.d(TAG, "Words icon clicked for course: " + course.getName() + " with ID: " + course.getId());
        Intent intent = new Intent(requireContext(), WordsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        // intent.putExtra("COURSE_NAME", course.getName());
        startActivity(intent);
    }
}