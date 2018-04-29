package com.gueg.tasks.interfaces;


import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.gueg.tasks.classes.Date;
import com.gueg.tasks.utilities.TasksManager;

import yuku.ambilwarna.AmbilWarnaDialog;

public interface OnMainActivityCallListener {
    TasksManager getTasksManager();
    FragmentManager getFragmentManagerL();
    void showDialog(AmbilWarnaDialog dialog);
    Context getMAContext();
    void addTaskPreset(Date date);
}
