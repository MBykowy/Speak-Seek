package com.example.a404.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Dodaj ten import, jeśli jeszcze go nie ma
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.a404.R;
import com.example.a404.data.dao.WordDao;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class SentenceGameActivity extends AppCompatActivity {
    private static final String TAG = "SentenceGameActivity";
    public static final String EXTRA_COURSE_ID = "COURSE_ID";
    private static final long DEFAULT_SENTENCE_COURSE_ID = 201L;
    private static final int NUM_OPTIONS = 3;

    private TextView sentenceTextView, scoreTextView;
    private Button option1Button, option2Button, option3Button;
    private Button[] optionButtons;
    private ProgressBar gameProgressBar;

    private List<Word> sentenceItems;
    private int currentSentenceIndex = 0;
    private Word currentWord;
    private String currentCorrectAnswer;

    private int currentSessionScore = 0;
    private UserRepository userRepository;
    private String currentUserId;
    private WordDbHelper dbHelper;
    private WordDao wordDao;
    private long courseId;

    private static final int POINTS_CORRECT_SENTENCE = 6;
    private static final int POINTS_WRONG_SENTENCE = -2;
    private static final int POINTS_SENTENCE_GAME_COMPLETION = 20;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_game);

        Log.d(TAG, "onCreate wywołane");

        sentenceTextView = findViewById(R.id.sentenceTextView);
        scoreTextView = findViewById(R.id.scoreTextViewSentenceGame);
        option1Button = findViewById(R.id.option1ButtonSentenceGame);
        option2Button = findViewById(R.id.option2ButtonSentenceGame);
        option3Button = findViewById(R.id.option3ButtonSentenceGame);
        gameProgressBar = findViewById(R.id.sentenceGameProgressBar);

        optionButtons = new Button[]{option1Button, option2Button, option3Button};

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, DEFAULT_SENTENCE_COURSE_ID);
        Log.d(TAG, "Otrzymano courseId: " + courseId);


        dbHelper = new WordDbHelper(this);
        wordDao = new WordDao(dbHelper);
        userRepository = new UserRepository(new FirebaseSource());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Zalogowany użytkownik: " + currentUserId);
        } else {
            Log.w(TAG, "Brak zalogowanego użytkownika.");
            Toast.makeText(this, "Błąd: Użytkownik nie jest zalogowany.", Toast.LENGTH_LONG).show();
            finish(); // Zakończ aktywność, jeśli użytkownik nie jest zalogowany
            return;
        }

        loadSentencesFromDb();

        for (Button btn : optionButtons) {
            btn.setOnClickListener(v -> {
                Button clickedButton = (Button) v;
                checkAnswer(clickedButton.getText().toString(), clickedButton);
            });
        }
        updateScoreDisplay();
    }


    private void loadSentencesFromDb() {
        Log.d(TAG, "Ładowanie zdań z bazy danych dla courseId: " + courseId);
        new Thread(() -> {
            List<Word> wordsFromDb = wordDao.getWordsByCourseId(courseId);
            // Filtruj słowa, aby uzyskać tylko te, które są zdaniami (np. mają kategorię "sentence")
            // Lub załóż, że wszystkie słowa z kursu o ID zdaniowym są zdaniami.
            // Dla tego przykładu zakładamy, że wszystkie słowa z kursu są zdaniami.
            sentenceItems = new ArrayList<>();
            if (wordsFromDb != null) {
                for (Word w : wordsFromDb) {
                    // Możesz dodać dodatkową logikę filtrowania, np. po kategorii, jeśli jest
                    // if ("sentence".equals(w.getCategory())) {
                    sentenceItems.add(w);
                    // }
                }
            }

            if (sentenceItems.isEmpty()) {
                Log.w(TAG, "Nie znaleziono zdań dla courseId: " + courseId);
                runOnUiThread(() -> {
                    Toast.makeText(SentenceGameActivity.this, "Brak zdań do wyświetlenia dla tego kursu.", Toast.LENGTH_LONG).show();
                    sentenceTextView.setText("Brak zdań.");
                    disableOptionButtons(); // Wyłącz przyciski, jeśli nie ma zdań
                });
                return; // Zakończ, jeśli nie ma zdań
            }

            Collections.shuffle(sentenceItems); // Tasuj zdania
            Log.d(TAG, "Załadowano " + sentenceItems.size() + " zdań.");

            runOnUiThread(() -> {
                currentSentenceIndex = 0;
                displayCurrentSentence();
                updateProgressBar();
            });
        }).start();
    }

    private void displayCurrentSentence() {
        if (sentenceItems == null || sentenceItems.isEmpty() || currentSentenceIndex >= sentenceItems.size()) {
            Log.d(TAG, "Brak więcej zdań do wyświetlenia lub lista jest pusta.");
            endGame();
            return;
        }

        currentWord = sentenceItems.get(currentSentenceIndex);
        // Tekst zdania to "text" a poprawna odpowiedź (tłumaczenie) to "translation"
        sentenceTextView.setText(currentWord.getText());
        currentCorrectAnswer = currentWord.getTranslation();

        Log.d(TAG, "Wyświetlanie zdania: " + currentWord.getText() + " (Poprawna odpowiedź: " + currentCorrectAnswer + ")");

        List<String> options = generateOptions(currentWord);
        for (int i = 0; i < optionButtons.length; i++) {
            if (i < options.size()) {
                optionButtons[i].setText(options.get(i));
                optionButtons[i].setVisibility(View.VISIBLE);
            } else {
                optionButtons[i].setVisibility(View.GONE); // Ukryj nieużywane przyciski
            }
        }
        resetButtonColors();
        enableOptionButtons();
    }

    private List<String> generateOptions(Word word) {
        List<String> options = new ArrayList<>();
        options.add(word.getTranslation()); // Poprawna odpowiedź

        // Użyj predefiniowanych dystraktorów, jeśli są dostępne
        List<String> predefinedDistractors = word.getPredefinedDistractorsList();
        if (predefinedDistractors != null && !predefinedDistractors.isEmpty()) {
            Collections.shuffle(predefinedDistractors);
            for (String distractor : predefinedDistractors) {
                if (options.size() < NUM_OPTIONS && !options.contains(distractor)) {
                    options.add(distractor);
                }
            }
        } else {
            // Jeśli brak predefiniowanych, spróbuj wziąć tłumaczenia innych słów z listy
            // (upewniając się, że nie są to zdania i nie są takie same jak poprawna odpowiedź)
            List<Word> otherWords = new ArrayList<>(sentenceItems);
            otherWords.remove(word); // Usuń bieżące słowo
            Collections.shuffle(otherWords);

            for (Word other : otherWords) {
                if (options.size() < NUM_OPTIONS && !other.getTranslation().equals(word.getTranslation()) && !options.contains(other.getTranslation())) {
                    options.add(other.getTranslation());
                }
            }
        }


        // Usuń duplikaty, jeśli jakieś powstały (np. predefiniowany dystraktor był taki sam jak poprawna odpowiedź)
        // i ogranicz do NUM_OPTIONS
        List<String> finalOptions = new ArrayList<>(new HashSet<>(options));


        // Jeśli po usunięciu duplikatów jest za mało opcji, dodaj placeholdery
        // To jest mało prawdopodobne, jeśli mamy wystarczająco dużo danych, ale jako zabezpieczenie
        int currentOptionNum = 1;
        while (finalOptions.size() < NUM_OPTIONS && finalOptions.size() < options.size()) { // Zabezpieczenie przed nieskończoną pętlą
            String placeholderOption = "Opcja " + currentOptionNum++;
            if (!finalOptions.contains(placeholderOption)) {
                finalOptions.add(placeholderOption);
            }
        }
        // Jeśli nadal za mało, dodaj bardziej unikalne placeholdery
        while (finalOptions.size() < NUM_OPTIONS) {
            finalOptions.add("Wybór " + (finalOptions.size() + 1));
        }


        // Ogranicz do NUM_OPTIONS i potasuj
        while (finalOptions.size() > NUM_OPTIONS) {
            finalOptions.remove(finalOptions.size() - 1); // Usuń nadmiarowe
        }

        Collections.shuffle(finalOptions);
        Log.d(TAG, "Wygenerowane opcje: " + finalOptions);
        return finalOptions.subList(0, Math.min(finalOptions.size(), NUM_OPTIONS));
    }


    private void checkAnswer(String selectedOptionText, Button clickedButton) {
        disableOptionButtons();
        boolean isCorrect = selectedOptionText.equals(currentCorrectAnswer);
        Log.d(TAG, "Sprawdzanie odpowiedzi: Wybrano '" + selectedOptionText + "', Poprawna: '" + currentCorrectAnswer + "'. Wynik: " + isCorrect);

        if (isCorrect) {
            clickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer_green));
            clickedButton.setTextColor(Color.WHITE);
            currentSessionScore += POINTS_CORRECT_SENTENCE;
            Toast.makeText(this, "Poprawnie!", Toast.LENGTH_SHORT).show();
        } else {
            clickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.incorrect_answer_red));
            clickedButton.setTextColor(Color.WHITE);
            currentSessionScore += POINTS_WRONG_SENTENCE;
            Toast.makeText(this, "Niepoprawnie. Poprawna odpowiedź: " + currentCorrectAnswer, Toast.LENGTH_SHORT).show();
            // Podświetl poprawną odpowiedź
            for (Button btn : optionButtons) {
                if (btn.getText().toString().equals(currentCorrectAnswer)) {
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer_green)); // Inny odcień dla poprawnej
                    btn.setTextColor(Color.BLACK); // Lub inny kontrastujący kolor
                }
            }
        }
        updateScoreDisplay();

        uiHandler.postDelayed(() -> {
            currentSentenceIndex++;
            if (currentSentenceIndex < sentenceItems.size()) {
                displayCurrentSentence();
                updateProgressBar();
            } else {
                endGame();
            }
        }, 2000); // Opóźnienie przed następnym zdaniem
    }

    private void updateScoreDisplay() {
        scoreTextView.setText(String.format(Locale.getDefault(),"Wynik: %d", currentSessionScore));
        Log.d(TAG, "Zaktualizowano wyświetlanie wyniku: " + currentSessionScore);
    }

    private void disableOptionButtons() {
        for (Button btn : optionButtons) {
            btn.setEnabled(false);
        }
        Log.d(TAG, "Przyciski opcji wyłączone.");
    }

    private void enableOptionButtons() {
        for (Button btn : optionButtons) {
            btn.setEnabled(true);
        }
        Log.d(TAG, "Przyciski opcji włączone.");
    }

    private void resetButtonColors() {
        for (Button btn : optionButtons) {
            // Reset to default button style or a specific drawable
            // btn.setBackgroundResource(android.R.drawable.btn_default); // Domyślny styl przycisku
            // Lub, jeśli masz własny styl:
            btn.setBackgroundColor(Color.LTGRAY); // Przykładowy domyślny kolor
            btn.setTextColor(Color.BLACK);
        }
        Log.d(TAG, "Zresetowano kolory przycisków.");
    }

    private void updateProgressBar() {
        if (sentenceItems != null && !sentenceItems.isEmpty()) {
            int progress = (int) (((double) currentSentenceIndex / sentenceItems.size()) * 100);
            gameProgressBar.setProgress(progress);
            Log.d(TAG, "Aktualizacja paska postępu: " + progress + "%");
        }
    }


    private void endGame() {
        Log.d(TAG, "Gra zakończona. Wynik sesji: " + currentSessionScore);
        Toast.makeText(this, "Koniec gry! Końcowy wynik: " + currentSessionScore, Toast.LENGTH_LONG).show();
        updateUserScoreInFirebase(currentSessionScore + POINTS_SENTENCE_GAME_COMPLETION);
        disableOptionButtons();
        gameProgressBar.setProgress(100);

        // Opcjonalnie: Wróć do poprzedniego ekranu po krótkim opóźnieniu
        // uiHandler.postDelayed(this::finish, 3000); // Możesz to odkomentować
    }

    private void updateUserScoreInFirebase(int pointsToAdd) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid(); // Upewnij się, że currentUserId jest aktualne
        } else {
            Log.w(TAG, "Nie można zaktualizować punktów: brak zalogowanego użytkownika.");
            if (getApplicationContext() != null) {
                Toast.makeText(SentenceGameActivity.this, "Błąd: Użytkownik nie jest zalogowany.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (currentUserId != null && !currentUserId.isEmpty() && userRepository != null) {
            Log.d(TAG, "Aktualizowanie punktów użytkownika w Firebase: " + pointsToAdd + " dla użytkownika: " + currentUserId);
            userRepository.updatePoints(currentUserId, pointsToAdd, new UserRepository.UserOperationCallback() {
                @Override
                public void onComplete(boolean success, Exception e) {
                    if (success) {
                        Log.d(TAG, "Punkty użytkownika (" + currentUserId + ") zaktualizowane pomyślnie.");
                        if (getApplicationContext() != null) {
                            Toast.makeText(SentenceGameActivity.this, "Wynik zaktualizowany!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Błąd podczas aktualizacji punktów użytkownika (" + currentUserId + ").", e);
                        if (getApplicationContext() != null) {
                            String errorMessage = "Błąd aktualizacji wyniku";
                            if (e != null && e.getMessage() != null) {
                                errorMessage += ": " + e.getMessage();
                            }
                            Toast.makeText(SentenceGameActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Log.w(TAG, "Nie można zaktualizować punktów: currentUserId jest null/pusty lub userRepository jest null.");
            if (getApplicationContext() != null) {
                if (currentUserId == null || currentUserId.isEmpty()) {
                    Toast.makeText(SentenceGameActivity.this, "Błąd: ID użytkownika nieznane.", Toast.LENGTH_SHORT).show();
                } else { // userRepository musi być null
                    Toast.makeText(SentenceGameActivity.this, "Błąd: Repozytorium użytkownika niedostępne.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane dla SentenceGameActivity");
        if (wordDao != null) {
            // wordDao.close(); // Rozważ, czy DAO powinno być zamykane tutaj, czy dbHelper
        }
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "WordDbHelper zamknięty.");
        }
        uiHandler.removeCallbacksAndMessages(null); // Usuń wszystkie oczekujące zadania handlera
        Log.d(TAG, "Handler UI wyczyszczony.");
    }
}