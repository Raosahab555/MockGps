package com.example.service;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SearchDBHelper extends SQLiteOpenHelper{

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "SearchHistory.db";
    public static final String TABLE_NAME = "HistorySearch";
    //IsLocate [0:搜索 1:定位]
    private static final String CREATE_TABLE="create table if not exists " + TABLE_NAME +
            " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SearchKey TEXT NOT NULL, Description TEXT, TimeStamp BIGINT NOT NULL, IsLocate INTEGER NOT NULL, WGS84Longitude TEXT, WGS84Latitude TEXT, BD09Longitude TEXT, BD09Latitude TEXT)";

    public SearchDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("SQL",CREATE_TABLE);sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
