package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class WordDao {
    private SQLiteDatabase database;
    private WordDbHelper dbHelper;
    private static final String TAG = "WordDao";


    public WordDao(WordDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        // Otwórz bazę danych do zapisu tylko wtedy, gdy jest to potrzebne,
        // lub upewnij się, że jest poprawnie zarządzana (otwierana/zamykana).
        // Dla uproszczenia, otwieramy ją tutaj.
        // Rozważ zarządzanie cyklem życia bazy danych w kontekście aplikacji.
        try {
            this.database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Error opening writable database", e);
            // Spróbuj otworzyć do odczytu jako fallback, jeśli zapis się nie udał
            // lub jeśli operacje zapisu nie są natychmiast potrzebne.
            try {
                this.database = dbHelper.getReadableDatabase();
            } catch (Exception ex) {
                Log.e(TAG, "Error opening readable database", ex);
            }
        }
    }


    public long insertWord(Word word) {
        if (database == null || !database.isOpen()) {
            Log.e(TAG, "Database is not open for writing.");
            // Spróbuj ponownie otworzyć bazę danych, jeśli jest zamknięta
            try {
                database = dbHelper.getWritableDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Failed to reopen writable database in insertWord", e);
                return -1; // Wskazuje błąd
            }
        }
        if (!database.isReadOnly()) {
            ContentValues values = new ContentValues();
            values.put(WordDbHelper.COLUMN_WORD_TEXT, word.getText());
            values.put(WordDbHelper.COLUMN_WORD_TRANSLATION, word.getTranslation());
            values.put(WordDbHelper.COLUMN_WORD_COURSE_ID, word.getCourseId());
            values.put(WordDbHelper.COLUMN_WORD_CATEGORY, word.getCategory());
            values.put(WordDbHelper.COLUMN_WORD_DISTRACTORS, word.getPredefinedDistractorsString());

            return database.insert(WordDbHelper.TABLE_WORDS, null, values);
        } else {
            Log.e(TAG, "Database is read-only. Cannot insert word.");
            return -1; // Wskazuje błąd
        }
    }

    @SuppressLint("Range")
    public List<Word> getWordsByCourseId(long courseId) {
        List<Word> words = new ArrayList<>();
        if (database == null || !database.isOpen()) {
            Log.e(TAG, "Database is not open for reading.");
            try {
                database = dbHelper.getReadableDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Failed to reopen readable database in getWordsByCourseId", e);
                return words; // Zwróć pustą listę w przypadku błędu
            }
        }

        Cursor cursor = null;
        try {
            cursor = database.query(
                    WordDbHelper.TABLE_WORDS,
                    new String[]{
                            WordDbHelper.COLUMN_ID,
                            WordDbHelper.COLUMN_WORD_TEXT,
                            WordDbHelper.COLUMN_WORD_TRANSLATION,
                            WordDbHelper.COLUMN_WORD_COURSE_ID,
                            WordDbHelper.COLUMN_WORD_CATEGORY,
                            WordDbHelper.COLUMN_WORD_DISTRACTORS
                    },
                    WordDbHelper.COLUMN_WORD_COURSE_ID + " = ?",
                    new String[]{String.valueOf(courseId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Word word = new Word();
                    word.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                    word.setText(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_TEXT)));
                    word.setTranslation(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_TRANSLATION)));
                    word.setCourseId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_COURSE_ID)));
                    word.setCategory(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_CATEGORY)));
                    word.setPredefinedDistractorsString(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_DISTRACTORS)));

                    words.add(word);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying words by course ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return words;
    }

    // Rozważ dodanie metody close() do zamykania bazy danych, gdy DAO nie jest już potrzebne,
    // np. w onDestroy() aktywności/fragmentu lub przez ViewModel.
    public void close() {
        if (database != null && database.isOpen()) {
            dbHelper.close(); // To zamknie bazę danych
            database = null; // Ustaw na null, aby uniknąć użycia zamkniętej bazy
            Log.d(TAG, "Database closed in WordDao");
        }
    }
}