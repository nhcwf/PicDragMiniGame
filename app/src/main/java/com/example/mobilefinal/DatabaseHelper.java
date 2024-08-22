/**
 * The HelloWorld program implements an application that
 * simply displays "Hello World!" to the standard output.
 *
 * @author  THTN
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MobileFinal.db";
    public static final int DATABASE_VERSION = 1;
    public static final String USERS_TABLE_NAME = "USERS";
    public static final String HIGHSCORES_TABLE_NAME = "HIGHSCORES";

    // Constructor
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + USERS_TABLE_NAME + "(USERNAME TEXT PRIMARY KEY, PASSWORD TEXT)");
        db.execSQL("CREATE TABLE " + HIGHSCORES_TABLE_NAME + "(ID INT PRIMARY KEY, SCORE INT, PLAYTIME LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HIGHSCORES_TABLE_NAME);

        onCreate(db);
    }

    private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert digested message to hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64) { hexString.insert(0, '0'); }

        return hexString.toString();
    }

    // Returns true if insert successfully, false otherwise.
    public boolean insertUser(String username, String password) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("username", username);

        // Put encode password using SHA-256 into the data set
        try {
            contentValues.put("password", toHexString(getSHA(password)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Insert a row into the database
        long result = database.insert(USERS_TABLE_NAME, null, contentValues);

        return result != -1;
    }

    // Returns true if username does not exist in the database, false otherwise.
    public boolean usernameNotFound(String username) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + USERS_TABLE_NAME + " WHERE USERNAME = ?", new String[] { username });

        return cursor.getCount() == 0;
    }

    // Returns true if the username and password are matching with ones in the database.
    public boolean isValidLogin(String username, String password) {
        SQLiteDatabase database = this.getReadableDatabase();
        String hashPassword = "";

        // Hashing the password to get the password hash for comparing
        try {
            hashPassword = toHexString(getSHA(password));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Cursor cursor = database.rawQuery("SELECT * FROM " + USERS_TABLE_NAME + " WHERE USERNAME = ? AND PASSWORD = ?", new String[] { username, hashPassword });

        return cursor.getCount() != 0;
    }

    // Insert a game session into database. Returns true if succeeded, false otherwise.
    public boolean insertGameSession(int id, int score, long playtimeMilliseconds) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("id", id);
        contentValues.put("score", score);
        contentValues.put("playtime", playtimeMilliseconds);

        long result = database.insert(HIGHSCORES_TABLE_NAME, null, contentValues);

        return result != -1;
    }

    // Returns the number of game session in the database.
    public int getGameSessionCount() {
        SQLiteDatabase database = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = database.rawQuery(String.format("SELECT * FROM %s", HIGHSCORES_TABLE_NAME), null);

        return cursor.getCount();
    }

    // Assigns a game session with id to the parameter gameSession.
    @SuppressLint("Range")
    public void setGameSession(GameSession gameSession, int id) {
        SQLiteDatabase database = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = database.rawQuery(String.format("SELECT * FROM %s WHERE ID = ?", HIGHSCORES_TABLE_NAME), new String[] { String.valueOf(id) });

        if (cursor.moveToFirst()) {
            int score = cursor.getInt(cursor.getColumnIndex("SCORE"));
            long playtimeMillisecond = cursor.getInt(cursor.getColumnIndex("PLAYTIME"));

            gameSession.setId(id);
            gameSession.setScore(score);
            gameSession.setPlaytime(playtimeMillisecond);
        }
    }
}
