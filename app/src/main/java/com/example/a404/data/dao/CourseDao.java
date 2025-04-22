package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.a404.data.model.Course;
import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private SQLiteDatabase database;
    private WordDbHelper dbHelper;
    private WordDao wordDao;

    public CourseDao(WordDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.database = dbHelper.getWritableDatabase();
        this.wordDao = new WordDao(dbHelper);
    }

    public long insertCourse(Course course) {
        ContentValues values = new ContentValues();
        values.put(WordDbHelper.COLUMN_COURSE_NAME, course.getName());
        values.put(WordDbHelper.COLUMN_COURSE_DESCRIPTION, course.getDescription());

        return database.insert(WordDbHelper.TABLE_COURSES, null, values);
    }

    @SuppressLint("Range")
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();

        Cursor cursor = database.query(
                WordDbHelper.TABLE_COURSES,
                new String[] {
                        WordDbHelper.COLUMN_ID,
                        WordDbHelper.COLUMN_COURSE_NAME,
                        WordDbHelper.COLUMN_COURSE_DESCRIPTION
                },
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
                course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));

                // Get words for this course
                List<Word> words = wordDao.getWordsByCourseId(course.getId());
                course.setWords(words);

                courses.add(course);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return courses;
    }

    @SuppressLint("Range")
    public Course getCourseById(long id) {
        Course course = null;

        Cursor cursor = database.query(
                WordDbHelper.TABLE_COURSES,
                new String[] {
                        WordDbHelper.COLUMN_ID,
                        WordDbHelper.COLUMN_COURSE_NAME,
                        WordDbHelper.COLUMN_COURSE_DESCRIPTION
                },
                WordDbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            course = new Course();
            course.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
            course.setName(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_NAME)));
            course.setDescription(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_COURSE_DESCRIPTION)));

            // Get words for this course
            List<Word> words = wordDao.getWordsByCourseId(course.getId());
            course.setWords(words);

            cursor.close();
        }

        return course;
    }
}