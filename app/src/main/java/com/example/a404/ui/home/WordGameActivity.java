package com.example.a404.ui.home;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WordGameActivity extends AppCompatActivity {

    private List<Word> words;
    private int currentIndex = 0;
    private TextView wordTextView;
    private Button[] optionButtons = new Button[4];
    private ProgressBar progressBar;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.a404.R.layout.activity_word_game);

        wordTextView = findViewById(R.id.wordTextView);
        progressBar = findViewById(R.id.progressBar);

        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);

        dbHelper = new WordDbHelper(this);
        courseDao = new CourseDao(dbHelper);

        long courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId != -1) {
            words = getWordsForCourse(courseId);
            showNextWord();
        }

    }

    private void showNextWord() {
        if (currentIndex >= words.size()) {
            Toast.makeText(this, "Course Completed!", Toast.LENGTH_LONG).show();
            finish(); // Or navigate to a summary screen
            return;
        }

        Word currentWord = words.get(currentIndex);
        wordTextView.setText("What is: " + currentWord.getText());

        List<String> options = getRandomOptions(currentWord);
        for (int i = 0; i < 4; i++) {
            final String selected = options.get(i);
            optionButtons[i].setText(selected);
            optionButtons[i].setOnClickListener(v -> handleAnswer(selected.equals(currentWord.getTranslation())));
        }

        int progress = (int) (((float) currentIndex / words.size()) * 100);
        progressBar.setProgress(progress);
    }

    private void handleAnswer(boolean isCorrect) {
        if (isCorrect) {
            currentIndex++;
            showNextWord();
        } else {
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getRandomOptions(Word correctWord) {
        List<String> options = new ArrayList<>();
        options.add(correctWord.getTranslation());

        // Add 3 incorrect options
        Random rand = new Random();
        while (options.size() < 4) {
            Word randomWord = words.get(rand.nextInt(words.size()));
            if (!options.contains(randomWord.getTranslation())) {
                options.add(randomWord.getTranslation());
            }
        }

        Collections.shuffle(options);
        return options;
    }


    private void loadWords(long courseId) {

    }
    private List<Word> getWordsForCourse(long courseId) {
        Course course = courseDao.getCourseById(courseId);
        if (course != null) {
            return course.getWords();
        }
        return null;
    }
}
