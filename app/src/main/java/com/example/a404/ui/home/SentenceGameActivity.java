package com.example.a404.ui.home;

import android.os.Bundle;
import android.util.Log; // Dodaj import dla Log
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData; // Dodaj
import androidx.lifecycle.Observer;  // Dodaj

import com.example.a404.R;
import com.example.a404.data.model.UserProfile;     // Dodaj
import com.example.a404.data.repository.UserRepository; // Dodaj
import com.example.a404.data.source.FirebaseSource;      // Dodaj
import com.google.firebase.auth.FirebaseAuth;           // Dodaj
import com.google.firebase.auth.FirebaseUser;           // Dodaj

public class SentenceGameActivity extends AppCompatActivity {
    private static final String TAG = "SentenceGameActivity"; // Dodaj TAG dla logowania
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
            {"go", "goes", "went"},
            {"is", "are", "am"},
            {"watched", "watch", "watches"},
            {"did", "do", "does"},
            {"go", "goes", "went"},
            {"sits", "sit", "sat"},
            {"is", "are", "am"},
            {"was", "is", "were"},
            {"is", "are", "am"},
            {"has", "have", "had"}
    };

    private int[] correctAnswers = {
            0, 0, 0, 0, 0, 2, 1, 0, 1, 1
    };

    private int currentSentenceIndex = 0;
    private int currentSessionScore = 0; // Punkty zdobyte w tej sesji gry
    private UserRepository userRepository;
    private String currentUserId;

    private static final int POINTS_CORRECT_SENTENCE = 6;
    private static final int POINTS_WRONG_SENTENCE = -2;
    private static final int POINTS_SENTENCE_GAME_COMPLETION = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_game);

        sentenceTextView = findViewById(R.id.sentenceTextView);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);

        // Inicjalizacja UserRepository
        FirebaseSource firebaseSource = new FirebaseSource(); // Zakładając prosty konstruktor
        userRepository = new UserRepository(firebaseSource);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            fetchInitialUserScore(); // Opcjonalne: pobierz i zaloguj początkowy wynik
        } else {
            Toast.makeText(this, "Błąd: Użytkownik niezalogowany.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Użytkownik niezalogowany, zamykanie SentenceGameActivity.");
            finish();
            return;
        }

        loadSentence();

        option1.setOnClickListener(v -> checkAnswer(0));
        option2.setOnClickListener(v -> checkAnswer(1));
        option3.setOnClickListener(v -> checkAnswer(2));
    }

    private void fetchInitialUserScore() {
        if (currentUserId == null || userRepository == null) return;

        LiveData<UserProfile> userProfileLiveData = userRepository.getUserProfile(currentUserId);
        userProfileLiveData.observe(this, new Observer<UserProfile>() {
            @Override
            public void onChanged(UserProfile userProfile) {
                if (userProfile != null) {
                    Log.d(TAG, "Pobrano profil użytkownika, aktualne punkty w Firebase: " + userProfile.getPoints());
                }
                userProfileLiveData.removeObserver(this); // Usuń, jeśli potrzebujesz tylko jednorazowego odczytu
            }
        });
    }

    private void loadSentence() {
        if (currentSentenceIndex < sampleSentences.length) {
            sentenceTextView.setText(sampleSentences[currentSentenceIndex]);
            option1.setText(options[currentSentenceIndex][0]);
            option2.setText(options[currentSentenceIndex][1]);
            option3.setText(options[currentSentenceIndex][2]);
        } else {
            // Ta sytuacja nie powinna wystąpić, jeśli logika w checkAnswer jest poprawna
            Log.e(TAG, "loadSentence wywołane, ale currentSentenceIndex jest poza zakresem.");
            endGame();
        }
    }

    private void checkAnswer(int selectedIndex) {
        if (currentSentenceIndex >= sampleSentences.length) {
            Log.w(TAG, "checkAnswer wywołane, ale gra już powinna być zakończona.");
            return; // Zabezpieczenie przed wielokrotnym zakończeniem gry
        }

        if (selectedIndex == correctAnswers[currentSentenceIndex]) {
            currentSessionScore += POINTS_CORRECT_SENTENCE;
            Toast.makeText(this, "Prawidłowo! +" + POINTS_CORRECT_SENTENCE + " pkt", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Prawidłowa odpowiedź. Punkty sesji: " + currentSessionScore);
        } else {
            currentSessionScore += POINTS_WRONG_SENTENCE;
            Toast.makeText(this, "Niepoprawnie. " + POINTS_WRONG_SENTENCE + " pkt", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Niepoprawna odpowiedź. Punkty sesji: " + currentSessionScore);
        }

        currentSentenceIndex++;
        if (currentSentenceIndex < sampleSentences.length) {
            loadSentence();
        } else {
            endGame();
        }
    }

    private void endGame() {
        Log.d(TAG, "Koniec gry. Punkty zdobyte w tej sesji: " + currentSessionScore);
        currentSessionScore += POINTS_SENTENCE_GAME_COMPLETION;
        Toast.makeText(this, "Koniec gry! Zdobyłeś " + currentSessionScore + " pkt w tej grze.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Dodano punkty za ukończenie. Łączne punkty sesji do zapisu: " + currentSessionScore);

        updateUserScoreInFirebase(currentSessionScore);
        finish();
    }

    private void updateUserScoreInFirebase(int pointsToAdd) {
        if (currentUserId == null || userRepository == null || pointsToAdd == 0) {
            Log.d(TAG, "updateUserScoreInFirebase: Brak potrzeby aktualizacji lub brak danych użytkownika/repo.");
            return;
        }
        Log.d(TAG, "Aktualizowanie punktów użytkownika w Firebase o: " + pointsToAdd);
        userRepository.updatePoints(currentUserId, pointsToAdd); // Zakładamy, że ta metoda jest asynchroniczna
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane dla SentenceGameActivity");
        // Można rozważyć zapisanie punktów, jeśli gra zostanie przerwana przedwcześnie,
        // ale obecnie punkty są zapisywane tylko po ukończeniu.
        // if (currentSessionScore != 0 && currentSentenceIndex < sampleSentences.length) {
        //    Log.d(TAG, "Gra przerwana, zapisywanie dotychczasowych punktów sesji: " + currentSessionScore);
        //    updateUserScoreInFirebase(currentSessionScore);
        // }
    }
}