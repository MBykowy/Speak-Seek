package com.example.a404.ui.language;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.a404.databinding.FragmentLanguageSelectionBinding;
import com.example.a404.ui.adapters.LanguageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LanguageSelectionFragment extends Fragment implements LanguageAdapter.OnLanguageSelectedListener {

    private static final String TAG = "LanguageSelectionFrag";
    private FragmentLanguageSelectionBinding binding;
    private LanguageSelectionViewModel viewModel;
    private LanguageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate wywołane");
        viewModel = new ViewModelProvider(this).get(LanguageSelectionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView wywołane");
        binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated wywołane");

        setupRecyclerView();

        // Wczytaj język użytkownika
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Wczytywanie języka dla użytkownika: " + userId);
            viewModel.loadUserLanguage(userId);
        }

        setupObservers();
    }

    private void setupRecyclerView() {
        binding.rvLanguages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLanguages.setHasFixedSize(true);
    }

    private void setupObservers() {
        viewModel.getAvailableLanguages().observe(getViewLifecycleOwner(), languages -> {
            Log.d(TAG, "Załadowano dostępne języki: " + languages.size());
            String currentLanguage = viewModel.getCurrentSelectedLanguage().getValue();
            Log.d(TAG, "Aktualnie wybrany język: " + currentLanguage);

            adapter = new LanguageAdapter(languages, this, currentLanguage);
            binding.rvLanguages.setAdapter(adapter);
        });

        viewModel.getCurrentSelectedLanguage().observe(getViewLifecycleOwner(), languageCode -> {
            Log.d(TAG, "Zmiana aktualnie wybranego języka: " + languageCode);
            if (adapter != null) {
                adapter.updateSelectedLanguage(languageCode);
            }
        });

        viewModel.getLanguageUpdateStatus().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Język został zmieniony", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    @Override
    public void onLanguageSelected(String languageCode) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Wybrano język: " + languageCode + " dla użytkownika: " + userId);
            viewModel.selectLanguage(userId, languageCode);
        }
    }

    private void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}