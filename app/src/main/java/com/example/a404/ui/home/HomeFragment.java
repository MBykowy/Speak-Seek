package com.example.a404.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation; // Upewnij się, że ten import jest obecny
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.ui.adapters.CourseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeFragment extends Fragment {

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
        dbHelper = new WordDbHelper(requireContext());
        courseDao = new CourseDao(dbHelper);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated wywołane");

        setupRecyclerView();
        setupNavigation(); // Ustaw nawigację

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Użytkownik zalogowany, ID: " + userId);
            homeViewModel.loadUserProfile(userId); // Załaduj profil użytkownika
        } else {
            Log.w(TAG, "Użytkownik nie jest zalogowany");
            // Możesz tutaj dodać logikę przekierowania do ekranu logowania
        }

        // Obserwuj zmiany w wybranym języku
        homeViewModel.getSelectedLanguageCode().observe(getViewLifecycleOwner(), languageCode -> {
            if (languageCode != null) {
                Log.d(TAG, "Obserwator: Wybrany język zmieniony na: " + languageCode);
                loadCourses(languageCode); // Załaduj kursy dla nowego języka
            } else {
                Log.d(TAG, "Obserwator: Kod języka jest null");
                // Obsłuż przypadek, gdy język nie jest jeszcze ustawiony (np. pokaż komunikat)
                binding.textViewNoCourses.setVisibility(View.VISIBLE);
                binding.textViewNoCourses.setText(R.string.language_not_selected); // Dodaj odpowiedni string
                binding.recyclerViewCourses.setVisibility(View.GONE);
            }
        });
    }


    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView wywołane");
        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCourses.setHasFixedSize(true); // Poprawia wydajność, jeśli rozmiar elementów jest stały
    }

    private void setupNavigation() {
        Log.d(TAG, "setupNavigation wywołane");
    }


    // Zmodyfikowana metoda przyjmująca kod języka
    private void loadCourses(String languageCode) {
        Log.d(TAG, "loadCourses wywołane dla języka: " + languageCode);
        if (courseDao == null) {
            Log.e(TAG, "courseDao nie został zainicjalizowany!");
            return;
        }

        List<Course> courses = courseDao.getAllCourses(languageCode);
        Log.d(TAG, "Pobrano " + courses.size() + " kursów dla języka: " + languageCode);

        if (courses.isEmpty()) {
            Log.d(TAG, "Brak kursów dla języka: " + languageCode + ". Wyświetlanie komunikatu.");
            binding.textViewNoCourses.setVisibility(View.VISIBLE); // Użyj binding
            // Użyj getString z formatowaniem, jeśli Twój string tego wymaga
            binding.textViewNoCourses.setText(getString(R.string.no_courses_available, languageCode)); // Użyj R.string
            binding.recyclerViewCourses.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Znaleziono kursy. Ukrywanie komunikatu i ustawianie adaptera.");
            binding.textViewNoCourses.setVisibility(View.GONE); // Użyj binding
            binding.recyclerViewCourses.setVisibility(View.VISIBLE);
            // TODO: Zamiast tworzyć nowy adapter za każdym razem, rozważ implementację metody
            //       updateCourses(List<Course> newCourses) w CourseAdapter i wywołanie notifyDataSetChanged()
            //       dla lepszej wydajności.
            courseAdapter = new CourseAdapter(requireContext(), courses);
            binding.recyclerViewCourses.setAdapter(courseAdapter);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView wywołane");
        // Nie zamykaj dbHelper tutaj, jego cyklem życia zarządza system.
        // Zamykanie bazy danych w onDestroyView fragmentu może prowadzić do błędów,
        // jeśli inne komponenty (np. inny fragment, aktywność) nadal jej używają.
        // dbHelper.close(); // Usunięto
        binding = null; // Ważne, aby zapobiec wyciekom pamięci związanych z widokiem
    }
}