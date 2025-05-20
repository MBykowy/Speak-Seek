package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.a404.data.model.Course;
import com.example.a404.data.model.Word; // Common import from mine and theirs
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private SQLiteDatabase database;
    private WordDbHelper dbHelper;
    private static final String TAG = "CourseDao";

    public CourseDao(WordDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        // Otwieranie bazy danych w konstruktorze lub dedykowanej metodzie open()
        // Należy pamiętać o jej zamknięciu, gdy nie jest już potrzebna.
        try {
            this.database = dbHelper.getWritableDatabase(); // lub getReadableDatabase() jeśli tylko odczyt
        } catch (Exception e) {
            Log.e(TAG, "Error opening database", e);
            // Rozważ rzucenie wyjątku lub obsługę błędu w inny sposób
        }
    }

    // Metoda do otwierania bazy danych, jeśli nie jest jeszcze otwarta
    public void open() {
        if (database == null || !database.isOpen()) {
            try {
                database = dbHelper.getWritableDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Error opening writable database in open()", e);
                try {
                    database = dbHelper.getReadableDatabase();
                } catch (Exception ex) {
                    Log.e(TAG, "Error opening readable database in open()", ex);
                }
            }
        }
    }

    // Method from 'mine'
    public List<Word> getWordsForCourse(long courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Uses local db instance
        List<Word> words = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    WordDbHelper.TABLE_WORDS,
                    null, // Query all columns
                    WordDbHelper.COLUMN_WORD_COURSE_ID + " = ?",
                    new String[]{String.valueOf(courseId)},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Word word = new Word();
                    word.setId(cursor.getLong(cursor.getColumnIndexOrThrow(WordDbHelper.COLUMN_ID)));
                    word.setText(cursor.getString(cursor.getColumnIndexOrThrow(WordDbHelper.COLUMN_WORD_TEXT)));
                    word.setTranslation(cursor.getString(cursor.getColumnIndexOrThrow(WordDbHelper.COLUMN_WORD_TRANSLATION)));
                    word.setCourseId(cursor.getLong(cursor.getColumnIndexOrThrow(WordDbHelper.COLUMN_WORD_COURSE_ID)));

                    // Sprawdź, czy kolumny kategorii i dystraktorów istnieją
                    int categoryIndex = cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_CATEGORY);
                    if (categoryIndex != -1 && !cursor.isNull(categoryIndex)) {
                        word.setCategory(cursor.getString(categoryIndex));
                    }

                    int distractorsIndex = cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_DISTRACTORS);
                    if (distractorsIndex != -1 && !cursor.isNull(distractorsIndex)) {
                        word.setPredefinedDistractorsString(cursor.getString(distractorsIndex));
                    }

                    words.add(word);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting words for course: " + courseId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Note: Not closing 'db' here as it's obtained from dbHelper.getReadableDatabase()
            // and dbHelper manages its lifecycle. Closing it might affect other operations
            // if dbHelper returns a shared instance that it expects to remain open.
            // Standard practice is to close cursors, but the db object obtained via getReadable/WritableDatabase
            // is typically closed when the helper itself is closed.
        }
        return words;
    }

    // Method from 'theirs'
    public int getWordCountForCourse(long courseId) {
        // This method now uses the getWordsForCourse method from 'mine' for consistency
        // or could directly use WordDao if preferred, but using local capability is often cleaner.
        // For this merge, let's use the newly added getWordsForCourse to avoid WordDao dependency here
        // if the goal is to make CourseDao more self-contained for word retrieval related to a course.
        // Alternatively, if WordDao is the designated class for all Word operations, then using it is fine.
        // Given 'theirs' used WordDao, let's keep that pattern for this method if it was intentional.

        // Option 1: Using WordDao (as in 'theirs')
        if (database == null || !database.isOpen()) {
            open();
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is not open in getWordCountForCourse.");
                return 0;
            }
        }
        WordDao wordDao = new WordDao(dbHelper); // Assuming WordDao is available and set up
        List<Word> words = wordDao.getWordsByCourseId(courseId); // Relies on WordDao
        wordDao.close(); // Important to close the DAO if it opens its own resources
        return words != null ? words.size() : 0;

        // Option 2: Using the getWordsForCourse from 'mine' (if we prefer to keep logic within CourseDao)
        // List<Word> words = getWordsForCourse(courseId);
        // return words != null ? words.size() : 0;
    }

    // Method from 'theirs'
    public boolean addWordToCourse(long courseId, Word word) {
        if (database == null || !database.isOpen()) {
            open();
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is not open for writing in addWordToCourse.");
                return false;
            }
        }
        if (database.isReadOnly()) {
            Log.e(TAG, "Database is read-only. Cannot add word to course.");
            return false;
        }

        // Using WordDao to insert the word, as per 'theirs'
        WordDao wordDao = new WordDao(dbHelper);
        word.setCourseId(courseId); // Ensure word is linked to the correct course
        long result = wordDao.insertWord(word); // insertWord should handle its own db transaction or use the shared one
        wordDao.close(); // Close WordDao if it was opened/managed resources
        return result != -1;
    }

    public long insertCourse(Course course) {
        if (database == null || !database.isOpen()) {
            open(); // Spróbuj otworzyć, jeśli zamknięta
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is not open for writing in insertCourse.");
                return -1;
            }
        }
        if (database.isReadOnly()) {
            Log.e(TAG, "Database is read-only. Cannot insert course.");
            return -1;
        }

        ContentValues values = new ContentValues();
        // Użyj COLUMN_ID z WordDbHelper, jeśli ID jest zarządzane przez JSON
        // Jeśli ID jest AUTOINCREMENT, nie wstawiaj go tutaj.
        // Zakładając, że ID z JSON jest używane (jak w WordDbHelper.insertSampleDataFromJson)
        if (course.getId() > 0) { // Lub inna logika sprawdzająca, czy ID powinno być wstawione
            values.put(WordDbHelper.COLUMN_ID, course.getId());
        }
        values.put(WordDbHelper.COLUMN_COURSE_NAME, course.getName());
        values.put(WordDbHelper.COLUMN_COURSE_DESCRIPTION, course.getDescription());
        values.put(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE, course.getLanguageCode());
        return database.insertWithOnConflict(WordDbHelper.TABLE_COURSES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @SuppressLint("Range")
    public List<Course> getAllCourses(String languageCode) { // Dodano parametr languageCode
        List<Course> courses = new ArrayList<>();
        if (database == null || !database.isOpen()) {
            open();
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is not open for reading in getAllCourses.");
                return courses; // Zwróć pustą listę
            }
        }

        Cursor cursor = null;
        String selection = null;
        String[] selectionArgs = null;

        if (languageCode != null && !languageCode.trim().isEmpty() && !languageCode.equalsIgnoreCase("all")) {
            selection = WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE + " = ?";
            selectionArgs = new String[]{languageCode};
            Log.d(TAG, "Fetching courses for language: " + languageCode);
        } else {
            Log.d(TAG, "Fetching all courses (languageCode is null, empty, or 'all').");
        }

        try {
            cursor = database.query(
                    WordDbHelper.TABLE_COURSES,
                    new String[]{WordDbHelper.COLUMN_ID, WordDbHelper.COLUMN_COURSE_NAME, WordDbHelper.COLUMN_COURSE_DESCRIPTION, WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE},
                    selection, // Zastosuj selekcję
                    selectionArgs, // Argumenty selekcji
                    null, null, WordDbHelper.COLUMN_COURSE_NAME + " ASC"); // Sortuj alfabetycznie wg nazwy

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Course course = new Course();
                    course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                    course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
                    course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));
                    course.setLanguageCode(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE)));
                    // Uwaga: Ta metoda nie ładuje słów do kursu. Słowa są ładowane osobno.
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying all courses with language filter", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "Found " + courses.size() + " courses for language code: " + (languageCode != null ? languageCode : "all"));
        return courses;
    }

    @SuppressLint("Range")
    public Course getCourseById(long courseId) {
        if (database == null || !database.isOpen()) {
            open();
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is not open for reading in getCourseById.");
                return null;
            }
        }
        Course course = null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    WordDbHelper.TABLE_COURSES,
                    new String[]{WordDbHelper.COLUMN_ID, WordDbHelper.COLUMN_COURSE_NAME, WordDbHelper.COLUMN_COURSE_DESCRIPTION, WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE},
                    WordDbHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(courseId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                course = new Course();
                course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
                course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));
                course.setLanguageCode(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE)));

                // Załaduj słowa dla tego kursu
                // To jest miejsce, gdzie można by zintegrować ładowanie słów,
                // ale WordGameActivity robi to już przez WordDao, co jest OK.
                // Jeśli chcesz, aby CourseDao zawsze zwracał kurs ze słowami, musiałbyś dodać tu logikę.
                // List<Word> words = getWordsForCourse(courseId); // Using the new method
                // course.setWords(words);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying course by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return course;
    }

    public void close() {
        if (database != null && database.isOpen()) {
            // dbHelper.close(); // This was in original, implies WordDbHelper manages the actual closing.
            // If CourseDao 'owns' the database instance it opened, it should close it.
            // However, SQLiteOpenHelper instances are usually shared and closed by the last user or context.
            // Calling dbHelper.close() closes the underlying database for all users of that helper instance.
            // If this DAO is the sole manager or its lifecycle aligns with the app's, then dbHelper.close() is correct.
            // For safety, let's stick to the original closing logic.
            dbHelper.close();
            database = null;  // Ustaw na null, aby uniknąć użycia zamkniętej bazy
            Log.d(TAG, "Database closed in CourseDao via dbHelper.close()");
        } else {
            Log.d(TAG, "Database already closed or null in CourseDao.close()");
        }
    }
}