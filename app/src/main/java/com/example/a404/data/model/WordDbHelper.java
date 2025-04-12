package com.example.a404.data.model;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WordDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wordlearning.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_WORDS = "words";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Course table columns
    public static final String COLUMN_COURSE_NAME = "name";
    public static final String COLUMN_COURSE_DESCRIPTION = "description";

    // Word table columns
    public static final String COLUMN_WORD_TEXT = "word_text";
    public static final String COLUMN_WORD_TRANSLATION = "translation";
    public static final String COLUMN_WORD_COURSE_ID = "course_id";

    // CREATE TABLE statements
    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COURSE_NAME + " TEXT NOT NULL, "
            + COLUMN_COURSE_DESCRIPTION + " TEXT);";

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
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_WORDS_TABLE);

        // Insert some sample data
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert courses
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ") VALUES ('Basic Vocabulary', 'Essential everyday English words for beginners')");
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ") VALUES ('Travel English', 'Useful words and phrases for travelers')");
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ") VALUES ('Business English', 'Professional vocabulary for workplace settings')");
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ") VALUES ('Academic English', 'Terms commonly used in educational contexts')");
        db.execSQL("INSERT INTO " + TABLE_COURSES + " (" + COLUMN_COURSE_NAME + ", " + COLUMN_COURSE_DESCRIPTION + ") VALUES ('Idioms & Expressions', 'Common English idioms and their meanings')");

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

        // Business English words (Course ID: 3)
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Meeting', 'An assembly of people for a particular purpose', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Deadline', 'A time or date by which something must be completed', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Budget', 'An estimate of income and expenditure', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Contract', 'A written or spoken agreement', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Presentation', 'A formal talk on a particular topic', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Negotiate', 'To discuss with a view to reaching an agreement', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Strategy', 'A plan of action designed to achieve a long-term goal', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Investment', 'The action of investing money for profit', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Revenue', 'Income from business activities', 3)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Stakeholder', 'A person with an interest in a business', 3)");

        // Academic English words (Course ID: 4)
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Thesis', 'A statement or theory put forward to be maintained or proved', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Research', 'Systematic investigation to establish facts', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Analysis', 'Detailed examination of something', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Critique', 'A detailed analysis and assessment', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Methodology', 'A system of methods used in a discipline', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hypothesis', 'A supposition made on evidence as a starting point for investigation', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Theory', 'A supposition or system of ideas explaining something', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Citation', 'A quotation from or reference to a book or author', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Abstract', 'A summary of a research paper or article', 4)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Plagiarism', 'The practice of taking someone else''s work and passing it off as one''s own', 4)");

        // Idioms & Expressions (Course ID: 5)
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Break a leg', 'Good luck (often said to performers)', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Cost an arm and a leg', 'To be very expensive', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Hit the nail on the head', 'To describe exactly what is causing a situation or problem', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Under the weather', 'Feeling ill', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Piece of cake', 'Something very easy to do', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Raining cats and dogs', 'Raining very heavily', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('The ball is in your court', 'It''s your turn to take action or make a decision', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Speak of the devil', 'Said when a person appears just after being mentioned', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Beat around the bush', 'To avoid getting to the point of an issue', 5)");
        db.execSQL("INSERT INTO " + TABLE_WORDS + " (" + COLUMN_WORD_TEXT + ", " + COLUMN_WORD_TRANSLATION + ", " + COLUMN_WORD_COURSE_ID + ") VALUES ('Once in a blue moon', 'Very rarely', 5)");
    }
}