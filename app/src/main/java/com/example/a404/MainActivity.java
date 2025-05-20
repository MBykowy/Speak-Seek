package com.example.a404;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.a404.data.repository.UserRepository;
import com.example.a404.data.source.FirebaseSource;
import com.example.a404.databinding.ActivityMainBinding;
import com.example.a404.ui.SignInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(new FirebaseSource());

        checkUserStatus();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

         AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                 R.id.navigation_home, R.id.navigation_dashboard, R.id.languageSelectionFragment, R.id.navigation_ranking, R.id.navigation_profile)
                 .build();
         NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
         NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
         NavigationUI.setupWithNavController(binding.navView, navController);

        // Wczytaj język użytkownika po zalogowaniu
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Wczytywanie języka dla: " + currentUser.getUid());
            loadUserLanguageSettings(currentUser.getUid());
        }
    }

    private void loadUserLanguageSettings(String userId) {
        Log.d(TAG, "Wczytywanie ustawień języka dla użytkownika: " + userId);
        // Usuń poprzedniego obserwatora, jeśli byłby dodany wielokrotnie (choć w onCreate to rzadkie)
        // Jeśli masz referencję do obserwatora, możesz go usunąć, ale lepiej po prostu używać observe z LifecycleOwner
        // userRepository.getUserProfile(userId).removeObserver(yourObserverInstance);

        userRepository.getUserProfile(userId).observe(this, userProfile -> { // Użyj "this" jako LifecycleOwner
            if (userProfile != null && userProfile.getSelectedLanguageCode() != null) {
                String languageCode = userProfile.getSelectedLanguageCode();
                Log.d(TAG, "Wczytano język nauki: " + languageCode);
                // Tutaj możesz faktycznie zastosować język, np. poprzez aktualizację konfiguracji i odtworzenie aktywności
                // Ale uważaj, żeby nie wpaść w pętlę, jeśli zmiana języka powoduje ponowne wywołanie onCreate
            } else {
                Log.d(TAG, "Profil użytkownika lub kod języka nie został znaleziony.");
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            Toast.makeText(this, "Wyjście zostało ukończone", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}