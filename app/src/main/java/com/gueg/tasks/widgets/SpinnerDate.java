package com.gueg.tasks.widgets;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import com.gueg.tasks.classes.Date;
import com.gueg.tasks.utilities.DateUtility;

import java.util.Arrays;
import java.util.Calendar;

public class SpinnerDate extends android.support.v7.widget.AppCompatSpinner {

    private Date selectedDate;

    public SpinnerDate(Context context) {
        super(context);
    }

    public SpinnerDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.addAll(Arrays.asList("","Aujourd'hui","Demain","Samedi","Choisir une date"));
        setAdapter(spinnerArrayAdapter);
        setSelection(0);
        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==4)
                    getDateFromPicker();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }




    public Date getDateFromPicker(){
        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        DateUtility du = new DateUtility();
                        selectedDate = new Date(du.getDate(year,month,dayOfMonth));
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        return selectedDate;

    }

    private Date getNextSaturday() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day =  Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        return new Date(calendar.getTime().getTime());
    }

    private Date getTomorrow() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day =  Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day+1);

        return new Date(calendar.getTime().getTime());
    }



    public Date getDate() {
        switch (getSelectedItemPosition()) {
            case 0:
                return new Date(0);
            case 1:
                return new Date(Calendar.getInstance().getTime().getTime());
            case 2:
                return getTomorrow();
            case 3:
                return getNextSaturday();
            default:
                return selectedDate;
        }
    }

    public void setDateFromPicker(long date) {
        selectedDate = new Date(date);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.addAll(Arrays.asList("","Aujourd'hui","Demain","Samedi",selectedDate.toString(),"Choisir une date"));
        setAdapter(spinnerArrayAdapter);
        setSelection(4);
    }




}
