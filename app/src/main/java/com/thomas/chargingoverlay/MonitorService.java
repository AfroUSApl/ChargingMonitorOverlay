
package com.thomas.chargingoverlay;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorService extends Service {

    private static final String CHANNEL_ID = "charging_monitor_channel";
    private static Handler handler = new Handler();
    private static Runnable runnable;

    public static void start(Context context) {
        Intent intent = new Intent(context, MonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, MonitorService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charging Monitor v3.0")
                .setContentText("Monitoring battery telemetry...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Monitor",
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public static String readSys(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su","-c","cat " + path});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
