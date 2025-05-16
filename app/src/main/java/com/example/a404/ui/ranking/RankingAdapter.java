package com.example.a404.ui.ranking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.bind(currentUser, position + 1);
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        private final TextView rankNumberTextView;
        private final TextView usernameTextView;
        private final TextView pointsTextView;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            rankNumberTextView = itemView.findViewById(R.id.rankNumberTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
        }

        public void bind(UserProfile user, int rank) {
            rankNumberTextView.setText(String.format("%d.", rank));
            usernameTextView.setText(user.getUsername());
            pointsTextView.setText(String.format("%d pts", user.getPoints()));
        }
    }
}
