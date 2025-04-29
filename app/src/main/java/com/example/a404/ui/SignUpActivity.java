package com.example.a404.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Dodaj import Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a404.R;
import com.example.a404.data.repository.UserRepository; // Import UserRepository
import com.example.a404.data.source.FirebaseSource;    // Import FirebaseSource
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;          // Import FirebaseUser

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity"; // Dodaj TAG do logowania
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private FirebaseAuth auth;
    private UserRepository userRepository; // Dodaj instancję UserRepository

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        // Inicjalizuj UserRepository (wymaga FirebaseSource)
        userRepository = new UserRepository(new FirebaseSource());

        etEmail = findViewById(R.id.etEmailSignUp);
        etPassword = findViewById(R.id.etPasswordSignUp);
        etConfirmPassword = findViewById(R.id.etConfirmPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Proszę wypełnić wszystkie pola.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Hasła nie pasują!", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Rejestracja w Firebase Auth udana.");
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                String userEmail = firebaseUser.getEmail(); // Pobierz email z FirebaseUser
                                // Użyj email z FirebaseUser lub z pola EditText, jeśli wolisz
                                String username = userEmail != null ? userEmail.split("@")[0] : "User"; // Prosta nazwa użytkownika

                                Log.d(TAG, "Tworzenie profilu użytkownika w Firestore dla userId: " + userId);
                                // Wywołaj metodę tworzenia profilu użytkownika
                                // Zakładamy, że createUserProfile przyjmuje userId, username i email
                                // oraz ustawia domyślne wartości dla punktów i języka.
                                // Dostosuj wywołanie do faktycznej sygnatury metody w UserRepository.
                                userRepository.createUserProfile(userId, username); // Usuń userEmail

                                Toast.makeText(this, "Rejestracja przebiegła pomyślnie!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, SignInActivity.class));
                                finish();
                            } else {
                                Log.e(TAG, "FirebaseUser jest null po udanej rejestracji.");
                                Toast.makeText(this, "Błąd: Nie można uzyskać danych użytkownika.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Błąd rejestracji w Firebase Auth: ", task.getException());
                            Toast.makeText(this, "Błąd rejestracji: " + task.getException().getMessage(), Toast.LENGTH_LONG).show(); // Dłuższy Toast dla błędów
                        }
                    });
        });

        TextView tvGoToSignIn = findViewById(R.id.tvGoToSignIn);
        tvGoToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });
    }
}