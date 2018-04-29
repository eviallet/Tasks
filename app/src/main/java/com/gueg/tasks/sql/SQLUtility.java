package com.gueg.tasks.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.classes.Time;
import com.gueg.tasks.notifications.NotificationUtility;
import com.gueg.tasks.sql.SQLReaderContract.SQLEntry;
import com.gueg.tasks.utilities.DateUtility;

import java.util.ArrayList;
import java.util.List;

public class SQLUtility {

    private Context context;

    private SQLReader helper;
    private static String[] projection = {
            SQLEntry._ID,
            SQLEntry.DB_COLUMN_NAME,
            SQLEntry.DB_COLUMN_DATE,
            SQLEntry.DB_COLUMN_TIME,
            SQLEntry.DB_COLUMN_PRIORITY,
            SQLEntry.DB_COLUMN_ISDONE,
            SQLEntry.DB_COLUMN_COMPLETIONDATE,
            SQLEntry.DB_COLUMN_DESCRIPTION,
            SQLEntry.DB_COLUMN_CATEGORY,
            SQLEntry.DB_COLUMN_HASCUSTOMCOLOR,
            SQLEntry.DB_COLUMN_COLOR,
            SQLEntry.DB_COLUMN_ATTENDEE_NAME,
            SQLEntry.DB_COLUMN_ATTENDEE_MAIL,
            SQLEntry.DB_COLUMN_NOTIFY,
            SQLEntry.DB_COLUMN_TASKID,
            SQLEntry.DB_COLUMN_WILLREPEAT,
            SQLEntry.DB_COLUMN_REPEAT,
            SQLEntry.DB_COLUMN_REPEATUNTIL
    };

    public SQLUtility(Context context) {
        this.context = context;
        helper = new SQLReader(context);
    }

