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

import androidx.annotation.NonNull; // Upewnij się, że jest, jeśli UserRepository.UserOperationCallback tego wymaga
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.dao.WordDao; // Dodaj import dla WordDao
import com.example.a404.data.model.Course;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.data.repository.GamificationRepository; // <<< DODAJ IMPORT
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
    private int currentSessionScore = 0; // Zmieniono nazwę z currentScore
    private TextView wordTextView;
    private TextView scoreTextView;
    private Button[] optionButtons = new Button[NUMBER_OF_OPTIONS]; // Użyj stałej
    private ProgressBar loadingProgressBar;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;

    private FirebaseSource firebaseSource;      // <<< DODAJ POLE
    private UserRepository userRepository;
    private GamificationRepository gamificationRepository; // <<< DODAJ POLE
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
        courseDao = new CourseDao(dbHelper);

        firebaseSource = new FirebaseSource(); // Inicjalizacja
        userRepository = new UserRepository(firebaseSource);
        gamificationRepository = new GamificationRepository(firebaseSource, getApplicationContext()); // Inicjalizacja

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            // fetchInitialUserScore(); // Można pominąć
        } else {
            Toast.makeText(this, "Błąd: Użytkownik niezalogowany.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User not logged in. Finishing activity.");
            finish();
            return;
        }

        updateScoreDisplay(); // Pokaż początkowy wynik sesji (0)

        long courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId != -1) {
            // ... (reszta logiki ładowania)
            if (wordTextView != null) wordTextView.setText("");
            for (Button btn : optionButtons) if (btn != null) btn.setVisibility(View.INVISIBLE);
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
            loadWordsForCourseAsync(courseId);
        } else {
            // ... (obsługa błędu)
        }
    }

    private void updateScoreDisplay() {
        if (scoreTextView != null) {
            scoreTextView.setText("Punkty sesji: " + currentSessionScore); // Zmieniono etykietę
        }
    }

    // fetchInitialUserScore() jest opcjonalne, jeśli nie wyświetlasz całkowitych punktów na bieżąco
    // private void fetchInitialUserScore() { ... }


    private void loadWordsForCourseAsync(long courseId) {
        Log.d(TAG, "Rozpoczynanie asynchronicznego ładowania słów dla kursu: " + courseId);
        if (loadingProgressBar != null) {
            loadingProgressBar.setIndeterminate(true);
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        executorService.execute(() -> {
            final List<Word> loadedWords = getWordsForCourseFromDb(courseId);
            mainThreadHandler.post(() -> {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setIndeterminate(false);
                    loadingProgressBar.setVisibility(View.GONE);
                }
                if (loadedWords != null && !loadedWords.isEmpty()) {
                    words = loadedWords;
                    Collections.shuffle(words);
                    currentIndex = 0;
                    currentSessionScore = 0; // Resetuj wynik dla nowej gry
                    updateScoreDisplay();
                    showNextWord();
                    for (Button btn : optionButtons) if (btn != null) btn.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Nie załadowano słów lub lista jest pusta dla kursu ID: " + courseId);
                    Toast.makeText(WordGameActivity.this, "Nie udało się załadować słów dla tego kursu.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private List<Word> getWordsForCourseFromDb(long courseId) {
        Log.d(TAG, "Pobieranie słów z DB dla kursu: " + courseId);
        WordDao wordDao = new WordDao(dbHelper); // Użyj WordDao
        List<Word> courseWords = wordDao.getWordsByCourseId(courseId);
        // wordDao.close(); // DAO powinno być zarządzane przez helper lub nie zamykane tutaj, jeśli dbHelper jest singletonem
        // Jeśli dbHelper nie jest singletonem, a WordDao otwiera i zamyka połączenie, to jest OK.
        // Bezpieczniej jest nie zamykać tutaj, chyba że wiesz dokładnie, jak działa WordDao.
        if (courseWords != null && !courseWords.isEmpty()) {
            Log.d(TAG, "Pobrano słowa przez WordDao. Liczba słów: " + courseWords.size());
            return courseWords;
        }
        Log.w(TAG, "Ostatecznie nie znaleziono słów dla kursu ID: " + courseId);
        return new ArrayList<>();
    }


    private void showNextWord() {
        if (words == null || words.isEmpty()) {
            Log.e(TAG, "showNextWord: lista słów jest null lub pusta.");
            // finalizeScoreAndCheckAchievements(currentSessionScore); // Można wywołać, jeśli coś zdobyto
            if (!isFinishing()) finish();
            return;
        }

        if (currentIndex >= words.size()) {
            Log.d(TAG, "Kurs ukończony. Punkty sesji przed bonusem: " + currentSessionScore);
            currentSessionScore += POINTS_COURSE_COMPLETION;
            updateScoreDisplay();
            Toast.makeText(this, "Kurs Ukończony! Zdobyłeś " + currentSessionScore + " pkt w tej sesji.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Dodano punkty za ukończenie. Łączne punkty sesji: " + currentSessionScore);
            finalizeScoreAndCheckAchievements(currentSessionScore); // <<< ZMIENIONO WYWOŁANIE
            return;
        }

        updateScoreDisplay();
        Word currentWord = words.get(currentIndex);
        // ... (reszta logiki showNextWord - wyświetlanie słowa, opcji, progress bar) ...
        if (wordTextView != null) {
            if (currentWord.getText() != null && currentWord.getText().contains("___")) {
                wordTextView.setText(currentWord.getText().replace("___", " _____ "));
            } else {
                wordTextView.setText("Co to znaczy: " + currentWord.getText());
            }
        }
        List<String> options = getRandomOptions(currentWord);
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i] != null) {
                optionButtons[i].setEnabled(true);
                if (i < options.size()) {
                    final String selectedOption = options.get(i);
                    optionButtons[i].setText(selectedOption);
                    optionButtons[i].setOnClickListener(v -> {
                        for (Button btn : optionButtons) btn.setEnabled(false);
                        handleAnswer(selectedOption.equals(currentWord.getTranslation()));
                    });
                    optionButtons[i].setVisibility(View.VISIBLE);
                } else {
                    optionButtons[i].setVisibility(View.GONE);
                }
            }
        }
        int progress = (int) (((float) (currentIndex + 1) / words.size()) * 100);
        if (loadingProgressBar != null) {
            loadingProgressBar.setProgress(progress);
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setIndeterminate(false);
        }
    }

    private void handleAnswer(boolean isCorrect) {
        if (isCorrect) {
            currentSessionScore += POINTS_CORRECT_ANSWER;
            Toast.makeText(this, "Dobrze! +" + POINTS_CORRECT_ANSWER + " pkt", Toast.LENGTH_SHORT).show();
        } else {
            currentSessionScore += POINTS_WRONG_ANSWER;
            Toast.makeText(this, "Źle! " + POINTS_WRONG_ANSWER + " pkt", Toast.LENGTH_SHORT).show();
        }
        updateScoreDisplay();
        Log.d(TAG, "Aktualne punkty sesji: " + currentSessionScore);
        currentIndex++;

        new Handler(Looper.getMainLooper()).postDelayed(this::showNextWord, 300);
    }

    // Zmieniono nazwę metody i dodano logikę sprawdzania osiągnięć
    private void finalizeScoreAndCheckAchievements(int pointsToAddThisSession) {
        if (currentUserId == null || userRepository == null) {
            Log.e(TAG, "finalizeScore: currentUserId lub userRepository jest null. Cannot update score.");
            if (currentIndex >= words.size() && !isFinishing()) finish();
            return;
        }
        // Jeśli nie ma punktów do dodania, a gra się zakończyła, po prostu zakończ.
        if (pointsToAddThisSession == 0 && currentIndex >= words.size()) {
            Log.d(TAG, "No points to add from session, game finished.");
            if (!isFinishing()) finish();
            return;
        }
        // Jeśli nie ma punktów do dodania, ale gra trwa (co nie powinno się zdarzyć przy wywołaniu tej metody)
        if (pointsToAddThisSession == 0) {
            Log.d(TAG, "No points to add from session, but game not formally finished by reaching end of words.");
            return; // Nie rób nic, jeśli nie ma punktów do zapisania, a gra nie doszła do końca
        }

        Log.d(TAG, "Finalizowanie wyniku. Aktualizowanie punktów użytkownika ("+currentUserId+") w Firebase o: " + pointsToAddThisSession);
        userRepository.updatePoints(currentUserId, pointsToAddThisSession, (success, e) -> {
            if (success) {
                Log.i(TAG, "Punkty użytkownika pomyślnie zaktualizowane w Firebase.");
                // Po pomyślnej aktualizacji punktów, pobierz zaktualizowany profil i sprawdź osiągnięcia
                firebaseSource.getUserProfile(currentUserId, (updatedProfile, profileError) -> {
                    if (profileError == null && updatedProfile != null) {
                        Log.d(TAG, "Pobrano zaktualizowany profil, punkty: " + updatedProfile.getPoints() + ", seria: " + updatedProfile.getCurrentStreak());
                        // === WYWOŁANIE SPRAWDZANIA OSIĄGNIĘĆ ===
                        gamificationRepository.checkAndUnlockSpecificAchievements(currentUserId, updatedProfile);
                    } else {
                        Log.e(TAG, "Błąd pobierania zaktualizowanego profilu po aktualizacji punktów.", profileError);
                    }
                    // Zakończ aktywność, jeśli gra została formalnie ukończona (wszystkie słowa przerobione)
                    if (currentIndex >= words.size() && !isFinishing()) {
                        Log.d(TAG, "Gra ukończona, zamykanie WordGameActivity.");
                        finish();
                    }
                });
            } else {
                Toast.makeText(WordGameActivity.this, "Błąd zapisu punktów.", Toast.LENGTH_SHORT).show();
                if (currentIndex >= words.size() && !isFinishing()) {
                    Log.d(TAG, "Gra ukończona (z błędem zapisu punktów), zamykanie WordGameActivity.");
                    finish();
                }
            }
        });
    }

    private List<String> getRandomOptions(Word correctWord) {
        // ... (kod tej metody bez zmian z poprzedniej wersji) ...
        List<String> options = new ArrayList<>();
        if (correctWord == null || correctWord.getTranslation() == null) {
            Log.e(TAG, "getRandomOptions: correctWord lub jego tłumaczenie jest null");
            for (int i = 0; i < NUMBER_OF_OPTIONS; i++) options.add("Opcja " + (i + 1));
            return options;
        }
        options.add(correctWord.getTranslation());

        List<String> predefined = correctWord.getPredefinedDistractorsList();
        if (predefined != null && !predefined.isEmpty()) {
            for (String distractor : predefined) {
                if (options.size() < NUMBER_OF_OPTIONS && !options.contains(distractor)) {
                    options.add(distractor);
                }
            }
            if (options.size() == NUMBER_OF_OPTIONS) {
                Collections.shuffle(options);
                return options;
            }
        }

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
        }

        if (words != null) {
            List<Word> tempList = new ArrayList<>(words);
            tempList.remove(correctWord);
            Collections.shuffle(tempList);
            for (Word word : tempList) {
                if (options.size() < NUMBER_OF_OPTIONS && !options.contains(word.getTranslation())) {
                    options.add(word.getTranslation());
                }
            }
        }

        int optionSuffix = 1;
        while (options.size() < NUMBER_OF_OPTIONS) {
            String tempOption = "Opcja " + optionSuffix++;
            if (!options.contains(tempOption) && (correctWord.getTranslation() == null || !correctWord.getTranslation().equalsIgnoreCase(tempOption))) {
                options.add(tempOption);
            } else if (optionSuffix > 100) break;
        }
        Collections.shuffle(options);
        return options.subList(0, Math.min(options.size(), NUMBER_OF_OPTIONS));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane. Aktywność kończy się: " + isFinishing());
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "Zamykanie executorService.");
            executorService.shutdownNow();
        }
        // Nie zamykaj DAO tutaj, jeśli dbHelper jest współdzielony lub zarządzany inaczej.
        // if (courseDao != null) courseDao.close();
        // dbHelper jest prawdopodobnie zamykany przez DAO.
    }
}