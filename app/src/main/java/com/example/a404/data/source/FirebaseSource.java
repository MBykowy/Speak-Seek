// Ścieżka: app/java/com/example/a404/data/source/FirebaseSource.java
package com.example.a404.data.source;

import android.util.Log;

import androidx.annotation.NonNull; // Dodaj ten import

import com.example.a404.data.model.Achievement; // Dodaj ten import
import com.example.a404.data.model.UnlockedAchievement; // Dodaj ten import
import com.example.a404.data.model.UserProfile; // Dodaj ten import
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference; // Dodaj ten import
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList; // Dodaj ten import
import java.util.HashMap;
import java.util.List; // Dodaj ten import
import java.util.Map;
import java.util.function.Function;

public class FirebaseSource {
    private static final String TAG = "FirebaseSource";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // === NOWA KOLEKCJA DLA OSIĄGNIĘĆ ===
    private final CollectionReference achievementsCollection;
    private final String USERS_COLLECTION = "users"; // Stała dla nazwy kolekcji użytkowników

    public FirebaseSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.achievementsCollection = db.collection("achievements"); // Inicjalizacja kolekcji osiągnięć
        Log.d(TAG, "FirebaseSource zainicjalizowany");
    }

    // === INTERFEJSY CALLBACK DLA OSIĄGNIĘĆ (jeśli jeszcze nie masz podobnych) ===
    public interface AchievementsDefinitionsCallback {
        void onCallback(List<Achievement> achievements, Exception e);
    }

    public interface UserUnlockedAchievementsCallback {
        void onCallback(List<UnlockedAchievement> unlockedAchievements, Exception e);
    }

    public interface FirestoreOperationCallback {
        void onCallback(boolean success, Exception e);
    }

    // Dodajmy też callback dla pobierania UserProfile, jeśli go nie masz
    public interface UserProfileCallback {
        void onCallback(UserProfile userProfile, Exception e);
    }


    /**
     * Zwraca instancję FirebaseFirestore
     */
    public FirebaseFirestore getFirestore() {
        return db;
    }

    /**
     * Zwraca instancję FirebaseAuth
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    // === METODY DLA UŻYTKOWNIKÓW (jeśli nie masz podobnej, dodaj lub dostosuj) ===
    public void getUserProfile(String userId, UserProfileCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onCallback(null, new IllegalArgumentException("User ID cannot be null or empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (callback != null) callback.onCallback(profile, null);
                    } else {
                        if (callback != null) callback.onCallback(null, null); // Użytkownik nie znaleziony, ale nie błąd
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(null, e);
                });
    }

    public void createUserProfile(UserProfile userProfile, FirestoreOperationCallback callback) {
        if (userProfile == null || userProfile.getUserId() == null || userProfile.getUserId().isEmpty()) {
            if (callback != null) callback.onCallback(false, new IllegalArgumentException("User profile or User ID cannot be null or empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userProfile.getUserId())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false, e);
                });
    }


    // === METODY DLA OSIĄGNIĘĆ ===

    /**
     * Pobiera wszystkie definicje osiągnięć z kolekcji 'achievements'.
     */
    public void getAllAchievementDefinitions(AchievementsDefinitionsCallback callback) {
        achievementsCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Achievement> achievementList = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    Achievement achievement = document.toObject(Achievement.class);
                    if (achievement != null) {
                        achievement.setId(document.getId());
                        achievementList.add(achievement);
                    }
                }
                if (callback != null) callback.onCallback(achievementList, null);
            } else {
                if (callback != null) callback.onCallback(null, task.getException());
            }
        });
    }

    /**
     * Pobiera listę odblokowanych osiągnięć dla danego użytkownika.
     */
    public void getUserUnlockedAchievements(String userId, UserUnlockedAchievementsCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onCallback(null, new IllegalArgumentException("User ID cannot be null or empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId).collection("unlockedAchievements")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<UnlockedAchievement> unlockedList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            UnlockedAchievement unlocked = document.toObject(UnlockedAchievement.class);
                            if (unlocked != null) {
                                // Zakładamy, że ID dokumentu jest ID osiągnięcia, a UnlockedAchievement.achievementId jest ustawiane przez Firestore
                                unlockedList.add(unlocked);
                            }
                        }
                        if (callback != null) callback.onCallback(unlockedList, null);
                    } else {
                        if (callback != null) callback.onCallback(null, task.getException());
                    }
                });
    }

    /**
     * Zapisuje informację o odblokowaniu osiągnięcia dla użytkownika.
     */
    public void markAchievementAsUnlocked(String userId, String achievementId, FirestoreOperationCallback callback) {
        if (userId == null || userId.isEmpty() || achievementId == null || achievementId.isEmpty()) {
            if (callback != null) callback.onCallback(false, new IllegalArgumentException("User ID or Achievement ID cannot be null or empty."));
            return;
        }
        UnlockedAchievement unlockedData = new UnlockedAchievement(achievementId);

        db.collection(USERS_COLLECTION).document(userId).collection("unlockedAchievements")
                .document(achievementId)
                .set(unlockedData)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false, e);
                });
    }

    // === METODY DO AKTUALIZACJI POSTĘPU UŻYTKOWNIKA DLA OSIĄGNIĘĆ ===

    /**
     * Inkrementuje licznik ukończonych lekcji dla użytkownika.
     */
    public void incrementUserLessonsCompleted(String userId, FirestoreOperationCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onCallback(false, new IllegalArgumentException("User ID cannot be null or empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId)
                .update("lessonsCompletedCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false, e);
                });
    }

    /**
     * Dodaje ID języka do listy języków, których naukę rozpoczął użytkownik.
     * Używa FieldValue.arrayUnion, aby uniknąć duplikatów.
     */
    public void addUserStartedLanguage(String userId, String languageId, FirestoreOperationCallback callback) {
        if (userId == null || userId.isEmpty() || languageId == null || languageId.isEmpty()) {
            if (callback != null) callback.onCallback(false, new IllegalArgumentException("User ID or Language ID cannot be null or empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId)
                .update("languagesStartedIds", FieldValue.arrayUnion(languageId))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false, e);
                });
    }


    // Twoje istniejące metody - zostawiam je bez zmian, chyba że chcesz coś dostosować
    // (getDocument, setDocument, updateField, updateFields, queryDocuments)

    /**
     * Pobiera dokument z Firestore
     */
    public void getDocument(String collection, String documentId,
                            OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(collection)
                .document(documentId)
                .get()
                .addOnCompleteListener(listener);
    }

    /**
     * Pobiera dokument z Firestore z callbackami sukces/błąd
     */
    public void getDocument(String collection, String documentId,
                            OnSuccessListener<DocumentSnapshot> successListener,
                            OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Zapisuje dokument w Firestore
     */
    public void setDocument(String collection, String documentId, Object data) {
        db.collection(collection)
                .document(documentId)
                .set(data);
    }

    /**
     * Zapisuje dokument w Firestore z callbackami sukces/błąd
     */
    public void setDocument(String collection, String documentId, Object data,
                            OnSuccessListener<Void> successListener,
                            OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Aktualizuje pole w dokumencie
     */
    public void updateField(String collection, String documentId,
                            String field, Object value, boolean increment) {
        DocumentReference docRef = db.collection(collection).document(documentId);

        if (increment && value instanceof Number) {
            docRef.update(field, FieldValue.increment(((Number)value).longValue()));
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            docRef.update(updates);
        }
    }

    /**
     * Aktualizuje pole w dokumencie z callbackami sukces/błąd
     */
    public void updateField(String collection, String documentId,
                            String field, Object value, boolean increment,
                            OnSuccessListener<Void> successListener,
                            OnFailureListener failureListener) {
        DocumentReference docRef = db.collection(collection).document(documentId);

        if (increment && value instanceof Number) {
            docRef.update(field, FieldValue.increment(((Number)value).longValue()))
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            docRef.update(updates)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }
    }

    /**
     * Aktualizuje wiele pól w dokumencie
     */
    public void updateFields(String collection, String documentId, Map<String, Object> updates,
                             OnSuccessListener<Void> successListener,
                             OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Wykonuje zapytanie do kolekcji
     */
    public void queryDocuments(String collection,
                               Function<Query, Query> queryBuilder,
                               OnCompleteListener<QuerySnapshot> listener) {
        Query baseQuery = db.collection(collection);
        Query builtQuery = queryBuilder.apply(baseQuery);
        builtQuery.get().addOnCompleteListener(listener);
    }
}