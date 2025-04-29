package com.example.a404.data.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Import dla Log

public class WordDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "WordDbHelper"; // Dodaj TAG
    private static final String DATABASE_NAME = "wordlearning.db";
    // Wersja bazy danych pozostaje 2, ponieważ schemat (kolumny) się nie zmienia
    private static final int DATABASE_VERSION = 3;

    // Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_WORDS = "words";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Course table columns
    public static final String COLUMN_COURSE_NAME = "name";
    public static final String COLUMN_COURSE_DESCRIPTION = "description";
    public static final String COLUMN_COURSE_LANGUAGE_CODE = "language_code"; // Kolumna kodu języka

    // Word table columns
    public static final String COLUMN_WORD_TEXT = "word_text";
    public static final String COLUMN_WORD_TRANSLATION = "translation";
    public static final String COLUMN_WORD_COURSE_ID = "course_id";

    // CREATE TABLE statements
    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COURSE_NAME + " TEXT NOT NULL, "
            + COLUMN_COURSE_DESCRIPTION + " TEXT, "
            + COLUMN_COURSE_LANGUAGE_CODE + " TEXT NOT NULL DEFAULT 'en');"; // Domyślnie 'en'

    private static final String CREATE_WORDS_TABLE = "CREATE TABLE " + TABLE_WORDS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WORD_TEXT + " TEXT NOT NULL, "
            + COLUMN_WORD_TRANSLATION + " TEXT NOT NULL, "
            + COLUMN_WORD_COURSE_ID + " INTEGER, "
            + "FOREIGN KEY(" + COLUMN_WORD_COURSE_ID + ") REFERENCES "
            + TABLE_COURSES + "(" + COLUMN_ID + "));";

    public WordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Tworzenie tabel bazy danych...");
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_WORDS_TABLE);
        Log.i(TAG, "Tabele utworzone, wstawianie przykładowych danych...");
        insertSampleData(db);
        Log.i(TAG, "Przykładowe dane wstawione.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Aktualizacja bazy danych z wersji " + oldVersion + " do " + newVersion);
        // Prosta strategia migracji: usuń stare tabele i utwórz nowe
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    // Updated sample data with German, Polish, Spanish, French
    private void insertSampleData(SQLiteDatabase db) {
        // === English Courses (IDs 1-5) ===
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Basic Vocabulary', 'Essential everyday English words for beginners', 'en')"); // ID 1
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Travel English', 'Useful words and phrases for travelers', 'en')");     // ID 2
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Business English', 'Professional vocabulary for workplace settings', 'en')"); // ID 3
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Academic English', 'Terms commonly used in educational contexts', 'en')");  // ID 4
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Idioms & Expressions', 'Common English idioms and their meanings', 'en')"); // ID 5

        // === German Course (ID 6) ===
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Grundwortschatz Deutsch', 'Wichtige deutsche Wörter für Anfänger', 'de')"); // ID 6

        // === Polish Course (ID 7) ===
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Podstawowe Słownictwo Polskie', 'Niezbędne polskie słowa na co dzień', 'pl')"); // ID 7

        // === Spanish Course (ID 8) ===
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Vocabulario Básico Español', 'Palabras esenciales en español para principiantes', 'es')"); // ID 8

        // === French Course (ID 9) ===
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ", " + COLUMN_COURSE_LANGUAGE_CODE + ") VALUES ('Vocabulaire de Base Français', 'Mots français essentiels pour débutants', 'fr')"); // ID 9


        // === English Words (IDs 1-5) ===
        // Basic Vocabulary words (Course ID: 1)
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hello', 'A greeting used when meeting someone', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Goodbye', 'A farewell used when leaving', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Thank you', 'Expression of gratitude', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Please', 'Used when requesting something politely', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Sorry', 'Used to express regret or apologize', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Friend', 'A person you like and enjoy spending time with', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Family', 'A group of related people', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Food', 'Things that people or animals eat', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Water', 'Clear liquid that falls as rain and forms rivers and seas', 1)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('House', 'A building where people live', 1)");

        // Travel English words (Course ID: 2)
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Airport', 'A place where aircraft take off and land', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hotel', 'A place providing accommodation and meals for travelers', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Passport', 'An official document that identifies you as a citizen', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Luggage', 'Bags and suitcases that travelers carry', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Ticket', 'A document showing you''ve paid for a journey or event', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Reservation', 'An arrangement to secure accommodation or a service', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Sightseeing', 'Visiting places of interest as a tourist', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Currency', 'Money used in a particular country', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Tourist', 'A person who travels for pleasure', 2)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Map', 'A diagrammatic representation of an area', 2)");

        // Business English words (Course ID: 3) - Keeping these as placeholders, add more if needed
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Meeting', 'An assembly of people for a particular purpose', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Deadline', 'A time or date by which something must be completed', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Contract', 'A written or spoken agreement', 3)");

        // Academic English words (Course ID: 4) - Keeping these as placeholders, add more if needed
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Thesis', 'A statement or theory put forward to be maintained or proved', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Research', 'Systematic investigation to establish facts', 4)");

        // Idioms & Expressions (Course ID: 5) - Keeping these as placeholders, add more if needed
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Break a leg', 'Good luck (often said to performers)', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Piece of cake', 'Something very easy to do', 5)");

        // === German Words (Course ID: 6) ===
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hallo', 'Hello', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Tschüss', 'Goodbye (informal)', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Danke', 'Thank you', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Bitte', 'Please / You''re welcome', 6)"); // Escaped quote
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Ja', 'Yes', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Nein', 'No', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Wasser', 'Water', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Essen', 'Food / To eat', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Flughafen', 'Airport', 6)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hotel', 'Hotel', 6)");

        // === Polish Words (Course ID: 7) ===
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Cześć', 'Hello / Hi', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Do widzenia', 'Goodbye', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Dziękuję', 'Thank you', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Proszę', 'Please / Here you are', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Tak', 'Yes', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Nie', 'No', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Woda', 'Water', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Jedzenie', 'Food', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Lotnisko', 'Airport', 7)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hotel', 'Hotel', 7)");

        // === Spanish Words (Course ID: 8) ===
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hola', 'Hello', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Adiós', 'Goodbye', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Gracias', 'Thank you', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Por favor', 'Please', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Sí', 'Yes', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('No', 'No', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Agua', 'Water', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Comida', 'Food', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Aeropuerto', 'Airport', 8)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hotel', 'Hotel', 8)");

        // === French Words (Course ID: 9) ===
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Bonjour', 'Hello', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Au revoir', 'Goodbye', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Merci', 'Thank you', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('S''il vous plaît', 'Please', 9)"); // Escaped quote
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Oui', 'Yes', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Non', 'No', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Eau', 'Water', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Nourriture', 'Food', 9)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Aéroport', 'Airport', 9)"); // Note: Often written l'aéroport
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hôtel', 'Hotel', 9)");
    }
}