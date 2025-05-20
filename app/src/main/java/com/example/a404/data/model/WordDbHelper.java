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

public class WordDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "WordDbHelper";
    private static final String DATABASE_NAME = "wordlearning.db";
    // Wersja bazy danych może pozostać taka sama, jeśli tylko zmieniasz sposób wstawiania danych
    // ale jeśli poprzednio były dane, a teraz chcesz je zastąpić z JSON,
    // i schemat się nie zmienił, możesz zostawić lub podnieść dla pewności.
    // Jeśli schemat się zmienił (jak w poprzednim kroku), wersja MUSI być podniesiona.
    private static final int DATABASE_VERSION = 5; // Utrzymujemy wersję 4

    // Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_WORDS = "words";

    // Common column names
    public static final String COLUMN_ID = "id"; // Używane jako _id dla CursorAdapter, ale tutaj jako klucz główny

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

    // CREATE TABLE statements
    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
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
        Log.d(TAG, "Database tables created. Inserting sample data from JSON.");
        insertSampleDataFromJson(db);
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
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion + ". Old data will be lost.");
        onUpgrade(db, oldVersion, newVersion); // Traktuj downgrade jak upgrade
    }

    private void insertSampleDataFromJson(SQLiteDatabase db) {
        try {
            InputStream inputStream = context.getAssets().open("sample_data.json");
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
                    long courseIdFromJson = courseObject.getLong("id"); // Użyj ID z JSON
                    String courseName = courseObject.getString("name");
                    String courseDescription = courseObject.getString("description");
                    String courseLangCode = courseObject.getString("language_code");

                    ContentValues courseValues = new ContentValues();
                    // Wstawiamy z konkretnym ID z JSON, jeśli chcemy zachować spójność
                    // Jeśli ID w tabeli jest AUTOINCREMENT i chcemy, aby baza sama nadała ID,
                    // to nie wstawiamy COLUMN_ID. Jednak dla spójności z JSON lepiej wstawić.
                    // Aby to działało, tabela nie powinna mieć AUTOINCREMENT dla ID,
                    // lub musimy zarządzać tym inaczej. Dla uproszczenia, zakładam, że ID z JSON jest używane.
                    // Jeśli COLUMN_ID jest AUTOINCREMENT, to nie podajemy go tutaj, a courseIdFromJson
                    // będzie używane tylko do powiązania słów.
                    // Zmieniam CREATE_COURSES_TABLE, aby ID nie było AUTOINCREMENT, jeśli chcemy używać ID z JSON.
                    // LUB: pozwalamy bazie nadać ID i mapujemy courseIdFromJson na rzeczywiste ID z bazy.
                    // Dla uproszczenia, pozwólmy bazie nadać ID i użyjmy zwróconego ID.

                    courseValues.put(COLUMN_COURSE_NAME, courseName);
                    courseValues.put(COLUMN_COURSE_DESCRIPTION, courseDescription);
                    courseValues.put(COLUMN_COURSE_LANGUAGE_CODE, courseLangCode);
                    // Nie wstawiamy ID, jeśli jest AUTOINCREMENT. SQLite sam je nada.
                    // long actualCourseId = db.insert(TABLE_COURSES, null, courseValues);
                    // Jeśli chcemy użyć ID z JSON, tabela musi być odpowiednio zdefiniowana.
                    // Dla tego przykładu, zakładam, że ID w JSON jest tylko dla referencji,
                    // a baza danych sama generuje ID.
                    // Poprawka: Aby zachować ID z JSON, musimy je wstawić.
                    // Zmieniam definicję tabeli, aby ID było PRIMARY KEY, ale niekoniecznie AUTOINCREMENT
                    // jeśli chcemy kontrolować ID.
                    // W tym przypadku, jeśli ID jest AUTOINCREMENT, to `courseIdFromJson` jest tylko do referencji
                    // dla słów. Zmodyfikujmy tak, aby używać ID z JSON jako klucza głównego.
                    // W tym celu CREATE_COURSES_TABLE powinno mieć `COLUMN_ID + " INTEGER PRIMARY KEY,"`
                    // a nie AUTOINCREMENT.
                    // Aktualna definicja ma AUTOINCREMENT, więc będziemy używać zwróconego ID.

                    // Wstaw kurs i pobierz jego rzeczywiste ID z bazy
                    ContentValues courseInsertValues = new ContentValues();
                    courseInsertValues.put(COLUMN_COURSE_NAME, courseName);
                    courseInsertValues.put(COLUMN_COURSE_DESCRIPTION, courseDescription);
                    courseInsertValues.put(COLUMN_COURSE_LANGUAGE_CODE, courseLangCode);
                    // Nie podajemy ID, jeśli jest AUTOINCREMENT. SQLite sam je nada.
                    // Jeśli chcemy użyć ID z JSON, musimy zmodyfikować schemat tabeli
                    // lub wstawiać ID bezpośrednio (co może być problematyczne z AUTOINCREMENT).

                    // Najprostsze podejście z AUTOINCREMENT:
                    // Wstawiamy kurs, dostajemy ID, a następnie używamy tego ID dla słów.
                    // To oznacza, że `courseIdFromJson` jest ignorowane jako klucz główny,
                    // ale może być użyte do logiki aplikacji, jeśli potrzebne.
                    // Dla tego przykładu, będziemy używać `courseIdFromJson` jako `course_id` dla słów,
                    // zakładając, że kursy są wstawiane w kolejności i ich ID będą odpowiadać.
                    // To jest kruche. Lepszym rozwiązaniem byłoby:
                    // 1. Usunąć AUTOINCREMENT z `COLUMN_ID` w `TABLE_COURSES` i wstawiać `courseIdFromJson`.
                    // 2. Lub, po wstawieniu kursu, pobrać jego `actualCourseId` i użyć go dla słów.

                    // Wybieram opcję 1 dla większej kontroli i spójności z JSON.
                    // Zmieniam CREATE_TABLE, aby COLUMN_ID nie było AUTOINCREMENT.
                    // Wymaga to zmiany w CREATE_COURSES_TABLE:
                    // `COLUMN_ID + " INTEGER PRIMARY KEY,"` zamiast `AUTOINCREMENT`
                    // Jeśli jednak chcemy zachować AUTOINCREMENT, to musimy pobrać ID po wstawieniu.
                    // Dla tego przykładu, zakładam, że ID z JSON jest używane jako klucz główny.
                    // Aby to zadziałało, zmień definicję tabeli `courses` (usunięcie AUTOINCREMENT).
                    // Jeśli nie chcesz zmieniać schematu, musisz pobrać ID po insercie.

                    // Zakładając, że ID w JSON jest docelowym ID w bazie:
                    courseValues.put(COLUMN_ID, courseIdFromJson); // Dodajemy ID z JSON
                    db.insertWithOnConflict(TABLE_COURSES, null, courseValues, SQLiteDatabase.CONFLICT_REPLACE);


                    if (courseObject.has("words")) {
                        JSONArray wordsArray = courseObject.getJSONArray("words");
                        for (int j = 0; j < wordsArray.length(); j++) {
                            JSONObject wordObject = wordsArray.getJSONObject(j);
                            ContentValues wordValues = new ContentValues();
                            wordValues.put(COLUMN_WORD_TEXT, wordObject.getString("text"));
                            wordValues.put(COLUMN_WORD_TRANSLATION, wordObject.getString("translation"));
                            wordValues.put(COLUMN_WORD_COURSE_ID, courseIdFromJson); // Użyj ID kursu z JSON
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
            Log.i(TAG, "Sample data inserted successfully from JSON.");

        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data from JSON", e);
        }
    }
}