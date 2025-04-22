package com.example.a404.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.a404.data.model.Word;
import com.example.a404.data.model.WordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class WordDao {
    private SQLiteDatabase database;
    private WordDbHelper dbHelper;

    public WordDao(WordDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.database = dbHelper.getWritableDatabase();
    }

    public long insertWord(Word word) {
        ContentValues values = new ContentValues();
        values.put(WordDbHelper.COLUMN_WORD_TEXT, word.getText());
        values.put(WordDbHelper.COLUMN_WORD_TRANSLATION, word.getTranslation());
        values.put(WordDbHelper.COLUMN_WORD_COURSE_ID, word.getCourseId());

        return database.insert(WordDbHelper.TABLE_WORDS, null, values);
    }

    @SuppressLint("Range")
    public List<Word> getWordsByCourseId(long courseId) {
        List<Word> words = new ArrayList<>();

        Cursor cursor = database.query(
                WordDbHelper.TABLE_WORDS,
                new String[] {
                        WordDbHelper.COLUMN_ID,
                        WordDbHelper.COLUMN_WORD_TEXT,
                        WordDbHelper.COLUMN_WORD_TRANSLATION,
                        WordDbHelper.COLUMN_WORD_COURSE_ID
                },
                WordDbHelper.COLUMN_WORD_COURSE_ID + " = ?",
                new String[] { String.valueOf(courseId) },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Word word = new Word();
                word.setId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_ID)));
                word.setText(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_TEXT)));
                word.setTranslation(cursor.getString(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_TRANSLATION)));
                word.setCourseId(cursor.getLong(cursor.getColumnIndex(WordDbHelper.COLUMN_WORD_COURSE_ID)));

                words.add(word);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return words;
    }
}