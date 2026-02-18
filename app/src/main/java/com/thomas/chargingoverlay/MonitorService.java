package com.thomas.chargingoverlay;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorService extends Service {

    private static final String CHANNEL_ID = "charging_monitor_channel";
    private TelemetryEngine engine;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charging Monitor v3.17 (Safe)")
                .setContentText("Stable telemetry engine running...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);

        engine = new TelemetryEngine(this);
        engine.start();
    }

    @Override
    public void onDestroy() {
        if (engine != null) engine.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    // 🔥 Restore readSys so MainActivity compiles

    public static String readSys(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su","-c","cat " + path});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }
}