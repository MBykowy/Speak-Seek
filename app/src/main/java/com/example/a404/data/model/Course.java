package com.example.a404.data.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private long id;
    private String name;
    private String description;
    private String languageCode; // Dodaj pole dla kodu języka
    private List<Word> words;

    public Course() {
        words = new ArrayList<>();
    }

    // Zaktualizuj konstruktor, jeśli chcesz ustawiać kod języka przy tworzeniu
    public Course(long id, String name, String description, String languageCode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.languageCode = languageCode; // Ustaw kod języka
        this.words = new ArrayList<>();
    }

    // Gettery i Settery dla istniejących pól
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Word> getWords() { return words; }
    public void setWords(List<Word> words) { this.words = words; }

    public void addWord(Word word) { this.words.add(word); }

    // Dodaj getter i setter dla languageCode
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
}