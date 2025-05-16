package com.example.a404.ui.ranking;

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
import com.example.a404.R; // Upewnij się, że R jest poprawnie zaimportowane
import com.example.a404.databinding.FragmentRankingBinding;

public class RankingFragment extends Fragment {

    private static final String TAG = "RankingFragment";
    private RankingViewModel rankingViewModel;
    private FragmentRankingBinding binding;
    private RankingAdapter adapter;
    // Użyj tej samej stałej lub pobierz ją z ViewModel, jeśli tam ją zdefiniujesz
    private static final int RANKING_LIMIT = 50;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        binding = FragmentRankingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rankingViewModel = new ViewModelProvider(this).get(RankingViewModel.class);
        setupRecyclerView();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        // Wywołanie observeRankingData tutaj jest teraz bezpieczne,
        // bo getRankedUsers() w ViewModel zadba o inicjalizację LiveData
        observeRankingData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - triggering ranking refresh");
        // Wywołaj loadRanking, aby odświeżyć dane za każdym razem, gdy fragment staje się widoczny
        rankingViewModel.loadRanking(RANKING_LIMIT);
    }

    private void setupRecyclerView() {
        adapter = new RankingAdapter();
        binding.rankingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rankingRecyclerView.setAdapter(adapter);
    }

    private void observeRankingData() {
        // Teraz rankingViewModel.getRankedUsers() na pewno nie zwróci null
        rankingViewModel.getRankedUsers().observe(getViewLifecycleOwner(), users -> {
            if (binding == null) {
                Log.w(TAG, "Binding is null, cannot submit list.");
                return;
            }

            // Pokaż/ukryj stan ładowania (jeśli masz ProgressBar)
            // binding.progressBar.setVisibility(View.GONE);

            if (users != null) {
                Log.d(TAG, "Ranking data received, submitting list with " + users.size() + " users.");
                adapter.submitList(users);
                binding.rankingRecyclerView.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
                binding.textViewEmptyRanking.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                if (users.isEmpty()) {
                    binding.textViewEmptyRanking.setText(R.string.ranking_is_empty); // Użyj zasobu string
                }
            } else {
                Log.w(TAG, "Received null user list from ViewModel (after observation started).");
                adapter.submitList(null); // Wyczyść adapter
                binding.rankingRecyclerView.setVisibility(View.GONE);
                binding.textViewEmptyRanking.setVisibility(View.VISIBLE);
                binding.textViewEmptyRanking.setText("Nie można załadować rankingu."); // Lub inny tekst błędu
            }
        });

        // Opcjonalnie: Pokaż stan ładowania na początku obserwacji
        // binding.progressBar.setVisibility(View.VISIBLE);
        binding.rankingRecyclerView.setVisibility(View.INVISIBLE); // Ukryj listę podczas ładowania
        binding.textViewEmptyRanking.setVisibility(View.GONE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        binding = null;
    }
}