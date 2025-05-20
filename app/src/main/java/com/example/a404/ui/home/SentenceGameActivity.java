package com.example.a404.ui.home;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a404.R;

public class SentenceGameActivity extends AppCompatActivity {
    private TextView sentenceTextView;
    private Button option1, option2, option3;

    private String[] sampleSentences = {
            "I ___ to school every day.",
            "She ___ playing the piano.",
            "We ___ a movie yesterday.",
            "He ___ his homework before dinner.",
            "They ___ to the park on Sundays.",
            "The cat ___ on the sofa.",
            "My friends ___ going to the party.",
            "It ___ raining all day.",
            "You ___ very kind.",
            "We ___ already finished our work."
    };

    private String[][] options = {
            {"go", "goes", "went"},                   // correct: 0
            {"is", "are", "am"},                      // correct: 0
            {"watched", "watch", "watches"},          // correct: 0
            {"did", "do", "does"},                    // correct: 0
            {"go", "goes", "went"},                   // correct: 0
            {"sits", "sit", "sat"},                   // correct: 2
            {"is", "are", "am"},                      // correct: 1
            {"was", "is", "were"},                    // correct: 0
            {"is", "are", "am"},                      // correct: 1
            {"has", "have", "had"}                    // correct: 1
    };

    private int[] correctAnswers = {
            0, // go
            0, // is
            0, // watched
            0, // did
            0, // go
            2, // sat
            1, // are
            0, // was
            1, // are
            1  // have
    };

    private int currentSentenceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_game);

        sentenceTextView = findViewById(R.id.sentenceTextView);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);

        loadSentence();

        option1.setOnClickListener(v -> checkAnswer(0));
        option2.setOnClickListener(v -> checkAnswer(1));
        option3.setOnClickListener(v -> checkAnswer(2));
    }

    private void loadSentence() {
        sentenceTextView.setText(sampleSentences[currentSentenceIndex]);
        option1.setText(options[currentSentenceIndex][0]);
        option2.setText(options[currentSentenceIndex][1]);
        option3.setText(options[currentSentenceIndex][2]);
    }

    private void checkAnswer(int selectedIndex) {
        if (selectedIndex == correctAnswers[currentSentenceIndex]) {
            Toast.makeText(this, "Prawidłowy!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Niepoprawne", Toast.LENGTH_SHORT).show();
        }

        currentSentenceIndex++;
        if (currentSentenceIndex < sampleSentences.length) {
            loadSentence();
        } else {
            Toast.makeText(this, "Koniec gry!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
