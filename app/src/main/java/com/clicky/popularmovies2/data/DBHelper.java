package com.clicky.popularmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * Created by fabianrodriguez on 10/1/15.
 *
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "popularmovies.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // TODO Auto-generated method stub
        FavoritesTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        FavoritesTable.onUpgrade(db, oldVersion, newVersion);
    }

}