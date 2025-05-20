package com.example.a404.ui.ranking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Dodaj import
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a404.R;
import com.example.a404.data.model.UserProfile;

public class RankingAdapter extends ListAdapter<UserProfile, RankingAdapter.RankingViewHolder> {

    public RankingAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<UserProfile> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserProfile>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
            // Możesz chcieć dodać porównanie rankingu, jeśli UserProfile zawierałby pole rank
            // na potrzeby DiffUtil, ale dla wyświetlania medali nie jest to krytyczne w tym miejscu.
            return oldItem.getUsername().equals(newItem.getUsername()) && oldItem.getPoints() == newItem.getPoints();
        }
    };

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        UserProfile currentUser = getItem(position);
        // Przekazujemy pozycję + 1 jako rangę. Zakładamy, że lista jest już posortowana.
        holder.bind(currentUser, position + 1);
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView medalImageView; // DODANO
        private final TextView rankNumberTextView;
        private final TextView usernameTextView;
        private final TextView pointsTextView;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            medalImageView = itemView.findViewById(R.id.medalImageView); // DODANO
            rankNumberTextView = itemView.findViewById(R.id.rankNumberTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
        }

        public void bind(UserProfile user, int rank) {
            usernameTextView.setText(user.getUsername());
            pointsTextView.setText(String.format("%d pts", user.getPoints()));

            // Logika wyświetlania medali
            if (rank == 1) {
                medalImageView.setVisibility(View.VISIBLE);
                medalImageView.setImageResource(R.drawable.ic_medal_gold);
                rankNumberTextView.setVisibility(View.GONE);
            } else if (rank == 2) {
                medalImageView.setVisibility(View.VISIBLE);
                medalImageView.setImageResource(R.drawable.ic_medal_silver);
                rankNumberTextView.setVisibility(View.GONE);
            } else if (rank == 3) {
                medalImageView.setVisibility(View.VISIBLE);
                medalImageView.setImageResource(R.drawable.ic_medal_bronze);
                rankNumberTextView.setVisibility(View.GONE);
            } else {
                medalImageView.setVisibility(View.GONE);
                rankNumberTextView.setVisibility(View.VISIBLE);
                rankNumberTextView.setText(String.format("%d.", rank));
            }
        }
    }
}