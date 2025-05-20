package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.a404.data.model.Course;
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
                // com.example.a404.data.dao.WordDao wordDao = new com.example.a404.data.dao.WordDao(dbHelper);
                // course.setWords(wordDao.getWordsByCourseId(courseId));
                // wordDao.close(); // Pamiętaj o zamknięciu, jeśli otwierasz tutaj
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
            dbHelper.close(); // To zamknie bazę danych poprzez WordDbHelper
            database = null;  // Ustaw na null, aby uniknąć użycia zamkniętej bazy
            Log.d(TAG, "Database closed in CourseDao");
        }
    }
}