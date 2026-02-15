package com.thomas.chargingoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorService extends Service {

    private static final String CHANNEL_ID = "charging_monitor_channel";
    private Handler handler = new Handler();
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        startForeground(1, buildNotification("Starting..."));

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(updateRunnable);
    }

    private void updateNotification() {

        double currentRaw = readSysfs("/sys/class/power_supply/battery/current_now");
        double voltageRaw = readSysfs("/sys/class/power_supply/battery/voltage_now");

        double amps = currentRaw / 1000.0;           // Samsung mA
        double volts = voltageRaw / 1000000.0;       // µV → V
        double watts = amps * volts;

        String text = String.format("V: %.2fV  A: %.2fA  W: %.2fW",
                volts, amps, watts);

        Notification notification = buildNotification(text);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }

    private double readSysfs(String path) {
        try {
            Process process = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "cat " + path});

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            reader.close();

            if (line != null) {
                return Double.parseDouble(line.trim());
            }

        } catch (Exception ignored) { }

        return 0;
    }

    private Notification buildNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charging Monitor")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Monitor",
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}