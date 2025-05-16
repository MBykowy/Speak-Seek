package com.example.a404.data.model;

public class VocabularyItem {
    private String objectLabel;
    private String languageCode;
    private String translation;

    // Konstruktor domyślny wymagany przez Firebase
    public VocabularyItem() {}

    public VocabularyItem(String objectLabel, String languageCode, String translation) {
        this.objectLabel = objectLabel;
        this.languageCode = languageCode;
        this.translation = translation;
    }

    // Gettery i settery
    public String getObjectLabel() { return objectLabel; }
    public void setObjectLabel(String objectLabel) { this.objectLabel = objectLabel; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
}