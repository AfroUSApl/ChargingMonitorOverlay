
package com.thomas.chargingoverlay;

import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorService extends Service {

    private static final String CHANNEL_ID = "charging_monitor";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updater;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotification("Starting..."));

        updater = () -> {
            updateNotification();
            handler.postDelayed(updater, 1000);
        };
        handler.post(updater);
    }

    private void updateNotification() {
        try {
            float voltage = readValue("voltage_now") / 1000000f;
            float current = readValue("current_now") / 1000f;
            float power = voltage * current;

            String compact = String.format("⚡ %.2fV | %.2fA | %.1fW",
                    voltage, current, power);

            String expanded = String.format(
                    "Voltage: %.2f V\nCurrent: %.2f A\nPower: %.1f W",
                    voltage, current, power);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(compact)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(expanded))
                    .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(
                            PendingIntent.getActivity(this, 0,
                                    new Intent(this, MainActivity.class),
                                    PendingIntent.FLAG_IMMUTABLE))
                    .build();

            startForeground(1, notification);

        } catch (Exception ignored) {}
    }

    private float readValue(String file) throws Exception {
        java.lang.Process process = Runtime.getRuntime()
                .exec(new String[]{"su", "-c",
                        "cat /sys/class/power_supply/battery/" + file});

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        return Float.parseFloat(reader.readLine());
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(text)
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Charging Monitor",
                NotificationManager.IMPORTANCE_LOW);

        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
