package com.example.a404.ui.language;

import android.os.Bundle;
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
import com.example.a404.service.TextToSpeechService;
import com.example.a404.ui.adapters.LanguageAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class LanguageSelectionFragment extends Fragment implements LanguageAdapter.OnLanguageSelectedListener {

    private FragmentLanguageSelectionBinding binding;
    private LanguageSelectionViewModel viewModel;
    private LanguageAdapter adapter;
    private TextToSpeechService textToSpeechService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicjalizuj ViewModel już w onCreate
        viewModel = new ViewModelProvider(this).get(LanguageSelectionViewModel.class);
        textToSpeechService = new TextToSpeechService(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupObservers();

        // Ustaw obserwator zmian języka dla TextToSpeech
        viewModel.getCurrentSelectedLanguage().observe(getViewLifecycleOwner(), languageCode -> {
            textToSpeechService.setLanguage(languageCode);
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.loadUserLanguage(userId);
    }

    private void setupRecyclerView() {
        binding.rvLanguages.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupObservers() {
        viewModel.getAvailableLanguages().observe(getViewLifecycleOwner(), languages -> {
            adapter = new LanguageAdapter(
                    languages,
                    this,
                    viewModel.getCurrentSelectedLanguage().getValue()
            );
            binding.rvLanguages.setAdapter(adapter);
        });

        viewModel.getCurrentSelectedLanguage().observe(getViewLifecycleOwner(), languageCode -> {
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.selectLanguage(userId, languageCode);
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