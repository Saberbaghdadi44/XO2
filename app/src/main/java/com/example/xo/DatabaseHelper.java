package com.example.xo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "xogame_archive.db";
    private static final int DATABASE_VERSION = 1;

    // Table des archives
    public static final String TABLE_ARCHIVES = "score_archives";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PLAYER1_NAME = "player1_name";
    public static final String COLUMN_PLAYER2_NAME = "player2_name";
    public static final String COLUMN_PLAYER1_WINS = "player1_wins";
    public static final String COLUMN_PLAYER2_WINS = "player2_wins";
    public static final String COLUMN_DRAWS = "draws";
    public static final String COLUMN_DATE = "date_created";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ARCHIVES_TABLE = "CREATE TABLE " + TABLE_ARCHIVES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PLAYER1_NAME + " TEXT, "
                + COLUMN_PLAYER2_NAME + " TEXT, "
                + COLUMN_PLAYER1_WINS + " INTEGER, "
                + COLUMN_PLAYER2_WINS + " INTEGER, "
                + COLUMN_DRAWS + " INTEGER, "
                + COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_ARCHIVES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARCHIVES);
        onCreate(db);
    }

    // Archiver une session de jeu
    public void archiveSession(String player1Name, String player2Name, int player1Wins, int player2Wins, int draws) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PLAYER1_NAME, player1Name);
        values.put(COLUMN_PLAYER2_NAME, player2Name);
        values.put(COLUMN_PLAYER1_WINS, player1Wins);
        values.put(COLUMN_PLAYER2_WINS, player2Wins);
        values.put(COLUMN_DRAWS, draws);

        // Date actuelle
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        values.put(COLUMN_DATE, currentDate);

        db.insert(TABLE_ARCHIVES, null, values);
        db.close();
    }

    // Récupérer toutes les archives
    public Cursor getAllArchives() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ARCHIVES,
                new String[]{COLUMN_ID, COLUMN_PLAYER1_NAME, COLUMN_PLAYER2_NAME,
                        COLUMN_PLAYER1_WINS, COLUMN_PLAYER2_WINS, COLUMN_DRAWS, COLUMN_DATE},
                null, null, null, null, COLUMN_DATE + " DESC");
    }

    // Supprimer les anciennes archives
    public void clearOldArchives() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ARCHIVES + " WHERE " + COLUMN_ID + " NOT IN " +
                "(SELECT " + COLUMN_ID + " FROM " + TABLE_ARCHIVES + " ORDER BY " +
                COLUMN_DATE + " DESC LIMIT 50)");
        db.close();
    }
}