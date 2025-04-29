package com.example.a404.ui.ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.a404.databinding.FragmentRankingBinding;

public class RankingFragment extends Fragment {

    private RankingViewModel rankingViewModel;
    private FragmentRankingBinding binding;
    private RankingAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRankingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rankingViewModel = new ViewModelProvider(this).get(RankingViewModel.class);

        setupRecyclerView();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeRankingData();
    }

    private void setupRecyclerView() {
        adapter = new RankingAdapter();
        binding.rankingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rankingRecyclerView.setAdapter(adapter);
    }

    private void observeRankingData() {

        rankingViewModel.getRankedUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                adapter.submitList(users);
            } else {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}