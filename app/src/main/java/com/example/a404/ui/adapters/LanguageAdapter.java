package com.example.a404.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Language;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languages;
    private OnLanguageSelectedListener listener;
    private String selectedLanguageCode;

    public LanguageAdapter(List<Language> languages, OnLanguageSelectedListener listener, String selectedLanguageCode) {
        this.languages = languages;
        this.listener = listener;
        this.selectedLanguageCode = selectedLanguageCode;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languages.get(position);
        holder.tvLanguageName.setText(language.getName());
        holder.rbSelected.setChecked(language.getCode().equals(selectedLanguageCode));

        holder.itemView.setOnClickListener(v -> {
            String previousSelected = selectedLanguageCode;
            selectedLanguageCode = language.getCode();

            if (!selectedLanguageCode.equals(previousSelected)) {
                listener.onLanguageSelected(language.getCode());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public void updateSelectedLanguage(String languageCode) {
        this.selectedLanguageCode = languageCode;
        notifyDataSetChanged();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView tvLanguageName;
        RadioButton rbSelected;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
            rbSelected = itemView.findViewById(R.id.rbLanguageSelected);
        }
    }

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }
}