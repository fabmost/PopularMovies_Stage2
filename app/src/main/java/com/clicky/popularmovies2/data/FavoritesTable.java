package com.clicky.popularmovies2.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 *
 * Created by fabianrodriguez on 10/1/15.
 *
 */
public class FavoritesTable {

    public static final String TABLE_FAVS = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SYNOPSIS = "synopsis";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_POSTER = "poster";
    public static final String COLUMN_BACK = "back";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FAVS
            + "("
            + COLUMN_ID + " text primary key, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_SYNOPSIS + " text not null, "
            + COLUMN_DATE + " text not null, "
            + COLUMN_RATE + " real not null, "
            + COLUMN_POSTER + " text not null, "
            + COLUMN_BACK + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(FavoritesTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVS);
        onCreate(database);
    }

}
