// Ścieżka: app/java/com/example/a404/ui/adapters/AchievementsAdapter.java
package com.example.a404.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a404.R; // Upewnij się, że masz dostęp do R
import com.example.a404.data.model.Achievement; // Model definicji osiągnięcia
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set; // Do przechowywania ID odblokowanych osiągnięć

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {

    private List<Achievement> achievementDefinitions = new ArrayList<>();
    private Set<String> unlockedAchievementIdsSet; // Zestaw ID odblokowanych osiągnięć
    private Context context;

    public AchievementsAdapter(Context context, List<Achievement> definitions, Set<String> unlockedIds) {
        this.context = context;
        this.achievementDefinitions = definitions != null ? definitions : new ArrayList<>();
        this.unlockedAchievementIdsSet = unlockedIds != null ? unlockedIds : new HashSet<>();
    }

    public void updateData(List<Achievement> newDefinitions, Set<String> newUnlockedIds) {
        this.achievementDefinitions = newDefinitions != null ? newDefinitions : new ArrayList<>();
        this.unlockedAchievementIdsSet = newUnlockedIds != null ? newUnlockedIds : new HashSet<>();
        notifyDataSetChanged(); // Odśwież całą listę
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievementDefinitions.get(position);
        holder.nameTextView.setText(achievement.getName());
        holder.descriptionTextView.setText(achievement.getDescription());

        boolean isUnlocked = unlockedAchievementIdsSet.contains(achievement.getId());

        // Ustawienie ikony osiągnięcia na podstawie iconName
        // Zakładamy, że iconName to nazwa zasobu drawable (np. "ic_achievement_first_lesson")
        // Musisz mieć te ikony w folderze res/drawable
        try {
            int iconResId = 0;
            if (achievement.getIconName() != null && !achievement.getIconName().isEmpty()) {
                iconResId = context.getResources().getIdentifier(
                        achievement.getIconName(),
                        "drawable",
                        context.getPackageName()
                );
            }

            if (iconResId != 0) {
                holder.iconImageView.setImageResource(iconResId);
            } else {
                // Ustaw domyślną ikonę, jeśli nie znaleziono lub nazwa jest pusta
                holder.iconImageView.setImageResource(R.drawable.ic_default_achievement_placeholder); // STWÓRZ TEN ZASÓB!
            }
        } catch (Resources.NotFoundException e) {
            // W razie błędu również ustaw domyślną ikonę
            holder.iconImageView.setImageResource(R.drawable.ic_default_achievement_placeholder); // STWóRZ TEN ZASÓB!
        }

        // Dostosowanie wyglądu w zależności od statusu odblokowania
        if (isUnlocked) {
            holder.iconImageView.clearColorFilter(); // Usuń filtr szarości
            holder.itemView.setAlpha(1.0f); // Pełna przezroczystość
            holder.statusIndicatorImageView.setImageResource(R.drawable.ic_unlocked_indicator); // STWÓRZ TEN ZASÓB! (np. галочка)
        } else {
            // Ustaw filtr szarości dla ikony głównej
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // 0 to skala szarości, 1 to pełny kolor
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.iconImageView.setColorFilter(filter);
            holder.itemView.setAlpha(0.6f); // Lekko przyciemnij cały element
            holder.statusIndicatorImageView.setImageResource(R.drawable.ic_locked_indicator); // STWÓRZ TEN ZASÓB! (np. kłódka)
        }
    }

    @Override
    public int getItemCount() {
        return achievementDefinitions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView descriptionTextView;
        ImageView statusIndicatorImageView;

        ViewHolder(View itemView) {
            super(itemView);
            // Użyj ID z Twojego layoutu item_achievement.xml
            iconImageView = itemView.findViewById(R.id.image_achievement); // <<< ZMIENIONE ID
            nameTextView = itemView.findViewById(R.id.text_achievement_name); // <<< ZMIENIONE ID
            descriptionTextView = itemView.findViewById(R.id.text_achievement_description); // <<< ZMIENIONE ID
            statusIndicatorImageView = itemView.findViewById(R.id.image_achievement_status_indicator); // <<< ZMIENIONE ID
        }
    }
}