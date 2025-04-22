package com.example.a404.ui.home;

import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.MainActivity;
import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.ui.adapters.CourseAdapter;
import com.example.a404.ui.adapters.LanguageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    private WordDbHelper dbHelper;
    private CourseDao courseDao;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadCourses();

        return root;
    }

    private void loadCourses() {

        dbHelper = new WordDbHelper(getContext());
        courseDao = new CourseDao(dbHelper);
        List<Course> courses = courseDao.getAllCourses();
        CourseAdapter adapter = new CourseAdapter(this.getContext(), courses);
        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCourses.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sprawdź, czy użytkownik ma wybrany język
        checkLanguageSelection();
    }

    private void checkLanguageSelection() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            homeViewModel.getUserProfile(userId).observe(getViewLifecycleOwner(), userProfile -> {
                if (userProfile != null) {
                    String selectedLanguage = userProfile.getSelectedLanguageCode();
                    if (selectedLanguage == null || selectedLanguage.isEmpty()) {
                        // Przekieruj do ekranu wyboru języka
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_navigation_home_to_languageSelectionFragment);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}