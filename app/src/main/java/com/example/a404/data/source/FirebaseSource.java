package com.example.a404.data.source;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FirebaseSource {
    private static final String TAG = "FirebaseSource";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        Log.d(TAG, "FirebaseSource zainicjalizowany");

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