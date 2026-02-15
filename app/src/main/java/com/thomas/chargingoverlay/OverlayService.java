
package com.thomas.chargingoverlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlayView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (overlayView != null) return START_STICKY;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new TextView(this);
        overlayView.setText("Charging Overlay Active");
        overlayView.setTextSize(18);
        overlayView.setBackgroundColor(0x88000000);
        overlayView.setTextColor(0xFFFFFFFF);
        overlayView.setPadding(40, 40, 40, 40);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 200;

        windowManager.addView(overlayView, params);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
