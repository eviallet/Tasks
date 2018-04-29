package com.gueg.tasks.classes;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Time extends java.sql.Time{

    boolean isTimeSet;

    public Time(long val) {
        super(val);
        isTimeSet = val != 0;
    }


    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        if(isTimeSet)
            return formatter.format(this);
        else
            return "";
    }

    public long toHours() {
        return TimeUnit.MILLISECONDS.toHours(getTime());
    }

    public long toMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(getTime()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(getTime()));
    }

}
