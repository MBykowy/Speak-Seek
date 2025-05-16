// Ścieżka: app/java/com/example/a404/ui/achievements/AchievementsFragment.java
package com.example.a404.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a404.R; // Upewnij się, że masz dostęp do R
import com.example.a404.ui.adapters.AchievementsAdapter;
import java.util.ArrayList;
import java.util.HashSet;

public class AchievementsFragment extends Fragment {

    private AchievementsViewModel achievementsViewModel;
    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter achievementsAdapter;
    private ProgressBar progressBar;
    private TextView messageTextView; // Do wyświetlania błędów lub informacji o braku danych

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_achievements, container, false);
        achievementsRecyclerView = root.findViewById(R.id.achievements_recycler_view);
        progressBar = root.findViewById(R.id.achievements_progress_bar);
        messageTextView = root.findViewById(R.id.achievements_message_text_view);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        achievementsViewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        // Inicjalizuj adapter z pustymi danymi na początku
        achievementsAdapter = new AchievementsAdapter(getContext(), new ArrayList<>(), new HashSet<>());
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        achievementsRecyclerView.setAdapter(achievementsAdapter);

        observeViewModel();

        // Rozpocznij ładowanie danych, gdy widok jest gotowy
        achievementsViewModel.loadAchievementsData();
    }

    private void observeViewModel() {
        achievementsViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                achievementsRecyclerView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.GONE);
            }
        });

        achievementsViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                messageTextView.setText(errorMessage);
                messageTextView.setVisibility(View.VISIBLE);
                achievementsRecyclerView.setVisibility(View.GONE);
                // Opcjonalnie: Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            } else {
                // Jeśli nie ma błędu, ale lista może być pusta, obsłuż to poniżej
                messageTextView.setVisibility(View.GONE);
            }
        });

        // Obserwuj obie listy (definicje i odblokowane) i aktualizuj adapter
        achievementsViewModel.allAchievementDefinitions.observe(getViewLifecycleOwner(), definitions -> {
            // Aktualizuj tylko, jeśli mamy już też dane o odblokowanych
            if (achievementsViewModel.unlockedAchievementIds.getValue() != null) {
                achievementsAdapter.updateData(definitions, achievementsViewModel.unlockedAchievementIds.getValue());
                checkEmptyState();
            }
        });

        achievementsViewModel.unlockedAchievementIds.observe(getViewLifecycleOwner(), unlockedIds -> {
            // Aktualizuj tylko, jeśli mamy już też definicje
            if (achievementsViewModel.allAchievementDefinitions.getValue() != null) {
                achievementsAdapter.updateData(achievementsViewModel.allAchievementDefinitions.getValue(), unlockedIds);
                checkEmptyState();
            }
        });
    }

    private void checkEmptyState() {
        boolean isLoading = achievementsViewModel.isLoading.getValue() != null && achievementsViewModel.isLoading.getValue();
        boolean hasError = achievementsViewModel.errorMessage.getValue() != null && !achievementsViewModel.errorMessage.getValue().isEmpty();

        if (!isLoading && !hasError) {
            if (achievementsAdapter.getItemCount() == 0) {
                messageTextView.setText("Brak dostępnych osiągnięć.");
                messageTextView.setVisibility(View.VISIBLE);
                achievementsRecyclerView.setVisibility(View.GONE);
            } else {
                messageTextView.setVisibility(View.GONE);
                achievementsRecyclerView.setVisibility(View.VISIBLE);
            }
        } else if (hasError) { // Jeśli jest błąd, messageTextView jest już ustawiony przez observer błędu
            achievementsRecyclerView.setVisibility(View.GONE);
        }
    }
}