    public List<Task> readAllTasks() {

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                SQLEntry.DB_TABLE_NAME,                     // The table to query
                projection,                                 // The columns to return
                null,                                       // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );

        List<Task> items = new ArrayList<>();
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_NAME));
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DATE))));
            Time time = new Time(DateUtility.convertTime(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TIME))));
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_PRIORITY));
            boolean isDone = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ISDONE))==1;
            Date completiondate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_COMPLETIONDATE))));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DESCRIPTION));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_CATEGORY));
            boolean hasCustomColor = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_HASCUSTOMCOLOR))==1;
            int color = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_COLOR));
            String attendeeName = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ATTENDEE_NAME));
            String attendeeMail = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ATTENDEE_MAIL));
            if(attendeeName==null)
                attendeeName="";
            if(attendeeMail==null)
                attendeeMail="";
            boolean notify = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_NOTIFY))==1;
            long taskId = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TASKID)));
            boolean willRepeat = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_WILLREPEAT))==1;
            String repeat = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_REPEAT));
            String repeatUntil = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_REPEATUNTIL));
            long sqlid = Long.decode(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry._ID)));


            items.add(new Task(name,date,time,priority,isDone,completiondate,description,category,hasCustomColor,color,attendeeName,attendeeMail,notify,taskId,willRepeat,repeat,repeatUntil,sqlid));
        }
        cursor.close();

        return items;
    }

    public void write(Task task) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(SQLEntry.DB_COLUMN_NAME,task.getName());
        values.put(SQLEntry.DB_COLUMN_DATE,task.getDate().getTime());
        values.put(SQLEntry.DB_COLUMN_TIME,task.getTime().toString());
        values.put(SQLEntry.DB_COLUMN_PRIORITY,task.getPriority());
        int isdone; if(task.isDone()) isdone = 1; else isdone = 0;
        values.put(SQLEntry.DB_COLUMN_ISDONE,isdone);
        values.put(SQLEntry.DB_COLUMN_COMPLETIONDATE,task.getCompletionDate().getTime());
        values.put(SQLEntry.DB_COLUMN_DESCRIPTION,task.getDescription());
        values.put(SQLEntry.DB_COLUMN_CATEGORY,task.getCategory());
        int hascc; if(task.hasCustomColor()) hascc = 1; else hascc = 0;
        values.put(SQLEntry.DB_COLUMN_HASCUSTOMCOLOR,hascc);
        values.put(SQLEntry.DB_COLUMN_COLOR,task.getColor());
        values.put(SQLEntry.DB_COLUMN_ATTENDEE_NAME,task.getAttendeeName());
        values.put(SQLEntry.DB_COLUMN_ATTENDEE_MAIL,task.getAttendeeMail());
        int notif; if(task.isReminderEnabled()) notif = 1; else notif = 0;
        values.put(SQLEntry.DB_COLUMN_NOTIFY, notif);
        values.put(SQLEntry.DB_COLUMN_TASKID, Long.toString(task.getTaskId()));
        int willr; if(task.willRepeat()) willr = 1; else willr = 0;
        values.put(SQLEntry.DB_COLUMN_WILLREPEAT, willr);
        values.put(SQLEntry.DB_COLUMN_REPEAT, task.getRepeat());
        values.put(SQLEntry.DB_COLUMN_REPEATUNTIL, task.getRepeatUntil());

        task.setSqlId(db.insert(SQLEntry.DB_TABLE_NAME, null, values));


    }

    public void deleteTask(Task task) {
        if(task.getSqlId()!=-1) {
            SQLiteDatabase db = helper.getWritableDatabase();
            // Define 'where' part of query.
            String selection = SQLEntry._ID + " LIKE ?";
            // Specify arguments in placeholder order.
            String[] selectionArgs = {Long.toString(task.getSqlId())};
            // Issue SQL statement.
            db.delete(SQLEntry.DB_TABLE_NAME, selection, selectionArgs);
        }


    }

    public void updateState(Task task, boolean state) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        int st; if(state) st = 1; else st = 0;
        values.put(SQLEntry.DB_COLUMN_ISDONE, st);
        values.put(SQLEntry.DB_COLUMN_COMPLETIONDATE, new Date().getTime());



        boolean update = false;
        String selection="";
        String[] selectionArgs = new String[1];
        // Which row to update, based on the title
        if (task.getSqlId() != -1) {
            selection = SQLEntry._ID + " LIKE ?";
            selectionArgs[0] = Long.toString(task.getSqlId());
            update = true;
        } else if(task.getTaskId() != -1){
            selection = SQLEntry.DB_COLUMN_TASKID + " LIKE ?";
            selectionArgs[0] = Long.toString(task.getTaskId());
            update = true;
        }

        if(update) {
            db.update(SQLEntry.DB_TABLE_NAME, values, selection, selectionArgs);
        }

    }

    public void updateTime(Task t, Date d, Time time) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(SQLEntry.DB_COLUMN_DATE, d.getTime());
        values.put(SQLEntry.DB_COLUMN_TIME, time.toString());


        NotificationUtility notificationUtility = new NotificationUtility(context);
        notificationUtility.cancelNotification(t);

        t.setDate(d);
        t.setTime(time);


        notificationUtility.addNotification(t);


        boolean update = false;
        String selection="";
        String[] selectionArgs = new String[1];
        // Which row to update, based on the title
        if (t.getSqlId() != -1) {
            selection = SQLEntry._ID + " LIKE ?";
            selectionArgs[0] = Long.toString(t.getSqlId());
            update = true;
        } else if(t.getTaskId() != -1){
            selection = SQLEntry.DB_COLUMN_TASKID + " LIKE ?";
            selectionArgs[0] = Long.toString(t.getTaskId());
            update = true;
        }

        if(update) {
            db.update(SQLEntry.DB_TABLE_NAME, values, selection, selectionArgs);
        }


    }


    public Task findTaskById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String selection = SQLEntry.DB_COLUMN_TASKID + " LIKE ?";
        String[] selectionArgs = {Long.toString(id)};

        Cursor cursor = db.query(
                SQLEntry.DB_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
                );

        Task t = null;
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_NAME));
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DATE))));
            Time time = new Time(DateUtility.convertTime(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TIME))));
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_PRIORITY));
            boolean isDone = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ISDONE))==1;
            Date completiondate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_COMPLETIONDATE))));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DESCRIPTION));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_CATEGORY));
            boolean hasCustomColor = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_HASCUSTOMCOLOR))==1;
            int color = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_COLOR));
            String attendeeName = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ATTENDEE_NAME));
            String attendeeMail = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_ATTENDEE_MAIL));
            if(attendeeName==null)
                attendeeName="";
            if(attendeeMail==null)
                attendeeMail="";
            boolean notify = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_NOTIFY))==1;
            long taskId = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TASKID)));
            boolean willRepeat = cursor.getInt(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_WILLREPEAT))==1;
            String repeat = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_REPEAT));
            String repeatUntil = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_REPEATUNTIL));
            long sqlid = Long.decode(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry._ID)));


            t = new Task(name,date,time,priority,isDone,completiondate,description,category,hasCustomColor,color,attendeeName,attendeeMail,notify,taskId,willRepeat,repeat,repeatUntil,sqlid);
        }

        cursor.close();

        return t;
    }



    public void onDestroy() {
        helper.close();
    }

}
