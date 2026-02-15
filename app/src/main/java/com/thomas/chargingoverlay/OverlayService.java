
package com.thomas.chargingoverlay;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private TextView textView;
    private Handler handler;
    private Runnable updater;

    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                showOverlay();
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                hideOverlay();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundSafe();
        return START_STICKY;
    }

    private void startForegroundSafe() {
        try {
            String channelId = "charging_overlay";

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Charging Overlay",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Charging Overlay Running")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                    .build();

            startForeground(1, notification);

        } catch (Exception ignored) {}
    }

    private void showOverlay() {
        if (overlayView != null) return;

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            textView = new TextView(this);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(18);
            textView.setBackgroundColor(Color.argb(180, 0, 0, 0));
            textView.setPadding(40, 40, 40, 40);

            overlayView = textView;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = 200;

            windowManager.addView(overlayView, params);

            updater = new Runnable() {
                @Override
                public void run() {
                    updateStats();
                    handler.postDelayed(this, 2000);
                }
            };

            handler.post(updater);

        } catch (Exception ignored) {}
    }

    private void hideOverlay() {
        try {
            if (overlayView != null) {
                handler.removeCallbacks(updater);
                windowManager.removeView(overlayView);
                overlayView = null;
            }
        } catch (Exception ignored) {}
    }

    private void updateStats() {
        try {
            String voltage = readSys("/sys/class/power_supply/battery/voltage_now");
            String current = readSys("/sys/class/power_supply/battery/current_now");

            if (voltage == null || current == null) return;

            float v = Float.parseFloat(voltage) / 1000000f;
            float c = Float.parseFloat(current) / 1000000f;
            float w = Math.abs(v * c);

            textView.setText(String.format("V: %.2fV  A: %.2fA  W: %.2fW", v, c, w));

        } catch (Exception ignored) {}
    }

    private String readSys(String path) {
        try {
            java.lang.Process process = Runtime.getRuntime().exec(
                    new String[]{"su", "-c", "cat " + path});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(powerReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
