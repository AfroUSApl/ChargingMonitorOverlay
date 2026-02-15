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
    private static final int NOTIFICATION_ID = 1;

    private Handler handler = new Handler();
    private Runnable updater;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification("Starting..."));

        updater = new Runnable() {
            @Override
            public void run() {
                updateStats();
                handler.postDelayed(this, 2000); // update every 2 sec
            }
        };

        handler.post(updater);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updater);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateStats() {

        try {
            double voltageRaw = readDouble("/sys/class/power_supply/battery/voltage_now");
            double currentRaw = readDouble("/sys/class/power_supply/battery/current_now");
            double powerRaw   = readDouble("/sys/class/power_supply/battery/power_now");

            // Samsung scaling
            double volts = voltageRaw / 1_000_000.0; // µV → V
            double amps  = currentRaw / 1000.0;      // mA → A

            double watts;

            if (powerRaw > 0) {
                watts = powerRaw / 1000.0;           // mW → W
            } else {
                watts = volts * amps;                // fallback calculation
            }

            String text = String.format("%.2fV  %.2fA  %.2fW", volts, amps, watts);

            Notification notification = buildNotification(text);
            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.notify(NOTIFICATION_ID, notification);
            }

            // Send broadcast to update MainActivity UI
            Intent intent = new Intent("UPDATE_STATS");
            intent.putExtra("voltage", volts);
            intent.putExtra("current", amps);
            intent.putExtra("power", watts);
            sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double readDouble(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat " + path});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            reader.close();
            if (line != null) {
                return Double.parseDouble(line.trim());
            }
        } catch (Exception ignored) {}
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
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}