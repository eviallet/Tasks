package com.gueg.tasks.sql;


import android.provider.BaseColumns;

public final class SQLReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SQLReaderContract() {}

    /* Inner class that defines the table contents */
    public class SQLEntry implements BaseColumns {
        public static final String _ID = "id";
        public static final String DB_TABLE_NAME = "tasks";
        public static final String DB_COLUMN_NAME = "name";
        public static final String DB_COLUMN_DATE = "date";
        public static final String DB_COLUMN_TIME= "time";
        public static final String DB_COLUMN_PRIORITY= "priority";
        public static final String DB_COLUMN_ISDONE= "isdone";
        public static final String DB_COLUMN_COMPLETIONDATE = "completiondate";
        public static final String DB_COLUMN_DESCRIPTION= "description";
        public static final String DB_COLUMN_CATEGORY= "category";
        public static final String DB_COLUMN_HASCUSTOMCOLOR= "hascustomcolor";
        public static final String DB_COLUMN_COLOR= "color";
        public static final String DB_COLUMN_ATTENDEE_NAME= "attendeename";
        public static final String DB_COLUMN_ATTENDEE_MAIL= "attendeemail";
        public static final String DB_COLUMN_NOTIFY= "notify";
        public static final String DB_COLUMN_TASKID= "taskid";
        public static final String DB_COLUMN_WILLREPEAT= "willrepeat";
        public static final String DB_COLUMN_REPEAT= "repeat";
        public static final String DB_COLUMN_REPEATUNTIL= "repeatuntil";

    }
}