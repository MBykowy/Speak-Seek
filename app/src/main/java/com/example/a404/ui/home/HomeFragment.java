package com.example.a404.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.a404.R;
import com.example.a404.databinding.FragmentHomeBinding;
import com.example.a404.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
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