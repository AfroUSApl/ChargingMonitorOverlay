package com.thomas.chargingoverlay;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlayView;
    private WindowManager.LayoutParams params;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int baseY = 200;

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateOverlay();
            pixelShift();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
        createOverlay();
        handler.post(updater);
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "charging_overlay",
                    "Charging Overlay",
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, "charging_overlay")
                    .setContentTitle("Charging Overlay Running")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                    .build();

            startForeground(1, notification);
        }
    }

    private void createOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new TextView(this);
        overlayView.setTextColor(0xFFFFFFFF);
        overlayView.setBackgroundColor(0x88000000);
        overlayView.setPadding(40, 20, 40, 20);
        overlayView.setTextSize(16);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = baseY;

        windowManager.addView(overlayView, params);
    }

    private void pixelShift() {
        params.x = random.nextInt(20) - 10;
        params.y = baseY + (random.nextInt(20) - 10);
        windowManager.updateViewLayout(overlayView, params);
    }

    private String readFile(String path) {
        try {
            java.lang.Process process = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "cat " + path});
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateOverlay() {
        try {
            String status = readFile("/sys/class/power_supply/battery/status");
            if (status == null || !status.contains("Charging")) {
                overlayView.setText("");
                return;
            }

            double voltage = Double.parseDouble(
                    readFile("/sys/class/power_supply/battery/voltage_now")) / 1000000.0;

            double current = Double.parseDouble(
                    readFile("/sys/class/power_supply/battery/current_now")) / 1000.0;

            double capacity = Double.parseDouble(
                    readFile("/sys/class/power_supply/battery/capacity"));

            String time = readFile("/sys/class/power_supply/battery/time_to_full_now");

            double power = voltage * current;

            overlayView.setText(
                    (int)capacity + "%\n" +
                    String.format("%.2f V\n", voltage) +
                    String.format("%.2f A\n", current) +
                    String.format("%.1f W\n", power) +
                    (time != null ? time + " min until full" : "")
            );

        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updater);
        if (overlayView != null)
            windowManager.removeView(overlayView);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
