package com.example.a404.data.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException; // Dodano import

public class WordDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "WordDbHelper";
    private static final String DATABASE_NAME = "wordlearning.db";
    // Podnieś wersję bazy danych, jeśli zmieniasz jej schemat lub sposób inicjalizacji danych
    private static final int DATABASE_VERSION = 7; // Podniesiono wersję

    // Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_WORDS = "words";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Course table columns
    public static final String COLUMN_COURSE_NAME = "name";
    public static final String COLUMN_COURSE_DESCRIPTION = "description";
    public static final String COLUMN_COURSE_LANGUAGE_CODE = "language_code";

    // Word table columns
    public static final String COLUMN_WORD_TEXT = "word_text";
    public static final String COLUMN_WORD_TRANSLATION = "translation";
    public static final String COLUMN_WORD_COURSE_ID = "course_id";
    public static final String COLUMN_WORD_CATEGORY = "category";
    public static final String COLUMN_WORD_DISTRACTORS = "distractors";

    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY," // Usunięto AUTOINCREMENT, aby używać ID z JSON
            + COLUMN_COURSE_NAME + " TEXT NOT NULL,"
            + COLUMN_COURSE_DESCRIPTION + " TEXT,"
            + COLUMN_COURSE_LANGUAGE_CODE + " TEXT"
            + ")";

    private static final String CREATE_WORDS_TABLE = "CREATE TABLE " + TABLE_WORDS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_WORD_TEXT + " TEXT NOT NULL,"
            + COLUMN_WORD_TRANSLATION + " TEXT NOT NULL,"
            + COLUMN_WORD_COURSE_ID + " INTEGER NOT NULL,"
            + COLUMN_WORD_CATEGORY + " TEXT,"
            + COLUMN_WORD_DISTRACTORS + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_WORD_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_ID + ")"
            + ")";

    private Context context;

    public WordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_WORDS_TABLE);
        Log.d(TAG, "Database tables created. Inserting sample data from JSON files.");
        // Ładuj dane z obu plików
        loadDataFromFile(db, "sample_data.json");
        loadDataFromFile(db, "sentence_data.json"); // Nowy plik
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ". Old data will be lost.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void loadDataFromFile(SQLiteDatabase db, String jsonFileName) {
        try {
            Log.d(TAG, "Attempting to load data from: " + jsonFileName);
            InputStream inputStream = context.getAssets().open(jsonFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            inputStream.close();

            JSONObject root = new JSONObject(jsonString.toString());
            JSONArray coursesArray = root.getJSONArray("courses");

            db.beginTransaction();
            try {
                for (int i = 0; i < coursesArray.length(); i++) {
                    JSONObject courseObject = coursesArray.getJSONObject(i);
                    long courseIdFromJson = courseObject.getLong("id");
                    String courseName = courseObject.getString("name");
                    String courseDescription = courseObject.getString("description");
                    String courseLangCode = courseObject.getString("language_code");

                    ContentValues courseValues = new ContentValues();
                    courseValues.put(COLUMN_ID, courseIdFromJson); // Użyj ID z JSON jako klucza głównego
                    courseValues.put(COLUMN_COURSE_NAME, courseName);
                    courseValues.put(COLUMN_COURSE_DESCRIPTION, courseDescription);
                    courseValues.put(COLUMN_COURSE_LANGUAGE_CODE, courseLangCode);
                    // Użyj CONFLICT_REPLACE, aby zaktualizować istniejące kursy lub wstawić nowe
                    db.insertWithOnConflict(TABLE_COURSES, null, courseValues, SQLiteDatabase.CONFLICT_REPLACE);

                    if (courseObject.has("words")) {
                        JSONArray wordsArray = courseObject.getJSONArray("words");
                        for (int j = 0; j < wordsArray.length(); j++) {
                            JSONObject wordObject = wordsArray.getJSONObject(j);
                            ContentValues wordValues = new ContentValues();
                            wordValues.put(COLUMN_WORD_TEXT, wordObject.getString("text"));
                            wordValues.put(COLUMN_WORD_TRANSLATION, wordObject.getString("translation"));
                            wordValues.put(COLUMN_WORD_COURSE_ID, courseIdFromJson); // Powiąż z ID kursu z JSON
                            wordValues.put(COLUMN_WORD_CATEGORY, wordObject.optString("category", null));
                            wordValues.put(COLUMN_WORD_DISTRACTORS, wordObject.optString("distractors", null));
                            db.insert(TABLE_WORDS, null, wordValues);
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            Log.i(TAG, "Sample data inserted successfully from " + jsonFileName);

        } catch (IOException e) {
            Log.e(TAG, "Error reading " + jsonFileName + " from assets", e);
        }
        catch (Exception e) {
            Log.e(TAG, "Error inserting sample data from " + jsonFileName, e);
        }
    }
}