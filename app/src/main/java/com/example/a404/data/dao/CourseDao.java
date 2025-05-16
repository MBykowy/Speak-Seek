package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.a404.data.model.Course;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private static final String TAG = "CourseDao";
    private SQLiteDatabase database;
    private WordDbHelper dbHelper;
    private WordDao wordDao;

    public CourseDao(WordDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.database = dbHelper.getWritableDatabase();
        this.wordDao = new WordDao(dbHelper);
    }

    // Zaktualizuj insertCourse, aby zapisywał kod języka
    public long insertCourse(Course course) {
        ContentValues values = new ContentValues();
        values.put(WordDbHelper.COLUMN_COURSE_NAME, course.getName());
        values.put(WordDbHelper.COLUMN_COURSE_DESCRIPTION, course.getDescription());
        // Dodaj zapis language_code
        values.put(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE, course.getLanguageCode());

        Log.d(TAG, "Wstawianie kursu: " + course.getName() + " z kodem języka: " + course.getLanguageCode());
        return database.insert(WordDbHelper.TABLE_COURSES, null, values);
    }

    // Zaktualizuj getAllCourses, aby filtrowało i odczytywało kod języka
    @SuppressLint("Range")
    public List<Course> getAllCourses(String languageCode) {
        Log.d(TAG, "Pobieranie kursów dla języka: " + languageCode);
        List<Course> courses = new ArrayList<>();

        // Zdefiniuj kolumny do pobrania, w tym nową kolumnę
        String[] projection = {
                WordDbHelper.COLUMN_ID,
                WordDbHelper.COLUMN_COURSE_NAME,
                WordDbHelper.COLUMN_COURSE_DESCRIPTION,
                WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE // Dodaj nową kolumnę
        };

        // Zdefiniuj warunek WHERE do filtrowania po kodzie języka
        String selection = WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE + " = ?";
        String[] selectionArgs = { languageCode };

        Cursor cursor = database.query(
                WordDbHelper.TABLE_COURSES,
                projection,
                selection,       // Użyj warunku WHERE
                selectionArgs,   // Użyj argumentów dla WHERE
                null, null, WordDbHelper.COLUMN_COURSE_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
                course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));
                // Odczytaj kod języka z kursora
                course.setLanguageCode(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE)));

                List<Word> words = wordDao.getWordsByCourseId(course.getId());
                course.setWords(words);

                courses.add(course);
            } while (cursor.moveToNext());

            cursor.close();
            Log.d(TAG, "Znaleziono " + courses.size() + " kursów dla języka: " + languageCode);
        } else {
            Log.d(TAG, "Nie znaleziono kursów dla języka: " + languageCode);
        }
        return courses;
    }

    // Zaktualizuj getCourseById, aby odczytywało kod języka
    @SuppressLint("Range")
    public Course getCourseById(long id) {
        Course course = null;
        Log.d(TAG, "Pobieranie kursu o ID: " + id);

        // Zdefiniuj kolumny do pobrania, w tym nową kolumnę
        String[] projection = {
                WordDbHelper.COLUMN_ID,
                WordDbHelper.COLUMN_COURSE_NAME,
                WordDbHelper.COLUMN_COURSE_DESCRIPTION,
                WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE // Dodaj nową kolumnę
        };

        Cursor cursor = database.query(
                WordDbHelper.TABLE_COURSES,
                projection,
                WordDbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            course = new Course();
            course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
            course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
            course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));
            // Odczytaj kod języka z kursora
            course.setLanguageCode(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_LANGUAGE_CODE)));

            List<Word> words = wordDao.getWordsByCourseId(course.getId());
            course.setWords(words);
            Log.d(TAG, "Znaleziono kurs: " + course.getName() + " (" + course.getLanguageCode() + "), liczba słów: " + words.size());

            cursor.close();
        } else {
            Log.d(TAG, "Nie znaleziono kursu o ID: " + id);
        }

        return course;
    }
}