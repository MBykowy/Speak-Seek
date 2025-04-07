package com.example.a404.data.source;

import com.google.android.gms.tasks.OnCompleteListener;
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
    private final FirebaseFirestore db;

    public FirebaseSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getDocument(String collection, String documentId,
                            OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(collection)
                .document(documentId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void setDocument(String collection, String documentId, Object data) {
        db.collection(collection)
                .document(documentId)
                .set(data);
    }

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

    public void queryDocuments(String collection,
                               Function<Query, Query> queryBuilder,
                               OnCompleteListener<QuerySnapshot> listener) {
        Query baseQuery = db.collection(collection);
        Query builtQuery = queryBuilder.apply(baseQuery);
        builtQuery.get().addOnCompleteListener(listener);
    }
}