package com.gueg.tasks.widgets;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import com.gueg.tasks.classes.Time;
import com.gueg.tasks.utilities.DateUtility;

import java.util.Arrays;

public class SpinnerTime extends android.support.v7.widget.AppCompatSpinner {

    private java.sql.Time selectedTime;
    private java.sql.Time defaultTime1;
    private java.sql.Time defaultTime2;
    private java.sql.Time defaultTime3;
    private java.sql.Time defaultTime4;


    public SpinnerTime(Context context) {
        super(context);
    }

    public SpinnerTime(Context context, AttributeSet attrs) {
        super(context, attrs);

        DateUtility du = new DateUtility();

        defaultTime1 = new Time(du.getTime(9,0));
        defaultTime2 = new Time(du.getTime(13,0));
        defaultTime3 = new Time(du.getTime(17,0));
        defaultTime4 = new Time(du.getTime(20,0));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.addAll(Arrays.asList("",defaultTime1.toString(),defaultTime2.toString(),defaultTime3.toString(),defaultTime4.toString(),"Choisir une heure"));
        setAdapter(spinnerArrayAdapter);
        setSelection(0);
    }


    public Time getTime() {
        if(getSelectedItemPosition()==0) {
            return new Time(0);
        }
        else {
            DateUtility du = new DateUtility();
            return new Time(du.convertTime((String) getSelectedItem()));
        }
    }

    public void setTimeFromPicker(Time time) {
        selectedTime = time;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.addAll(Arrays.asList("",defaultTime1.toString(),defaultTime2.toString(),defaultTime3.toString(),defaultTime4.toString(),selectedTime.toString(),"Choisir une heure"));
        setAdapter(spinnerArrayAdapter);
        setSelection(5);
    }



}

