package com.example.a404.data.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private long id;
    private String name;
    private String description;
    private List<Word> words;

    public Course() {
        words = new ArrayList<>();
    }

    public Course(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.words = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public void addWord(Word word) {
        this.words.add(word);
    }
}