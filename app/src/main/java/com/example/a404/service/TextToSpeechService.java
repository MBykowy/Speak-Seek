package com.example.a404.service;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

public class TextToSpeechService {
    private static final String TAG = "TextToSpeechService";

    private TextToSpeech textToSpeech = null;
    private final MutableLiveData<Boolean> isInitialized = new MutableLiveData<>(false);
    private String currentLanguageCode = "en";

    public TextToSpeechService(Context context) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(new Locale(currentLanguageCode));
                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                        langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Język nie jest obsługiwany: " + currentLanguageCode);
                    isInitialized.postValue(false);
                } else {
                    isInitialized.postValue(true);
                    Log.d(TAG, "TTS zainicjalizowany pomyślnie");
                }
            } else {
                Log.e(TAG, "Inicjalizacja TTS nie powiodła się");
                isInitialized.postValue(false);
            }
        });
    }

    public LiveData<Boolean> isInitialized() {
        return isInitialized;
    }

    public void speak(String text) {
        if (textToSpeech != null && isInitialized.getValue() != null && isInitialized.getValue()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Log.e(TAG, "Próba użycia TTS przed inicjalizacją");
        }
    }

    public boolean setLanguage(String languageCode) {
        if (textToSpeech == null || isInitialized.getValue() == null || !isInitialized.getValue()) {
            currentLanguageCode = languageCode;
            return false;
        }

        try {
            Locale locale = new Locale(languageCode);
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Język nie jest obsługiwany: " + languageCode);
                return false;
            }
            currentLanguageCode = languageCode;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Błąd podczas ustawiania języka: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isInitialized.setValue(false);
        }
    }
}