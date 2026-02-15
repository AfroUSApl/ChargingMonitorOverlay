
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
    private Handler handler = new Handler();
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
        startForegroundNotification();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);
    }

    private void startForegroundNotification() {
        String channelId = "charging_overlay";
        NotificationChannel channel = new NotificationChannel(
                channelId, "Charging Overlay",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("Charging Overlay Running")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);
    }

    private void showOverlay() {
        if (overlayView != null) return;

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
                PixelFormat.TRANSLUCENT);

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
    }

    private void hideOverlay() {
        if (overlayView != null) {
            handler.removeCallbacks(updater);
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    private void updateStats() {
        try {
            String voltage = readSys("/sys/class/power_supply/battery/voltage_now");
            String current = readSys("/sys/class/power_supply/battery/current_now");

            float v = Float.parseFloat(voltage) / 1000000f;
            float c = Float.parseFloat(current) / 1000000f;
            float w = Math.abs(v * c);

            textView.setText(String.format("V: %.2fV  A: %.2fA  W: %.2fW", v, c, w));
        } catch (Exception e) {
            textView.setText("Reading charging data...");
        }
    }

    private String readSys(String path) throws Exception {
        java.lang.Process process = Runtime.getRuntime().exec(
                new String[]{"su", "-c", "cat " + path});
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
