package com.example.a404.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class WordGameActivity extends AppCompatActivity {

    private static final String TAG = "WordGameActivity";
    private List<Word> words = new ArrayList<>();
    private int currentIndex = 0;
    private int currentScore = 0;
    private TextView wordTextView;
    private TextView scoreTextView;
    private Button[] optionButtons = new Button[4];
    private ProgressBar loadingProgressBar;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;
    private UserRepository userRepository;
    private String currentUserId;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static final int POINTS_CORRECT_ANSWER = 15;
    private static final int POINTS_WRONG_ANSWER = -5;
    private static final int POINTS_COURSE_COMPLETION = 10;
    private static final int NUMBER_OF_OPTIONS = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        wordTextView = findViewById(R.id.wordTextView);
        scoreTextView = findViewById(R.id.scoreTextView);

        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);

        dbHelper = new WordDbHelper(this);
        courseDao = new CourseDao(dbHelper); // CourseDao również potrzebuje dbHelper

        FirebaseSource firebaseSource = new FirebaseSource();
        userRepository = new UserRepository(firebaseSource);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            fetchInitialUserScore();
        } else {
            Toast.makeText(this, "Błąd: Użytkownik niezalogowany.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updateScoreDisplay();

        long courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId != -1) {
            Log.d(TAG, "Otrzymano COURSE_ID: " + courseId);
            if (wordTextView != null) {
                wordTextView.setText(""); // Wyczyść tekst na początku
            }
            for (Button btn : optionButtons) {
                if (btn != null) {
                    btn.setVisibility(View.INVISIBLE); // Ukryj przyciski na początku
                }
            }
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.VISIBLE); // Pokaż pasek ładowania
            }
            loadWordsForCourseAsync(courseId);
        } else {
            Log.e(TAG, "Nieprawidłowy COURSE_ID (-1)");
            Toast.makeText(this, "Błąd: Nieprawidłowy ID kursu.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateScoreDisplay() {
        if (scoreTextView != null) {
            scoreTextView.setText("Punkty: " + currentScore);
        }
    }

    private void fetchInitialUserScore() {
        if (currentUserId == null || userRepository == null) return;

        LiveData<UserProfile> userProfileLiveData = userRepository.getUserProfile(currentUserId);
        userProfileLiveData.observe(this, new Observer<UserProfile>() {
            @Override
            public void onChanged(UserProfile userProfile) {
                if (userProfile != null) {
                    // Możesz chcieć zaktualizować currentScore o punkty użytkownika,
                    // jeśli gra ma kontynuować poprzedni wynik,
                    // ale dla nowej sesji gry zwykle zaczyna się od 0.
                    // currentScore = userProfile.getPoints();
                    // updateScoreDisplay();
                    Log.d(TAG, "Początkowe punkty użytkownika (nieużywane w sesji gry): " + userProfile.getPoints());
                }
                userProfileLiveData.removeObserver(this); // Usuń obserwatora po pierwszym odczycie
            }
        });
    }

    private void loadWordsForCourseAsync(long courseId) {
        Log.d(TAG, "Rozpoczynanie asynchronicznego ładowania słów dla kursu: " + courseId);
        if (loadingProgressBar != null) {
            loadingProgressBar.setIndeterminate(true);
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        executorService.execute(() -> {
            final List<Word> loadedWords = getWordsForCourseFromDb(courseId);
            Log.d(TAG, "Wątek tła: Załadowano " + (loadedWords != null ? loadedWords.size() : 0) + " słów.");
            mainThreadHandler.post(() -> {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setIndeterminate(false);
                    loadingProgressBar.setVisibility(View.GONE); // Ukryj po załadowaniu
                }
                if (loadedWords != null && !loadedWords.isEmpty()) {
                    words = loadedWords;
                    Collections.shuffle(words); // Pomieszaj słowa na początku kursu
                    currentIndex = 0;
                    currentScore = 0; // Resetuj wynik dla nowej gry
                    updateScoreDisplay();
                    showNextWord();
                    for (Button btn : optionButtons) {
                        if (btn != null) {
                            btn.setVisibility(View.VISIBLE); // Pokaż przyciski po załadowaniu słów
                        }
                    }
                } else {
                    Log.e(TAG, "Nie załadowano słów lub lista jest pusta.");
                    Toast.makeText(WordGameActivity.this, "Nie udało się załadować słów dla tego kursu.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private List<Word> getWordsForCourseFromDb(long courseId) {
        Log.d(TAG, "Pobieranie słów z DB dla kursu: " + courseId);
        // CourseDao jest używany do pobierania informacji o kursie,
        // ale słowa są pobierane przez WordDao.
        // Zakładając, że CourseDao.getCourseById(courseId) zwraca obiekt Course,
        // który zawiera listę słów (być może pobranych przez WordDao wewnątrz CourseDao).
        // Jeśli CourseDao nie ładuje słów, musisz użyć WordDao bezpośrednio.

        // Poprawka: Użyj WordDao do pobrania słów bezpośrednio, jeśli CourseDao tego nie robi.
        // Dla uproszczenia, zakładam, że CourseDao.getCourseById() zwraca kurs ZE słowami.
        // Jeśli nie, musisz to zmienić:
        // WordDao wordDao = new WordDao(dbHelper);
        // List<Word> courseWords = wordDao.getWordsByCourseId(courseId);
        // wordDao.close(); // Pamiętaj o zamknięciu DAO
        // return courseWords;

        Course course = courseDao.getCourseById(courseId); // Ta metoda powinna ładować słowa do obiektu Course
        if (course != null && course.getWords() != null && !course.getWords().isEmpty()) {
            Log.d(TAG, "Znaleziono kurs i słowa. Liczba słów: " + course.getWords().size());
            return course.getWords();
        }
        Log.w(TAG, "Nie znaleziono kursu lub kurs nie ma słów dla ID: " + courseId + ". Próba pobrania przez WordDao.");
        // Fallback, jeśli CourseDao nie załadował słów
        com.example.a404.data.dao.WordDao wordDao = new com.example.a404.data.dao.WordDao(dbHelper);
        List<Word> courseWords = wordDao.getWordsByCourseId(courseId);
        wordDao.close(); // Zamknij WordDao po użyciu
        if (!courseWords.isEmpty()) {
            Log.d(TAG, "Pobrano słowa przez WordDao. Liczba słów: " + courseWords.size());
            return courseWords;
        }

        Log.w(TAG, "Ostatecznie nie znaleziono słów dla kursu ID: " + courseId);
        return new ArrayList<>();
    }


    private void showNextWord() {
        if (words == null || words.isEmpty()) {
            Log.e(TAG, "showNextWord: lista słów jest null lub pusta.");
            Toast.makeText(this, "Brak słów do wyświetlenia.", Toast.LENGTH_LONG).show();
            updateUserScoreInFirebase(currentScore); // Zapisz wynik nawet jeśli nie ma słów (choć to dziwny przypadek)
            finish();
            return;
        }

        if (currentIndex >= words.size()) {
            Log.d(TAG, "Kurs ukończony. Punkty przed bonusem: " + currentScore);
            currentScore += POINTS_COURSE_COMPLETION;
            updateScoreDisplay();
            Toast.makeText(this, "Kurs Ukończony! Zdobyłeś " + currentScore + " pkt w tej sesji.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Dodano punkty za ukończenie. Łączne punkty sesji: " + currentScore);
            updateUserScoreInFirebase(currentScore);
            finish();
            return;
        }

        updateScoreDisplay();

        Word currentWord = words.get(currentIndex);
        Log.d(TAG, "Wyświetlanie słowa: " + currentWord.getText() + " (index: " + currentIndex + "), Kategoria: " + currentWord.getCategory());
        if (wordTextView != null) {
            // Zmiana formatu pytania, jeśli słowo zawiera placeholder "___"
            if (currentWord.getText() != null && currentWord.getText().contains("___")) {
                wordTextView.setText(currentWord.getText().replace("___", " _____ "));
            } else {
                wordTextView.setText("Co to znaczy: " + currentWord.getText());
            }
        }

        List<String> options = getRandomOptions(currentWord);
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i] != null) {
                if (i < options.size()) {
                    optionButtons[i].setText(options.get(i));
                    int finalI = i;
                    optionButtons[i].setOnClickListener(v -> {
                        // Sprawdź, czy kliknięta opcja to poprawne tłumaczenie
                        handleAnswer(options.get(finalI).equals(currentWord.getTranslation()));
                    });
                    optionButtons[i].setEnabled(true);
                } else {
                    optionButtons[i].setText(""); // Wyczyść tekst, jeśli mniej niż 4 opcje
                    optionButtons[i].setEnabled(false);
                }
            }
        }
        int progress = (int) (((float) (currentIndex + 1) / words.size()) * 100);
        if (loadingProgressBar != null) {
            loadingProgressBar.setProgress(progress);
            loadingProgressBar.setVisibility(View.VISIBLE); // Upewnij się, że jest widoczny jako pasek postępu
            loadingProgressBar.setIndeterminate(false); // Nie jest już nieokreślony
        }
    }

    private void handleAnswer(boolean isCorrect) {
        if (isCorrect) {
            Log.d(TAG, "Odpowiedź poprawna.");
            currentScore += POINTS_CORRECT_ANSWER;
            Toast.makeText(this, "Dobrze! +" + POINTS_CORRECT_ANSWER + " pkt", Toast.LENGTH_SHORT).show();
            currentIndex++;
            showNextWord();
        } else {
            Log.d(TAG, "Odpowiedź niepoprawna.");
            currentScore += POINTS_WRONG_ANSWER;
            Toast.makeText(this, "Źle! " + POINTS_WRONG_ANSWER + " pkt", Toast.LENGTH_SHORT).show();
            // Użytkownik pozostaje na tym samym pytaniu lub przechodzi dalej - tutaj przechodzi dalej
            // currentIndex++;
            // showNextWord();
            // Jeśli chcesz, aby użytkownik pozostał na tym samym pytaniu, usuń powyższe dwie linie
            // i ewentualnie zablokuj przyciski na chwilę lub zmień ich kolor.
        }
        updateScoreDisplay();
        Log.d(TAG, "Aktualne punkty sesji: " + currentScore);
    }

    private void updateUserScoreInFirebase(int pointsToAdd) {
        if (currentUserId == null || userRepository == null || pointsToAdd == 0) {
            Log.d(TAG, "updateUserScoreInFirebase: Nie ma potrzeby aktualizacji lub brak danych użytkownika/repo.");
            return;
        }
        Log.d(TAG, "Aktualizowanie punktów użytkownika w Firebase o: " + pointsToAdd);
        userRepository.updatePoints(currentUserId, pointsToAdd);
    }

    private List<String> getRandomOptions(Word correctWord) {
        List<String> options = new ArrayList<>();
        if (correctWord == null || correctWord.getTranslation() == null) {
            Log.e(TAG, "getRandomOptions: correctWord lub jego tłumaczenie jest null");
            for (int i = 0; i < NUMBER_OF_OPTIONS; i++) options.add("Błąd opcji " + (i + 1));
            return options;
        }
        options.add(correctWord.getTranslation());

        // 1. Użyj predefiniowanych dystraktorów, jeśli są dostępne
        List<String> predefined = correctWord.getPredefinedDistractorsList();
        if (predefined != null && !predefined.isEmpty()) {
            for (String distractor : predefined) {
                if (options.size() < NUMBER_OF_OPTIONS && !options.contains(distractor)) {
                    options.add(distractor);
                }
            }
            Collections.shuffle(options); // Wmieszaj predefiniowane z poprawną odpowiedzią
            Log.d(TAG, "Użyto predefiniowanych dystraktorów: " + options);
            // Jeśli mamy już wystarczająco opcji, zwróć je
            if (options.size() == NUMBER_OF_OPTIONS) {
                Collections.shuffle(options); // Upewnij się, że poprawna odpowiedź jest wmieszana
                return options;
            }
        }


        // 2. Spróbuj znaleźć dystraktory z tej samej kategorii
        if (correctWord.getCategory() != null && !correctWord.getCategory().isEmpty() && words != null) {
            List<Word> sameCategoryWords = words.stream()
                    .filter(word -> correctWord.getCategory().equals(word.getCategory()) && !correctWord.equals(word) && !options.contains(word.getTranslation()))
                    .collect(Collectors.toList());
            Collections.shuffle(sameCategoryWords);
            for (Word word : sameCategoryWords) {
                if (options.size() < NUMBER_OF_OPTIONS && !options.contains(word.getTranslation())) {
                    options.add(word.getTranslation());
                }
            }
            Log.d(TAG, "Po dodaniu z tej samej kategorii: " + options);
        }

        // 3. Jeśli nadal brakuje opcji, dobierz losowe z całego kursu (inne niż już dodane i poprawna)
        if (words != null) {
            List<Word> tempList = new ArrayList<>(words);
            tempList.remove(correctWord); // Usuń poprawne słowo, aby go nie wylosować jako dystraktor
            Collections.shuffle(tempList);

            for (Word word : tempList) {
                if (options.size() < NUMBER_OF_OPTIONS && !options.contains(word.getTranslation())) {
                    options.add(word.getTranslation());
                }
            }
            Log.d(TAG, "Po dodaniu losowych z kursu: " + options);
        }


        // 4. Jeśli nadal brakuje (np. bardzo mały kurs), dodaj opcje zastępcze
        int optionSuffix = 1;
        while (options.size() < NUMBER_OF_OPTIONS) {
            String tempOption = "Opcja " + optionSuffix++;
            // Upewnij się, że opcja zastępcza nie jest przypadkiem poprawnym tłumaczeniem
            // lub już istniejącą opcją (mało prawdopodobne, ale dla pewności)
            if (!options.contains(tempOption) && (correctWord.getTranslation() == null || !correctWord.getTranslation().equalsIgnoreCase(tempOption))) {
                options.add(tempOption);
            } else if (optionSuffix > 100) { // Zabezpieczenie przed nieskończoną pętlą
                Log.e(TAG, "Nie można wygenerować unikalnych opcji zastępczych.");
                break;
            }
        }
        Log.d(TAG, "Po dodaniu opcji zastępczych (jeśli potrzebne): " + options);


        Collections.shuffle(options);
        Log.d(TAG, "Wygenerowane ostateczne opcje: " + options.toString());
        return options.subList(0, Math.min(options.size(), NUMBER_OF_OPTIONS)); // Upewnij się, że zwracamy dokładnie tyle opcji ile trzeba
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (courseDao != null) {
            courseDao.close(); // Zamknij CourseDao
        }
        if (dbHelper != null) {
            // dbHelper jest zamykany przez DAO, które go używają,
            // ale jeśli WordDao nie jest zamykane gdzie indziej, można to zrobić tutaj.
            // Jednak WordDao jest teraz zamykane w getWordsForCourseFromDb.
        }
    }
}