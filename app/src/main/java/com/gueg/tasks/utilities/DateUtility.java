package com.gueg.tasks.utilities;

import com.gueg.tasks.classes.Date;
import com.gueg.tasks.classes.Time;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DateUtility {

    public static int YESTERDAY = -1;
    public static int TODAY = 0;
    public static int TOMORROW = 1;
    public static int TWODAYS = 2;

    public static long getDate(int year, int month, int day) {
        Calendar calendar =  Calendar.getInstance();
        calendar.set(year,month,day);
        return calendar.getTime().getTime();
    }

    public static long getTime(int hour, int minutes) {
        return TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minutes);
    }

    public static int getProximity(Date date) {
        return Math.round(date.toDays()-new Date(Calendar.getInstance().getTime().getTime()).toDays());
    }

    public static boolean isTimePast(Time time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY,(int)time.toHours());
        cal.set(Calendar.MINUTE,(int)time.toMinutes());
        long currentTime = new Date().getTime();
        return currentTime>cal.getTime().getTime();
    }

    /**
     * Used for completion date ends : 2 hours shifting, not accurate.
     */
    public static boolean isDatePast(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY,1);
        long currentTime = new Date().getTime();
        return currentTime>cal.getTime().getTime();
    }

    public static long convertTime(String time) {
        if(time.isEmpty())
            return 0;
        else {
        int quoteInd = time.indexOf(":");

        int hour = Integer.valueOf(time.substring(0, quoteInd));
        int min = Integer.valueOf(time.substring(quoteInd+1,time.length()));

        return getTime(hour,min);
        }
    }






}
