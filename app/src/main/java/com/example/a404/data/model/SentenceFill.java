package com.example.a404.data.model;



import java.util.List;

public class SentenceFill {
    private String sentence;
    private List<String> options;
    private String correctAnswer;

    public SentenceFill(String sentence, List<String> options, String correctAnswer) {
        this.sentence = sentence;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getSentence() {
        return sentence;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
