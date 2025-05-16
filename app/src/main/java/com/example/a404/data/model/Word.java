package com.example.a404.data.model;

public class Word {
    private long id;
    private String text;
    private String translation;
    private long courseId;

    public Word() {
    }

    public Word(long id, String text, String translation, long courseId) {
        this.id = id;
        this.text = text;
        this.translation = translation;
        this.courseId = courseId;
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
}