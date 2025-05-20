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

import androidx.annotation.NonNull; // Dodaj, jeśli potrzebne dla callbacków
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData; // Dodaj
import androidx.lifecycle.Observer;  // Dodaj

import com.example.a404.R;
import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.UserProfile; // Dodaj
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.data.repository.UserRepository; // Dodaj
import com.example.a404.data.source.FirebaseSource;  // Dodaj
import com.google.firebase.auth.FirebaseAuth;       // Dodaj
import com.google.firebase.auth.FirebaseUser;       // Dodaj

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordGameActivity extends AppCompatActivity {

    private static final String TAG = "WordGameActivity";
    private List<Word> words = new ArrayList<>();
    private int currentIndex = 0;
    private int currentScore = 0; // Lokalna zmienna do śledzenia wyniku w tej sesji gry
    private TextView wordTextView;
    private Button[] optionButtons = new Button[4];
    private ProgressBar gameProgressBar;
    private CourseDao courseDao;
    private WordDbHelper dbHelper;
    private UserRepository userRepository; // Dodaj
    private String currentUserId;        // Dodaj

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static final int POINTS_CORRECT_ANSWER = 15;
    private static final int POINTS_WRONG_ANSWER = -5;
    private static final int POINTS_COURSE_COMPLETION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game);

        wordTextView = findViewById(R.id.wordTextView);
        gameProgressBar = findViewById(R.id.progressBar);

        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);

        dbHelper = new WordDbHelper(this);
        courseDao = new CourseDao(dbHelper);

        // Inicjalizacja UserRepository
        // Zakładam, że FirebaseSource jest prostym dostawcą instancji Firebase
        FirebaseSource firebaseSource = new FirebaseSource();
        userRepository = new UserRepository(firebaseSource);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            fetchInitialUserScore(); // Pobierz początkowy wynik użytkownika
        } else {
            Toast.makeText(this, "Błąd: Użytkownik niezalogowany.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        long courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId != -1) {
            Log.d(TAG, "Otrzymano COURSE_ID: " + courseId);
            wordTextView.setVisibility(View.INVISIBLE);
            for (Button btn : optionButtons) {
                btn.setVisibility(View.INVISIBLE);
            }
            gameProgressBar.setVisibility(View.INVISIBLE);
            loadWordsForCourseAsync(courseId);
        } else {
            Log.e(TAG, "Nieprawidłowy COURSE_ID (-1)");
            Toast.makeText(this, "Błąd: Nieprawidłowy ID kursu.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fetchInitialUserScore() {
        if (currentUserId == null || userRepository == null) return;

        // Użyj LiveData z UserRepository, jeśli tak jest zaimplementowane
        // Dla uproszczenia, zakładam, że masz metodę w FirebaseSource lub UserRepository
        // zwracającą UserProfile bezpośrednio (asynchronicznie) lub używasz LiveData.
        // Poniżej przykład z LiveData:
        LiveData<UserProfile> userProfileLiveData = userRepository.getUserProfile(currentUserId);
        userProfileLiveData.observe(this, new Observer<UserProfile>() {
            @Override
            public void onChanged(UserProfile userProfile) {
                if (userProfile != null) {
                    // Nie ustawiamy currentScore na punkty z Firebase,
                    // currentScore to punkty ZDOBYTE W TEJ SESJI.
                    // Ale możemy wyświetlić całkowite punkty użytkownika, jeśli chcesz.
                    Log.d(TAG, "Pobrano profil użytkownika, aktualne punkty w Firebase: " + userProfile.getPoints());
                }
                // Usuń obserwatora po pierwszym odczycie, jeśli nie potrzebujesz ciągłych aktualizacji tutaj
                userProfileLiveData.removeObserver(this);
            }
        });
    }


    private void loadWordsForCourseAsync(long courseId) {
        Log.d(TAG, "Rozpoczynanie asynchronicznego ładowania słów dla kursu: " + courseId);
        executorService.execute(() -> {
            final List<Word> loadedWords = getWordsForCourseFromDb(courseId);
            Log.d(TAG, "Wątek tła: Załadowano " + (loadedWords != null ? loadedWords.size() : 0) + " słów.");
            mainThreadHandler.post(() -> {
                if (loadedWords != null && !loadedWords.isEmpty()) {
                    this.words = loadedWords;
                    Log.d(TAG, "Słowa pomyślnie załadowane i przypisane.");
                    wordTextView.setVisibility(View.VISIBLE);
                    for (Button btn : optionButtons) {
                        btn.setVisibility(View.VISIBLE);
                    }
                    gameProgressBar.setVisibility(View.VISIBLE);
                    showNextWord();
                } else {
                    Log.e(TAG, "Nie udało się załadować słów lub lista jest pusta.");
                    Toast.makeText(WordGameActivity.this, "Nie udało się załadować słów dla tego kursu.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private List<Word> getWordsForCourseFromDb(long courseId) {
        Log.d(TAG, "Pobieranie słów z DB dla kursu: " + courseId);
        Course course = courseDao.getCourseById(courseId);
        if (course != null && course.getWords() != null) {
            Log.d(TAG, "Znaleziono kurs i słowa. Liczba słów: " + course.getWords().size());
            return course.getWords();
        }
        Log.w(TAG, "Nie znaleziono kursu lub kurs nie ma słów dla ID: " + courseId);
        return new ArrayList<>();
    }

    private void showNextWord() {
        if (words == null || words.isEmpty()) {
            Log.e(TAG, "showNextWord: lista słów jest null lub pusta.");
            Toast.makeText(this, "Brak słów do wyświetlenia.", Toast.LENGTH_LONG).show();
            updateUserScoreInFirebase(currentScore); // Zaktualizuj wynik, nawet jeśli gra kończy się błędem
            finish();
            return;
        }

        if (currentIndex >= words.size()) {
            Log.d(TAG, "Kurs ukończony. Aktualne punkty sesji: " + currentScore);
            Toast.makeText(this, "Kurs Ukończony! Zdobyłeś " + (currentScore+10) + " pkt w tej sesji.", Toast.LENGTH_LONG).show();
            currentScore += POINTS_COURSE_COMPLETION; // Dodaj punkty za ukończenie kursu
            Log.d(TAG, "Dodano punkty za ukończenie. Łączne punkty sesji: " + currentScore);
            updateUserScoreInFirebase(currentScore);
            finish();
            return;
        }

        Word currentWord = words.get(currentIndex);
        Log.d(TAG, "Wyświetlanie słowa: " + currentWord.getText() + " (index: " + currentIndex + ")");
        wordTextView.setText("Co to znaczy: " + currentWord.getText());

        List<String> options = getRandomOptions(currentWord);
        for (int i = 0; i < optionButtons.length; i++) {
            if (i < options.size()) {
                final String selectedOption = options.get(i);
                optionButtons[i].setText(selectedOption);
                optionButtons[i].setOnClickListener(v -> {
                    Log.d(TAG, "Opcja kliknięta: " + selectedOption);
                    handleAnswer(selectedOption.equals(currentWord.getTranslation()));
                });
            } else {
                optionButtons[i].setVisibility(View.GONE);
            }
        }
        int progress = (int) (((float) (currentIndex + 1) / words.size()) * 100);
        gameProgressBar.setProgress(progress);
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
            // Punkty nie mogą być na minusie (dotyczy punktów ZDOBYTYCH W TEJ SESJI,
            // ogólne punkty użytkownika w Firebase będą zarządzane przez FieldValue.increment,
            // które może zejść do zera, jeśli tak zaimplementujesz logikę w UserRepository)
            // Dla currentScore w tej sesji, możemy zrobić tak:
            // currentScore = Math.max(0, currentScore);
            // Ale jeśli chcemy, aby punkty użytkownika W OGÓLE nie spadły poniżej zera,
            // to logikę trzeba dodać przy aktualizacji w Firebase, np. pobierając punkty,
            // dodając, sprawdzając czy >=0 i dopiero zapisując.
            // Prostsze jest pozwolenie FieldValue.increment działać, a UserRepository może mieć reguły.
            // Tutaj dla punktów sesji, nie ma to znaczenia, bo dodajemy je potem do całości.
            Toast.makeText(this, "Źle! " + POINTS_WRONG_ANSWER + " pkt", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "Aktualne punkty sesji: " + currentScore);
    }

    private void updateUserScoreInFirebase(int pointsToAdd) {
        if (currentUserId == null || userRepository == null || pointsToAdd == 0) {
            Log.d(TAG, "updateUserScoreInFirebase: Nie ma potrzeby aktualizacji lub brak danych użytkownika/repo.");
            return;
        }

        Log.d(TAG, "Aktualizowanie punktów użytkownika w Firebase o: " + pointsToAdd);
        // Zakładamy, że UserRepository.updatePoints używa FieldValue.increment()
        // i jest asynchroniczne.
        userRepository.updatePoints(currentUserId, pointsToAdd);

        // Jeśli chcesz upewnić się, że punkty użytkownika nie spadną poniżej 0 w Firebase,
        // musiałbyś zmodyfikować `userRepository.updatePoints` lub zrobić to tutaj bardziej złożenie:
        // 1. Pobierz UserProfile.
        // 2. Oblicz nowe punkty: userProfile.getPoints() + pointsToAdd.
        // 3. Upewnij się, że nowe punkty >= 0.
        // 4. Zapisz cały UserProfile z nowymi punktami (lub tylko pole points).
        // To jest bardziej skomplikowane niż użycie FieldValue.increment().
        // Najprościej jest pozwolić FieldValue.increment() działać, a jeśli nie chcesz ujemnych punktów,
        // to po prostu wyświetlaj 0, jeśli punkty w Firebase są < 0.
    }


    private List<String> getRandomOptions(Word correctWord) {
        List<String> options = new ArrayList<>();
        if (correctWord == null || correctWord.getTranslation() == null) {
            Log.e(TAG, "getRandomOptions: correctWord lub jego tłumaczenie jest null");
            for(int i=0; i<4; i++) options.add("Błąd opcji " + (i+1));
            return options;
        }
        options.add(correctWord.getTranslation());

        if (words == null || words.size() < 4) {
            Log.w(TAG, "Zbyt mało słów w kursie, aby wygenerować unikalne błędne opcje.");
            while (options.size() < 4) {
                options.add(correctWord.getTranslation() + (options.size()));
            }
        } else {
            Random rand = new Random();
            List<Word> tempList = new ArrayList<>(words);
            tempList.remove(correctWord);

            while (options.size() < 4 && !tempList.isEmpty()) {
                Word randomWord = tempList.remove(rand.nextInt(tempList.size()));
                if (randomWord.getTranslation() != null && !options.contains(randomWord.getTranslation())) {
                    options.add(randomWord.getTranslation());
                }
            }
            while(options.size() < 4) {
                options.add("Opcja " + (options.size() +1));
            }
        }

        Collections.shuffle(options);
        Log.d(TAG, "Wygenerowane opcje: " + options.toString());
        return options;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane");

        // Zaktualizuj wynik, jeśli aktywność jest niszczona przedwcześnie,
        // a użytkownik zdobył jakieś punkty.
        // Można to pominąć, jeśli punkty mają być przyznawane tylko za ukończenie.
        // if (currentScore != 0 && currentIndex < words.size()) {
        //    updateUserScoreInFirebase(currentScore);
        // }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}