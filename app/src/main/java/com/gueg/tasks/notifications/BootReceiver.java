package com.gueg.tasks.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.sql.SQLUtility;

import java.util.List;

@SuppressWarnings("unchecked")
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final SQLUtility sql = new SQLUtility(context);
        NotificationUtility notificationUtility = new NotificationUtility(context);
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected List<Task> doInBackground(Object[] params) {
                return sql.readAllTasks();
            }
        };
        List<Task> tasksList;
        try {
            tasksList = (List<Task>)asyncTask.execute().get();
            for(Task task : tasksList) {
                if (task.isTimeSet() && task.isReminderEnabled() && !task.isDone())
                    notificationUtility.addNotification(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
