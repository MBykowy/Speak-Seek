package com.example.a404.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Achievement;
// Zaimportuj Glide lub inną bibliotekę do ładowania obrazów, jeśli będziesz ładować ikony z URL
// import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<Achievement> achievements = new ArrayList<>();

    public AchievementAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements == null ? 0 : achievements.size();
    }

    public void setAchievements(List<Achievement> newAchievements) {
        this.achievements.clear();
        if (newAchievements != null) {
            this.achievements.addAll(newAchievements);
        }
        notifyDataSetChanged(); // Użyj DiffUtil dla lepszej wydajności w przyszłości
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageAchievement;
        private final TextView textAchievementName;
        private final TextView textAchievementDescription;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAchievement = itemView.findViewById(R.id.image_achievement);
            textAchievementName = itemView.findViewById(R.id.text_achievement_name);
            textAchievementDescription = itemView.findViewById(R.id.text_achievement_description);
        }

        public void bind(Achievement achievement) {
            textAchievementName.setText(achievement.getName());
            textAchievementDescription.setText(achievement.getDescription());

            // Ładowanie obrazu (ikony) - przykład z Glide, jeśli iconUrl jest dostępny
            // Jeśli masz domyślną ikonę w drawable, możesz ją ustawić tutaj
            // if (achievement.getIconUrl() != null && !achievement.getIconUrl().isEmpty()) {
            //     Glide.with(itemView.getContext())
            //             .load(achievement.getIconUrl())
            //             .placeholder(R.drawable.ic_achievement_default) // Domyślny obrazek ładowania
            //             .error(R.drawable.ic_achievement_default) // Domyślny obrazek błędu
            //             .into(imageAchievement);
            // } else {
            //     imageAchievement.setImageResource(R.drawable.ic_achievement_default); // Domyślna ikona
            // }
            // Na razie ustawiamy domyślną ikonę, ponieważ ładowanie z URL nie jest zaimplementowane
            imageAchievement.setImageResource(R.drawable.ic_achievement_default); // Upewnij się, że masz taki zasób
        }
    }
}