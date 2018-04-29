package com.gueg.tasks.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.gueg.tasks.interfaces.OnPickerSet;
import com.gueg.tasks.classes.Time;
import com.gueg.tasks.utilities.DateUtility;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private OnPickerSet mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onTimeSet(new Time(DateUtility.getTime(hourOfDay,minute)));
    }

    public void setOnPickerSetListener(OnPickerSet listener) {
        mListener = listener;
    }


}

