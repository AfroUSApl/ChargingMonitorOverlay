
package com.thomas.chargingoverlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OverlayService extends Service {

    private WindowManager wm;
    private TextView textView;
    private Thread thread;
    private boolean running = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        textView = new TextView(this);
        textView.setTextSize(16);
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundColor(0x88000000);
        textView.setTextColor(0xFFFFFFFF);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = 200;

        wm.addView(textView, params);

        thread = new Thread(() -> {
            while (running) {
                try {
                    double voltage = readSmart("/sys/class/power_supply/battery/voltage_now");
                    double current = readSmart("/sys/class/power_supply/battery/current_now");
                    double watts = voltage * current;

                    String display = String.format("%.2f V\n%.2f A\n%.1f W",
                            voltage, current, watts);

                    textView.post(() -> textView.setText(display));
                    Thread.sleep(2000);

                } catch (Exception e) {
                    textView.post(() -> textView.setText("Charging..."));
                }
            }
        });

        thread.start();
        return START_STICKY;
    }

    private double readSmart(String path) throws Exception {
        String raw = readFile(path);
        if (raw == null) return 0;

        double val = Double.parseDouble(raw.trim());

        if (Math.abs(val) > 100000) return val / 1000000.0;  // µ units
        if (Math.abs(val) > 1000) return val / 1000.0;       // m units
        return val;
    }

    private String readFile(String path) throws Exception {
        java.lang.Process process = Runtime.getRuntime()
                .exec(new String[]{"su", "-c", "cat " + path});
        BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return br.readLine();
    }

    @Override
    public void onDestroy() {
        running = false;
        if (wm != null && textView != null) {
            wm.removeView(textView);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
