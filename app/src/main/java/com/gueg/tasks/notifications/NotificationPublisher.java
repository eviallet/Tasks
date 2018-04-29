package com.gueg.tasks.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationPublisher extends WakefulBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra("NOTIFICATION");
        int id = intent.getIntExtra("TASK_ID", 0);
        if(notification!=null&&id!=0)
            notificationManager.notify(id, notification);

    }
}
