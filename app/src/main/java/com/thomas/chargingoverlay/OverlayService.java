package com.thomas.chargingoverlay;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateStats();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
        createOverlay();
        handler.post(updateRunnable);
    }

    private void startForegroundService() {
        NotificationChannel channel = new NotificationChannel(
                "charging_overlay",
                "Charging Overlay",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "charging_overlay")
                .setContentTitle("Charging Overlay Active")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);
    }

    private void createOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        TextView textView = new TextView(this);
        textView.setTextSize(16);
        textView.setPadding(30, 20, 30, 20);
        textView.setBackgroundColor(0xCC000000);
        textView.setTextColor(0xFFFFFFFF);

        overlayView = textView;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        windowManager.addView(overlayView, params);
    }

    private void updateStats() {
        try {
            String voltage = readSys("/sys/class/power_supply/battery/voltage_now");
            String current = readSys("/sys/class/power_supply/battery/current_now");
            String temp = readSys("/sys/class/power_supply/battery/temp");

            double v = Double.parseDouble(voltage) / 1000000.0;
            double a = Math.abs(Double.parseDouble(current) / 1000000.0);
            double w = v * a;
            double t = Double.parseDouble(temp) / 10.0;

            ((TextView) overlayView).setText(
                    String.format("%.1fW\n%.2fV | %.2fA\n%.1f°C", w, v, a, t)
            );

        } catch (Exception ignored) {}
    }

    private String readSys(String path) {
        try {
            java.lang.Process process = Runtime.getRuntime().exec(
        new String[]{"su", "-c", "cat " + path});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
