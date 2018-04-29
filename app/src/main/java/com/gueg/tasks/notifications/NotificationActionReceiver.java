package com.gueg.tasks.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.gueg.tasks.activities.MainActivity;
import com.gueg.tasks.activities.TimeModifierActivity;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.sql.SQLUtility;

public class NotificationActionReceiver extends WakefulBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        String action = intent.getStringExtra("ACTION");
        int id = intent.getIntExtra("TASK_ID", 0);
        if(action!=null&&id!=0) {
            SQLUtility sql = new SQLUtility(context);
            Task task =sql.findTaskById(id);
            if(task!=null) {
                if (action.equals(NotificationUtility.NOTIFICATION_ACTION_OK)) {
                    sql.updateState(task,true);
                    notificationManager.cancel(id);
                    Intent i = new Intent(context,MainActivity.class);
                    i.putExtra("REFRESH",true);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else if (action.equals(NotificationUtility.NOTIFICATION_ACTION_LATER)) {
                    Intent timeModifier = new Intent(context, TimeModifierActivity.class);
                    timeModifier.putExtra("TASK",task);
                    timeModifier.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(timeModifier);
                    notificationManager.cancel(id);
                } else if (action.equals(NotificationUtility.NOTIFICATION_ACTION_EDIT)) {
                    Intent editIntent = new Intent(context, MainActivity.class);
                    editIntent.putExtra("ACTION_EDIT",true);
                    editIntent.putExtra("TASK_EDIT",task);
                    editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(editIntent);
                    notificationManager.cancel(id);
                }
            }
        }


    }

}
