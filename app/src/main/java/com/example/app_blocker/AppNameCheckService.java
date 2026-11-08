package com.example.app_blocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
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
    private String lastActivityName = null;
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

    private String checkAppName(){
        Log.d(TAG, "Performing apps name check");

        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);

        try {
            long currentTimeMillis = System.currentTimeMillis();
            String activityName = null;
            String packageName = null;

            UsageEvents queryEvents = usageStatsManager.queryEvents(
                    currentTimeMillis - 60000L,
                    currentTimeMillis
            );

            while(queryEvents.hasNextEvent()){
                UsageEvents.Event event = new UsageEvents.Event();
                queryEvents.getNextEvent(event);

                if(event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                    packageName = event.getPackageName();
                    activityName = event.getClassName();
                }
                else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED){
                    if (event.getPackageName() != packageName){
                        break;
                    }
                    packageName = null;
                }
            }

            if(packageName != null && activityName != null){
                return lastActivityName = activityName;
            }
            else{
                return lastActivityName;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
