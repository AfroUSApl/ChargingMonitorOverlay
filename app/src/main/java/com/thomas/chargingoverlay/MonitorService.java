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
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = buildNotification("Starting...");
        startForeground(1, notification);

        runnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification() {

        double voltageRaw = readSysfs("/sys/class/power_supply/battery/voltage_now");
        double currentRaw = readSysfs("/sys/class/power_supply/battery/current_now");

        if (voltageRaw == 0) {
            Notification notification = buildNotification("No kernel data");
            getManager().notify(1, notification);
            return;
        }

        double volts = voltageRaw / 1000000.0;
        double amps = currentRaw / 1000000.0;
        double watts = volts * amps;

        String text = String.format("V: %.2fV  A: %.2fA  W: %.2fW",
                volts, amps, watts);

        Notification notification = buildNotification(text);
        getManager().notify(1, notification);
    }

    private double readSysfs(String path) {
        try {

            Process process = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "cat " + path});

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            reader.close();
            process.waitFor();

            if (line != null && !line.isEmpty()) {
                return Double.parseDouble(line.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private NotificationManager getManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            getManager().createNotificationChannel(channel);
        }
    }
}