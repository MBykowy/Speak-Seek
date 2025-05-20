package com.example.a404.data.model;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Word {
    private long id;
    private String text;
    private String translation;
    private long courseId;
    private String category; // Nowe pole
    private String predefinedDistractors; // Nowe pole (przechowywane jako String np. "d1,d2,d3")

    public Word() {
    }

    public Word(long id, String text, String translation, long courseId, String category, String predefinedDistractors) {
        this.id = id;
        this.text = text;
        this.translation = translation;
        this.courseId = courseId;
        this.category = category;
        this.predefinedDistractors = predefinedDistractors;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPredefinedDistractorsString() {
        return predefinedDistractors;
    }

    public void setPredefinedDistractorsString(String predefinedDistractors) {
        this.predefinedDistractors = predefinedDistractors;
    }

    // Metoda pomocnicza do pobierania dystraktorów jako listy
    public List<String> getPredefinedDistractorsList() {
        if (predefinedDistractors == null || predefinedDistractors.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(predefinedDistractors.split("\\s*,\\s*")); // Dzieli po przecinku, ignorując spacje
    }

    // Metoda pomocnicza do ustawiania dystraktorów z listy
    public void setPredefinedDistractorsList(List<String> distractors) {
        if (distractors == null || distractors.isEmpty()) {
            this.predefinedDistractors = null;
        } else {
            this.predefinedDistractors = String.join(",", distractors);
        }
    }
}