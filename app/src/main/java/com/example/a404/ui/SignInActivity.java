package com.example.a404.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Dodaj ten import
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a404.MainActivity;
import com.example.a404.R;
import com.example.a404.data.model.Achievement; // Dodaj ten import
import com.example.a404.data.model.UserProfile; // Dodaj ten import
import com.example.a404.data.source.FirebaseSource; // Dodaj ten import
import com.example.a404.service.AchievementHelper; // Dodaj ten import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Dodaj ten import

import java.util.List; // Dodaj ten import

public class SignInActivity extends AppCompatActivity {
    private static final String TAG_SIGN_IN = "SignInActivity"; // Tag dla logowania
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private FirebaseAuth auth;

    // === NOWE POLA DLA OSIĄGNIĘĆ ===
    private FirebaseSource firebaseSource;
    private AchievementHelper achievementHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        // === INICJALIZACJA POMOCNIKÓW OSIĄGNIĘĆ ===
        firebaseSource = new FirebaseSource();
        achievementHelper = new AchievementHelper(getApplicationContext(), firebaseSource);

        etEmail = findViewById(R.id.etEmailSignIn);
        etPassword = findViewById(R.id.etPasswordSignIn);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Proszę podać swój adres e-mail i hasło.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show(); // Zmieniono wiadomość
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                // === SPRAWDŹ OSIĄGNIĘCIE "FIRST_LOGIN" ===
                                handleFirstLoginAchievementCheck(firebaseUser);
                            }
                            // Przeniesienie do MainActivity nastąpi po sprawdzeniu osiągnięcia (w handleFirstLoginAchievementCheck)
                            // lub jeśli nie ma użytkownika (co nie powinno się zdarzyć tutaj)
                        } else {
                            Toast.makeText(this, "Błąd logowania: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Zmieniono wiadomość
                        }
                    });
        });

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }

    private void handleFirstLoginAchievementCheck(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        Log.d(TAG_SIGN_IN, "Handling first login achievement check for UID: " + userId);

        // Krok 1: Pobierz profil użytkownika (lub utwórz, jeśli go nie ma - co jest mniej prawdopodobne przy logowaniu)
        firebaseSource.getUserProfile(userId, (profile, e) -> {
            if (e != null) {
                Log.e(TAG_SIGN_IN, "Error fetching profile for logged in user: " + userId, e);
                // Mimo błędu, kontynuuj do MainActivity, użytkownik jest zalogowany
                proceedToMainActivity();
                return;
            }

            UserProfile userProfileToUse;

            if (profile != null) {
                userProfileToUse = profile;
                Log.d(TAG_SIGN_IN, "Profile found for user: " + userProfileToUse.getUsername());
            } else {
                // Profil nie istnieje - to nie powinno się zdarzyć, jeśli proces rejestracji tworzy profil.
                // Dla bezpieczeństwa, utwórzmy podstawowy profil.
                Log.w(TAG_SIGN_IN, "Profile NOT found for UID: " + userId + ". Creating a basic profile for achievement check.");
                String username = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : (firebaseUser.getEmail() != null ? firebaseUser.getEmail().split("@")[0] : "Użytkownik");
                userProfileToUse = new UserProfile(userId, username, 0, "en"); // Użyj domyślnego języka, np. "en"

                // Zapisz ten nowo utworzony profil (ważne, aby istniał w Firestore)
                firebaseSource.createUserProfile(userProfileToUse, (success, creationEx) -> {
                    if (success) {
                        Log.d(TAG_SIGN_IN, "Basic profile created for achievement check for UID: " + userId);
                        // Kontynuuj sprawdzanie osiągnięć z nowym profilem
                        checkAchievementsWithProfile(userProfileToUse);
                    } else {
                        Log.e(TAG_SIGN_IN, "Failed to create basic profile for UID: " + userId, creationEx);
                        // Mimo to, kontynuuj do MainActivity
                        proceedToMainActivity();
                    }
                });
                return; // Zakończ, bo createUserProfile jest asynchroniczne
            }

            // Jeśli profil istniał od razu, sprawdź osiągnięcia
            checkAchievementsWithProfile(userProfileToUse);
        });
    }

    private void checkAchievementsWithProfile(UserProfile profileForCheck) {
        Log.d(TAG_SIGN_IN, "Checking achievements with profile: " + profileForCheck.getUsername());
        achievementHelper.checkAndUnlockAchievements(profileForCheck, (newlyUnlocked, error) -> {
            if (error != null) {
                Log.e(TAG_SIGN_IN, "Error checking achievements after login", error);
                // Mimo błędu, kontynuuj do MainActivity
                proceedToMainActivity();
                return;
            }

            if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
                for (Achievement ach : newlyUnlocked) {
                    if ("FIRST_LOGIN".equals(ach.getTriggerType())) { // Sprawdź typ triggera
                        Log.i(TAG_SIGN_IN, "Achievement UNLOCKED: " + ach.getName());
                        // Toast jest już wyświetlany w AchievementHelper, jeśli tak skonfigurowałeś,
                        // lub możesz go wyświetlić tutaj:
                        Toast.makeText(this, "Zdobyto osiągnięcie: " + ach.getName(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Log.d(TAG_SIGN_IN, "No new achievements unlocked upon login.");
            }

            // Niezależnie od wyniku sprawdzania osiągnięć, przejdź do MainActivity
            proceedToMainActivity();
        });
    }

    private void proceedToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Zakończ SignInActivity, aby użytkownik nie mógł do niej wrócić przyciskiem "wstecz"
    }
}