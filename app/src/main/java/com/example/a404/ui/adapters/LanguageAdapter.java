// Ścieżka: app/java/com/example/a404/ui/adapters/LanguageAdapter.java
package com.example.a404.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <<< DODAJ IMPORT
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Language;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    private static final String TAG = "LanguageAdapter";
    private final List<Language> languages;
    private final OnLanguageSelectedListener listener;
    private String selectedLanguageCode;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }

    public LanguageAdapter(List<Language> languages, OnLanguageSelectedListener listener, String selectedLanguageCode) {
        this.languages = languages;
        this.listener = listener;
        this.selectedLanguageCode = selectedLanguageCode;
        Log.d(TAG, "Adapter utworzony, wybrany język: " + selectedLanguageCode);
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language, parent, false); // Upewnij się, że item_language.xml ma ImageView
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languages.get(position);
        holder.languageName.setText(language.getName());

        // Ustaw flagę
        if (language.getFlagResourceId() != 0) {
            holder.languageFlag.setImageResource(language.getFlagResourceId());
            holder.languageFlag.setVisibility(View.VISIBLE);
        } else {
            // Opcjonalnie: Ustaw placeholder lub ukryj, jeśli nie ma flagi
            holder.languageFlag.setImageResource(R.drawable.ic_flag_placeholder); // Zakładając placeholder
            // holder.languageFlag.setVisibility(View.GONE); // Alternatywnie ukryj
        }

        // Zaznacz wybrany język
        boolean isSelected = language.getCode().equals(selectedLanguageCode);
        holder.radioButton.setChecked(isSelected);

        Log.d(TAG, "Bind pozycji " + position + " język: " + language.getCode() +
                ", zaznaczony: " + isSelected + ", wybrany kod: " + selectedLanguageCode);

        holder.itemView.setOnClickListener(v -> {
            if (!language.getCode().equals(selectedLanguageCode)) { // Aktualizuj tylko jeśli się zmienił
                selectedLanguageCode = language.getCode();
                listener.onLanguageSelected(language.getCode());
                notifyDataSetChanged(); // Odświeży wszystkie itemy, aby zaktualizować RadioButtony
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public void updateSelectedLanguage(String languageCode) {
        Log.d(TAG, "Aktualizacja wybranego języka w adapterze: " + languageCode);
        if (languageCode != null && !languageCode.equals(selectedLanguageCode)) {
            this.selectedLanguageCode = languageCode;
            notifyDataSetChanged();
        }
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView languageFlag; // <<< DODANO
        TextView languageName;
        RadioButton radioButton;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            languageFlag = itemView.findViewById(R.id.iv_language_flag); // <<< DODANO - upewnij się, że to ID jest w item_language.xml
            languageName = itemView.findViewById(R.id.tv_language_name);
            radioButton = itemView.findViewById(R.id.radio_language);
        }
    }
}