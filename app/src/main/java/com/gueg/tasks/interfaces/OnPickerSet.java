package com.gueg.tasks.interfaces;


import com.gueg.tasks.classes.Time;

public interface OnPickerSet {
    void onTimeSet(Time time);
    void onDateSet(long date);
}
