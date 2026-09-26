package com.example.app_blocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private Context context;
    private NotificationManager notifManager;
    private static final String CHANNEL_ID = "AppNameCheckServChannel";

    public NotificationHelper(Context con){
        this.context = con;
        this.notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotifChannel();
    }

    public Notification createNotif(String contentTxt){
        Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notifIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("App blocker service is running")
                .setContentText(contentTxt)
                .setSmallIcon(com.google.android.material.R.drawable.abc_ic_star_black_16dp) // Temporary
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        return builder.build();
    }

    private void createNotifChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App name check channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("Channel for app name check service notifications");
            notifManager.createNotificationChannel(channel);
        }
    }
}
