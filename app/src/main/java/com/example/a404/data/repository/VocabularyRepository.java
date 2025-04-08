package com.example.a404.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a404.data.model.VocabularyItem;
import com.example.a404.data.source.FirebaseSource;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class VocabularyRepository {
    private final FirebaseSource firebaseSource;
    private final Map<String, String> localCache = new HashMap<>();

    public VocabularyRepository(FirebaseSource firebaseSource) {
        this.firebaseSource = firebaseSource;
    }

    public LiveData<String> getTranslation(String objectLabel, String languageCode) {
        MutableLiveData<String> translationLiveData = new MutableLiveData<>();

        // Klucz cache dla tego tłumaczenia
        String cacheKey = objectLabel + "_" + languageCode;

        // Sprawdź czy mamy już w cache
        if (localCache.containsKey(cacheKey)) {
            translationLiveData.setValue(localCache.get(cacheKey));
            return translationLiveData;
        }

        // Pobierz z Firestore jeśli nie ma w cache
        firebaseSource.queryDocuments(
                "vocabulary",
                query -> query.whereEqualTo("objectLabel", objectLabel)
                        .whereEqualTo("languageCode", languageCode),
                task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            VocabularyItem item = document.toObject(VocabularyItem.class);
                            String translation = item.getTranslation();
                            localCache.put(cacheKey, translation);
                            translationLiveData.setValue(translation);
                            return;
                        }
                    }
                    // Brak tłumaczenia
                    translationLiveData.setValue(objectLabel);
                });

        return translationLiveData;
    }

    public void loadInitialVocabulary() {
        // Dodaj podstawowe słownictwo jeśli potrzebne
        // Przykładowa implementacja:
        addVocabularyItem(new VocabularyItem("apple", "pl", "jabłko"));
        addVocabularyItem(new VocabularyItem("book", "pl", "książka"));
        addVocabularyItem(new VocabularyItem("car", "pl", "samochód"));
        // itd.
    }

    private void addVocabularyItem(VocabularyItem item) {
        String documentId = item.getObjectLabel() + "_" + item.getLanguageCode();
        firebaseSource.setDocument("vocabulary", documentId, item);
    }
}