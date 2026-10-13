package com.example.app_blocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;

public class AppNameCheckService extends Service{
    private Handler handler;
    private Runnable checkRunnable;
    private NotificationHelper notifHelper;
    private boolean isRunning = false;
    private static final int CHECK_INTERVAL_MILLIS = 900;

    private static final String TAG = "AppNameCheckService"; //Debug purposes

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "Foreground service created");

        notifHelper = new NotificationHelper(this);
        handler = new Handler(Looper.getMainLooper());

        checkRunnable = new Runnable() {
            @Override
            public void run() {
                if(isRunning){
                    checkAppName();

                    handler.postDelayed(this, CHECK_INTERVAL_MILLIS);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "Foreground Service started");

        if(!isRunning){
            isRunning = true;
            showForegroundNotif();
            handler.post(checkRunnable);
        }

        return START_STICKY;
    }

    private void showForegroundNotif(){
        Log.d(TAG, "Showing foreground notif");

        String notifText = "Looking for prohibited apps usage";

        Notification notif = notifHelper.createNotif(notifText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notifHelper.getNotificationId(), notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(notifHelper.getNotificationId(), notif);
        }
    }

    private void checkAppName(){
        Log.d(TAG, "Performing apps name check");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Foreground service destroyed");

        if (handler != null && checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
        }

        if (notifHelper != null) {
            notifHelper.cancelNotification();
        }

        isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isMonitoring() {
        return isRunning;
    }
}
