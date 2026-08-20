package com.example.app_blocker;

import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

public class ActiveAppNameCheckService extends Service {
    private static final int INTERVAL = 900;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable checkNameRunnable = new Runnable() {
        @Override public void run() {
            checkAppName();
            handler.postDelayed(checkNameRunnable, INTERVAL);
        }
    };

    @Override public IBinder onBind(Intent intent){
        return null;
    }

    @Override public void onCreate(){
        super.onCreate();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Notification notification =
                    new NotificationCompat.Builder(this, "CHANNEL_ID")
                            // Create the notification to display while the service
                            // is running
                            .build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                        100,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                );
            } else {
                startForeground(100, notification);
            }
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    e instanceof ForegroundServiceStartNotAllowedException
            ) {
                // App not in a valid state to start foreground service
                // (e.g started from bg)
            }
            // ...
        }

        return START_STICKY;
    }

    private void checkAppName(){
        Log.d("Debug", "Foreground Service is working!");
    }

    @Override public void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(checkNameRunnable);
    }
}
