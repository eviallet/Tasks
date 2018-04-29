package com.gueg.tasks.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.R;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.classes.Time;
import com.gueg.tasks.utilities.DateUtility;

import static android.content.Context.ALARM_SERVICE;

@SuppressWarnings("deprecation")
public class NotificationUtility {

    private Context context;
    NotificationManager mNotificationManager;
    AlarmManager mAlarmManager;

    public static String NOTIFICATION_ACTION_OK = "OK";
    public static String NOTIFICATION_ACTION_LATER = "LATER";
    public static String NOTIFICATION_ACTION_EDIT = "EDIT";


    public NotificationUtility(Context a) {
        context = a;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    public void addNotification(Task task) {

        if(task.isReminderEnabled()) {

            // INTENT
            Intent intent = new Intent(context, NotificationPublisher.class);
            intent.putExtra("NOTIFICATION", getNotification(task));
            intent.putExtra("TASK_ID", (int) task.getTaskId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


            if (DateUtility.isTimePast(new Time(task.getNotificationTime()))) // if missed
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, new Date().getTime() + 2000, pendingIntent);
            else
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getNotificationTime(), pendingIntent);

        }
    }

    public PendingIntent getPendingIntent(Task task) {
        Intent intent = new Intent(context, NotificationPublisher.class);
        intent.putExtra("NOTIFICATION",getNotification(task));
        intent.putExtra("TASK_ID",(int)task.getTaskId());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void cancelNotification(PendingIntent intent) {
        if(intent!=null)
            mAlarmManager.cancel(intent);
    }

    public void cancelNotification(Task t) {
        PendingIntent intent = getPendingIntent(t);
        if(intent!=null)
            mAlarmManager.cancel(intent);
    }

    private Notification getNotification(Task task) {

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        contentView.setTextViewText(R.id.notification_title,task.getName());
        contentView.setTextViewText(R.id.notification_detail,task.getDescription());

        Intent intentEdit = new Intent(context, NotificationActionReceiver.class);
        intentEdit.putExtra("ACTION",NOTIFICATION_ACTION_EDIT);
        intentEdit.putExtra("TASK_ID",(int)task.getTaskId());
        PendingIntent pendingEdit = PendingIntent.getBroadcast(context, 0, intentEdit, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentOk = new Intent(context, NotificationActionReceiver.class);
        intentOk.putExtra("ACTION",NOTIFICATION_ACTION_OK);
        intentOk.putExtra("TASK_ID",(int)task.getTaskId());
        PendingIntent pendingOk = PendingIntent.getBroadcast(context, 1, intentOk, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentLater = new Intent(context, NotificationActionReceiver.class);
        intentLater.putExtra("ACTION",NOTIFICATION_ACTION_LATER);
        intentLater.putExtra("TASK_ID",(int)task.getTaskId());
        PendingIntent pendingLater = PendingIntent.getBroadcast(context, 2, intentLater, PendingIntent.FLAG_UPDATE_CURRENT);


        // TODO si disposition conservée : bkg notif = B/W? sharedpref
        //contentView.setTextColor();
        contentView.setOnClickPendingIntent(R.id.notification_image_ok,pendingOk);
        contentView.setOnClickPendingIntent(R.id.notification_image_later,pendingLater);
        contentView.setOnClickPendingIntent(R.id.notification_image_edit,pendingEdit);


        // SET NOTIFICATION CONTENT
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_small_icon)
                .setContent(contentView)
                /*.setContentTitle(task.getName())
                .setContentText(task.getDescription())
                .setContentIntent(pendingEdit)
                .addAction(new Notification.Action(R.drawable.notification_later,"Plus Tard",pendingLater))
                .addAction(new Notification.Action(R.drawable.notification_ok,"Terminé",pendingOk))*/
                .setWhen(new Date().getTime())
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[] { 0, 250, 250, 250 })
                .setLights(task.getColor(), 1000, 1000)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        notification.flags ^= Notification.FLAG_AUTO_CANCEL;



        return notification;
    }



}
