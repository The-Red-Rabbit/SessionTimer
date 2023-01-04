package com.example.sessiontimer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationMaster {

    private final static int NOTIFICATION_ID = 3143;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private NotificationCompat.Builder builderGen;

    public NotificationMaster(Context context) {
        createNotificationChannel(context);
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, "stdchannel");
        builderGen = new NotificationCompat.Builder(context, "stdchannel");
    }

    public void setupNotification(PendingIntent pendingIntent, PendingIntent pendingIntentClOut) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        builder.setContentTitle("Currently working...")
                .setContentText("You are at work! Start:   "+dateFormat.format(new Date()))
                .setSmallIcon(R.drawable.note_add_24px)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true);

        builder.setContentIntent(pendingIntent);
        builder.addAction(R.drawable.note_add_24px, "Clock out", pendingIntentClOut);
        builder.setWhen(new Date().getTime());

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void setupGenericNotification(String title, String text) {
        builderGen.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.paid24px)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true);

        notificationManager.notify(3144, builderGen.build());
    }

    public void dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Standart";
            String description = "Standart Notificationchannel.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("stdchannel", name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.argb(0,0,255,0));
            channel.enableLights(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
