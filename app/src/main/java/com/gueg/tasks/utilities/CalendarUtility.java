package com.gueg.tasks.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Reminders;
import android.support.v4.app.ActivityCompat;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.classes.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


public class CalendarUtility {

    private int calId;
    private Context context;

    private static String[] EVENTS_QUERY = new String[]{
            Events._ID,
            Events.CALENDAR_ID,
            Events.TITLE,
            Events.DESCRIPTION,
            Events.DTSTART,
            Events.HAS_ATTENDEE_DATA
    };

    private static String EVENTS_SELECTION = "(( " + Events.DTSTART + " >= " + new Date().getTime() + " ))";

    /**
     * Used to delete entries only
     */
    public CalendarUtility(Context a) {
        context = a;
    }

    public CalendarUtility(Context a, int calId) {
        context = a;
        this.calId = calId;
    }


    public ArrayList<Task> retrieveCalendarEvents() {
        ArrayList<Task> retrievedTasks = new ArrayList<>();


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Cursor cur = context.getContentResolver().query(Events.CONTENT_URI, EVENTS_QUERY, EVENTS_SELECTION, null, null);

        if(cur!=null) {
            if (cur.moveToFirst()) {
                do {
                    if(Long.decode(cur.getString(cur.getColumnIndex(Events.CALENDAR_ID)))==calId) {
                        String eventId = cur.getString(cur.getColumnIndex(Events._ID));
                        String title = cur.getString(cur.getColumnIndex(Events.TITLE));
                        String description = cur.getString(cur.getColumnIndex(Events.DESCRIPTION));
                        Date dtstart = new Date(Long.decode(cur.getString(cur.getColumnIndex(Events.DTSTART))));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dtstart);
                        long hour, minute;
                        Time timestart = new Time(DateUtility.getTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                        hour = timestart.toHours();
                        minute = timestart.toMinutes();
                        if (hour == 0 && minute == 0)
                            timestart = new Time(0);
                        boolean hasAttendee = cur.getString(cur.getColumnIndex(Events.HAS_ATTENDEE_DATA)).equals("1");
                        String attendeeName = "";
                        String attendeeMail = "";
                        if (hasAttendee) {
                            String[] ATTENDEES_QUERY = new String[]{
                                    Attendees.EVENT_ID,
                                    Attendees.ATTENDEE_NAME,
                                    Attendees.ATTENDEE_EMAIL
                            };
                            String ATTENDEE_SELECTION = "(( " + Attendees.EVENT_ID + " == " + eventId + " ))";
                            Cursor att = context.getContentResolver().query(Attendees.CONTENT_URI,ATTENDEES_QUERY,ATTENDEE_SELECTION,null,null);

                            if(att!=null) {
                                if(att.moveToFirst()) {
                                    do {
                                        attendeeName = att.getString(cur.getColumnIndex(Attendees.ATTENDEE_NAME));
                                        attendeeMail = att.getString(cur.getColumnIndex(Attendees.ATTENDEE_EMAIL));
                                        if(attendeeMail==null)
                                            attendeeMail=attendeeName.substring(0,attendeeName.indexOf("@"));
                                    } while(cur.moveToNext());
                                }
                                att.close();
                            }
                        }

                        retrievedTasks.add(new Task(title, dtstart, timestart, description, attendeeName, attendeeMail, Long.decode(eventId)));
                    }
                } while (cur.moveToNext());
            }

            cur.close();
        }

        return retrievedTasks;
    }



    public void writeTask(Task t) {
        long calID = calId;
        Calendar cal = Calendar.getInstance();
        cal.setTime(t.getDate());
        long startMillis;
        long endMillis;
        if(t.isTimeSet()) {
            cal.set(Calendar.HOUR_OF_DAY, (int) t.getTime().toHours());
            cal.set(Calendar.MINUTE, (int) t.getTime().toMinutes());
            startMillis = cal.getTimeInMillis();
            cal.add(Calendar.HOUR_OF_DAY, 1);
            endMillis = cal.getTimeInMillis();
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            startMillis = cal.getTimeInMillis();
            cal.add(Calendar.HOUR_OF_DAY, 23);
            cal.add(Calendar.MINUTE,59);
            endMillis = cal.getTimeInMillis();
        }

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND,endMillis);
        values.put(Events.TITLE, t.getName());
        values.put(Events.DESCRIPTION, t.getDescription());
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "Europe/Paris");

        if(t.willRepeat()) {
            String repeat = t.getRepeat();
            String repeatUntil = t.getRepeatUntil();

            String valRepeat;
            String valRepeatUntil;

            switch (repeat) {
                case "Chaque semaine":
                    valRepeat="WEEKLY";
                    break;
                case "Chaque mois":
                    valRepeat="MONTHLY";
                    break;
                default:
                    valRepeat="YEARLY";
                    break;
            }
            switch(repeatUntil) {
                case "Pendant un mois":
                    valRepeatUntil=getUTC("Mois");
                    break;
                case "Pendant 6 mois":
                    valRepeatUntil=getUTC("6Mois");
                    break;
                case "Pendant 1 an":
                    valRepeatUntil=getUTC("An");
                    break;
                default:
                    valRepeatUntil=getUTC("5Ans");
                    break;
            }

            values.put(Events.RRULE,"FREQ="+valRepeat+";UNTIL="+valRepeatUntil);

        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
             return;
        }
        Uri event = cr.insert(Events.CONTENT_URI, values);

        assert event != null;
        t.setTaskId(Long.parseLong(event.getLastPathSegment()));


        if(t.hasAttendee()) {
            ContentValues attendee = new ContentValues();
            attendee.put(Attendees.ATTENDEE_NAME, t.getAttendeeName());
            attendee.put(Attendees.ATTENDEE_EMAIL, t.getAttendeeMail());
            attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ATTENDEE);
            attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
            attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_ACCEPTED);
            attendee.put(Attendees.EVENT_ID, t.getTaskId());
            cr.insert(Attendees.CONTENT_URI, attendee);
        }


        if(t.isReminderEnabled()) {
            ContentValues reminder = new ContentValues();
            reminder.put(Reminders.MINUTES, 1000000000);
            reminder.put(Reminders.EVENT_ID, t.getTaskId());
            reminder.put(Reminders.METHOD, Reminders.METHOD_ALERT);
            cr.insert(Reminders.CONTENT_URI, reminder);
        }


    }

    public void deleteTask(Task t) {
        if(t.getTaskId()!=-1) {
            Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, t.getTaskId());
            context.getContentResolver().delete(deleteUri, null, null);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getUTC(String d) {
        Calendar cal = Calendar.getInstance();
        switch(d) {
            case "Mois":
                cal.add(Calendar.MONTH,1);
                break;
            case "6Mois":
                cal.add(Calendar.MONTH,6);
                break;
            case "An":
                cal.add(Calendar.YEAR,1);
                break;
            default:
                cal.add(Calendar.YEAR,5);
                break;
        }
        Date time = new Date(cal.getTimeInMillis());
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFmt.format(time);
    }



}
