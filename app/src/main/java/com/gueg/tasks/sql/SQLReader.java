package com.gueg.tasks.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gueg.tasks.sql.SQLReaderContract.SQLEntry;


public class SQLReader extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    SQLEntry.DB_TABLE_NAME + " (" +
                    SQLEntry._ID + " INTEGER PRIMARY KEY," +
                    SQLEntry.DB_COLUMN_NAME + " TEXT," +
                    SQLEntry.DB_COLUMN_DATE + " TEXT," +
                    SQLEntry.DB_COLUMN_TIME + " TEXT," +
                    SQLEntry.DB_COLUMN_PRIORITY + " INTEGER," +
                    SQLEntry.DB_COLUMN_ISDONE + " INTEGER," +
                    SQLEntry.DB_COLUMN_COMPLETIONDATE + " TEXT," +
                    SQLEntry.DB_COLUMN_DESCRIPTION + " TEXT," +
                    SQLEntry.DB_COLUMN_CATEGORY + " TEXT," +
                    SQLEntry.DB_COLUMN_HASCUSTOMCOLOR + " INTEGER," +
                    SQLEntry.DB_COLUMN_COLOR + " INTEGER," +
                    SQLEntry.DB_COLUMN_ATTENDEE_NAME + " TEXT," +
                    SQLEntry.DB_COLUMN_ATTENDEE_MAIL + " TEXT," +
                    SQLEntry.DB_COLUMN_NOTIFY + " INTEGER," +
                    SQLEntry.DB_COLUMN_TASKID + " TEXT," +
                    SQLEntry.DB_COLUMN_WILLREPEAT + " INTEGER," +
                    SQLEntry.DB_COLUMN_REPEAT + " TEXT," +
                    SQLEntry.DB_COLUMN_REPEATUNTIL + " TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SQLEntry.DB_TABLE_NAME;


    public SQLReader(Context context) {
        super(context, SQLEntry.DB_COLUMN_NAME, null, DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }





}
