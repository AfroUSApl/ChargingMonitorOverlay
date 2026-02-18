package com.thomas.chargingoverlay;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;

public class TelemetryEngine {

    private final Context context;
    private HandlerThread thread;
    private Handler handler;

    private static final long NORMAL_INTERVAL = 2000;
    private static final long HOT_INTERVAL = 5000;

    public TelemetryEngine(Context context) {
        this.context = context;
    }

    public void start() {
        thread = new HandlerThread("TelemetryEngineThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        scheduleNext(NORMAL_INTERVAL);
    }

    public void stop() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (thread != null) thread.quitSafely();
    }

    private void scheduleNext(long delay) {
        handler.postDelayed(this::readTelemetry, delay);
    }

    private void readTelemetry() {

        try {
            BatteryManager bm =
                    (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

            int currentMicroA =
                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

            double currentA = currentMicroA / 1_000_000.0;

            // Voltage from sysfs (root)
            String voltageRaw = MonitorService.readSys(
                    "/sys/class/power_supply/battery/voltage_now");

            double voltageV = 0;
            if (voltageRaw != null)
                voltageV = Integer.parseInt(voltageRaw) / 1_000_000.0;

            double powerW = currentA * voltageV;

            String tempRaw = MonitorService.readSys(
                    "/sys/class/power_supply/battery/temp");

            double batteryTemp = 0;
            if (tempRaw != null)
                batteryTemp = Integer.parseInt(tempRaw) / 10.0;

            boolean isHot = batteryTemp > 45.0;

            System.out.println("V=" + voltageV +
                    " I=" + currentA +
                    " P=" + powerW +
                    " Temp=" + batteryTemp);

            scheduleNext(isHot ? HOT_INTERVAL : NORMAL_INTERVAL);

        } catch (Exception e) {
            scheduleNext(NORMAL_INTERVAL);
        }
    }
}