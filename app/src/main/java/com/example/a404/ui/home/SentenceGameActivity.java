package com.example.a404.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Dodaj, jeśli UserRepository.UserOperationCallback tego wymaga
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.a404.R;
import com.example.a404.data.model.UserProfile;
import com.example.a404.data.repository.GamificationRepository; // <<< DODAJ IMPORT
import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SentenceGameActivity extends AppCompatActivity {
    private static final String TAG = "SentenceGameActivity";
    private TextView sentenceTextView;
    private Button option1, option2, option3;

    private String[] sampleSentences = {
            "I ___ to school every day.", "She ___ playing the piano.", "We ___ a movie yesterday.",
            "He ___ his homework before dinner.", "They ___ to the park on Sundays.", "The cat ___ on the sofa.",
            "My friends ___ going to the party.", "It ___ raining all day.", "You ___ very kind.",
            "We ___ already finished our work."
    };

    private String[][] options = {
            {"go", "goes", "went"}, {"is", "are", "am"}, {"watched", "watch", "watches"},
            {"did", "do", "does"}, {"go", "goes", "went"}, {"sits", "sit", "sat"},
            {"is", "are", "am"}, {"was", "is", "were"}, {"is", "are", "am"},
            {"has", "have", "had"}
    };

    private int[] correctAnswers = {0, 0, 0, 0, 0, 2, 1, 0, 1, 1};

    private int currentSentenceIndex = 0;
    private int currentSessionScore = 0;

    private FirebaseSource firebaseSource; // <<< DODAJ POLE
    private UserRepository userRepository;
    private GamificationRepository gamificationRepository; // <<< DODAJ POLE
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

        firebaseSource = new FirebaseSource(); // Inicjalizacja
        userRepository = new UserRepository(firebaseSource);
        gamificationRepository = new GamificationRepository(firebaseSource, getApplicationContext()); // Inicjalizacja

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            // fetchInitialUserScore(); // Opcjonalne
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

    // fetchInitialUserScore() jest opcjonalne
    // private void fetchInitialUserScore() { ... }


    private void loadSentence() {
        if (currentSentenceIndex < sampleSentences.length) {
            sentenceTextView.setText(sampleSentences[currentSentenceIndex]);
            option1.setText(options[currentSentenceIndex][0]);
            option2.setText(options[currentSentenceIndex][1]);
            option3.setText(options[currentSentenceIndex][2]);
            // Upewnij się, że przyciski są aktywne
            option1.setEnabled(true);
            option2.setEnabled(true);
            option3.setEnabled(true);
        } else {
            // To nie powinno się zdarzyć, jeśli endGame jest wywoływane poprawnie
            Log.e(TAG, "loadSentence wywołane, ale currentSentenceIndex jest poza zakresem.");
            endGame();
        }
    }

    private void checkAnswer(int selectedIndex) {
        if (currentSentenceIndex >= sampleSentences.length) {
            Log.w(TAG, "checkAnswer wywołane, ale gra już powinna być zakończona lub jest w trakcie kończenia.");
            return;
        }
        // Wyłącz przyciski po odpowiedzi
        option1.setEnabled(false);
        option2.setEnabled(false);
        option3.setEnabled(false);

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

        // Małe opóźnienie przed pokazaniem następnego zdania lub zakończeniem gry
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentSentenceIndex < sampleSentences.length) {
                loadSentence();
            } else {
                endGame();
            }
        }, 300); // Opóźnienie 1.2 sekundy
    }

    private void endGame() {
        // Zabezpieczenie przed wielokrotnym wywołaniem endGame, jeśli np. opóźnienie się nałoży
        if (isFinishing() || currentSentenceIndex < sampleSentences.length) {
            // Jeśli aktywność już się kończy LUB jeśli gra formalnie jeszcze się nie skończyła
            // (np. zostało wywołane z loadSentence po błędzie), nie rób nic więcej.
            if (!isFinishing() && currentSentenceIndex < sampleSentences.length) {
                Log.w(TAG, "endGame called prematurely, but not finishing activity yet.");
            }
            return;
        }

        Log.d(TAG, "Koniec gry. Punkty zdobyte w tej sesji (przed bonusem): " + currentSessionScore);
        currentSessionScore += POINTS_SENTENCE_GAME_COMPLETION;
        Toast.makeText(this, "Koniec gry! Zdobyłeś " + currentSessionScore + " pkt w tej grze.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Dodano punkty za ukończenie. Łączne punkty sesji do zapisu: " + currentSessionScore);

        finalizeScoreAndCheckAchievements(currentSessionScore);
        // finish() zostanie wywołane w callbacku finalizeScoreAndCheckAchievements
    }

    // Zmieniono nazwę metody i dodano logikę sprawdzania osiągnięć
    private void finalizeScoreAndCheckAchievements(int pointsToAddThisSession) {
        if (currentUserId == null || userRepository == null) {
            Log.e(TAG, "finalizeScore: currentUserId lub userRepository jest null. Cannot update score.");
            if (!isFinishing()) finish(); // Zakończ grę, jeśli nie można zapisać punktów
            return;
        }
        // Jeśli nie ma punktów do dodania, a gra się zakończyła, po prostu zakończ.
        if (pointsToAddThisSession == 0 && currentSentenceIndex >= sampleSentences.length) {
            Log.d(TAG, "No points to add from session, game finished.");
            if (!isFinishing()) finish();
            return;
        }
        // Jeśli nie ma punktów do dodania, a gra trwa (mało prawdopodobne, bo wywoływane z endGame)
        if (pointsToAddThisSession == 0) {
            Log.d(TAG, "No points to add from session, but game not formally finished by reaching end of sentences.");
            return;
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
                    // Zakończ aktywność, jeśli gra została formalnie ukończona
                    if (currentSentenceIndex >= sampleSentences.length && !isFinishing()) {
                        Log.d(TAG, "Gra ukończona, zamykanie SentenceGameActivity.");
                        finish();
                    }
                });
            } else {
                Log.e(TAG, "Błąd aktualizacji punktów użytkownika w Firebase.", e);
                Toast.makeText(SentenceGameActivity.this, "Błąd zapisu punktów.", Toast.LENGTH_SHORT).show();
                if (currentSentenceIndex >= sampleSentences.length && !isFinishing()) {
                    Log.d(TAG, "Gra ukończona (z błędem zapisu punktów), zamykanie SentenceGameActivity.");
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy wywołane dla SentenceGameActivity. Aktywność kończy się: " + isFinishing());
        // Zapis punktów jest teraz obsługiwany w endGame -> finalizeScoreAndCheckAchievements
    }
}