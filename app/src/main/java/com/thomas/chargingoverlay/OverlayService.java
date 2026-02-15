
package com.thomas.chargingoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlay;
    private Handler handler = new Handler();
    private final String CHANNEL_ID = "overlay_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForegroundServiceInternal();

        if (overlay != null) return START_STICKY;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlay = new TextView(this);
        overlay.setTextSize(16);
        overlay.setTextColor(0xFFFFFFFF);
        overlay.setBackgroundColor(0xCC000000);
        overlay.setPadding(40,40,40,40);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 200;

        windowManager.addView(overlay, params);

        overlay.setOnLongClickListener(v -> {
            stopSelf();
            return true;
        });

        startUpdating();

        return START_STICKY;
    }

    private void startForegroundServiceInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Overlay Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Charging Overlay Running")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                    .build();

            startForeground(1, notification);
        }
    }

    private void startUpdating() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateData() {
        try {
            String voltage = readFile("/sys/class/power_supply/battery/voltage_now");
            String current = readFile("/sys/class/power_supply/battery/current_now");
            String capacity = readFile("/sys/class/power_supply/battery/capacity");
            String status = readFile("/sys/class/power_supply/battery/status");

            double v = Double.parseDouble(voltage.trim()) / 1000000.0;
            double mA = Double.parseDouble(current.trim()) / 1000.0;
            double watts = v * (mA / 1000.0);

            String text = "⚡ " + capacity.trim() + "%  " + status.trim() +
                    "\n" + String.format("%.2f V", v) +
                    "\n" + String.format("%.0f mA", mA) +
                    "\n" + String.format("%.2f W", watts);

            overlay.setText(text);

        } catch (Exception e) {
            overlay.setText("Root required\n" + e.getMessage());
        }
    }

    private String readFile(String path) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat " + path});
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (overlay != null && windowManager != null) {
            windowManager.removeView(overlay);
            overlay = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
