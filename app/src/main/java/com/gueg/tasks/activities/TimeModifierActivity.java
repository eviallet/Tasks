package com.gueg.tasks.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gueg.tasks.classes.Date;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.classes.Time;
import com.gueg.tasks.fragments.DatePickerFragment;
import com.gueg.tasks.fragments.TimePickerFragment;
import com.gueg.tasks.interfaces.OnPickerSet;
import com.gueg.tasks.sql.SQLUtility;

public class TimeModifierActivity extends AppCompatActivity {

    boolean isDateSet, isTimeSet;

    Date setDate;
    Time setTime;
    Task task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = (Task) getIntent().getSerializableExtra("TASK");
        if(task!=null) {

            isDateSet = false;
            isTimeSet = false;

            OnPickerSet dateListener = new OnPickerSet() {
                @Override
                public void onTimeSet(Time time) {
                }

                @Override
                public void onDateSet(long date) {
                    setDate = new Date(date);
                    isDateSet = true;
                }
            };
            OnPickerSet timeListener = new OnPickerSet() {
                @Override
                public void onTimeSet(Time time) {
                    setTime = time;
                    isTimeSet = true;
                    applyChanges();
                }

                @Override
                public void onDateSet(long date) {
                }
            };


            TimePickerFragment dialogTime = new TimePickerFragment();
            dialogTime.setOnPickerSetListener(timeListener);
            dialogTime.show(getSupportFragmentManager(), "TIMEPICKER");
            DatePickerFragment dialogDate = new DatePickerFragment();
            dialogDate.setOnPickerSetListener(dateListener);
            dialogDate.show(getSupportFragmentManager(), "DATEPICKER");

        }
    }

    private void applyChanges() {
        if (isDateSet && isTimeSet) {
            SQLUtility sql = new SQLUtility(this);
            sql.updateTime(task, setDate, setTime);
            Intent i = new Intent(this,MainActivity.class);
            i.putExtra("REFRESH",true);
            startActivity(i);
            finishAndRemoveTask();
        } else {
            finishAndRemoveTask();
        }
    }
}
