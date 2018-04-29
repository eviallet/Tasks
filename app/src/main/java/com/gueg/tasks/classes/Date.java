package com.gueg.tasks.classes;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Date extends java.sql.Date implements Serializable {


    SimpleDateFormat formatter = new SimpleDateFormat("EEE dd MMM", Locale.FRANCE);
    boolean isDateSet;

    /**
     * this becomes today's date
     */
    public Date() {
        super(Calendar.getInstance().getTime().getTime());
        isDateSet = true;
    }

    /**
     * @param date millis elapsed since 1/1/1970
     */
    public Date(long date) {
        super(date);
        isDateSet = date != 0;
    }

    public boolean isSet() {
        return isDateSet;
    }


    @Override
    public String toString() {
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/France"));
        if(isDateSet)
            return formatter.format(this);
        else
            return "";
    }

    public long toDays() {
        return TimeUnit.MILLISECONDS.toDays(getTime());
    }


    public boolean isEqual(Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        int date1Day = cal.get(Calendar.DAY_OF_MONTH);
        int date1Month = cal.get(Calendar.MONTH);
        int date1Year = cal.get(Calendar.YEAR);
        cal.setTime(date2);
        int date2Day = cal.get(Calendar.DAY_OF_MONTH);
        int date2Month = cal.get(Calendar.MONTH);
        int date2Year = cal.get(Calendar.YEAR);

        return date1Day==date2Day&&date1Month==date2Month&&date1Year==date2Year;
    }


}
