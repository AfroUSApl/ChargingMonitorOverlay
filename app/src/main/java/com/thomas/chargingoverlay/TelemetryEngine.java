 
package com.thomas.chargingoverlay;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TelemetryEngine {

    private final Context context;
    private HandlerThread thread;
    private Handler handler;

    private Process rootProcess;
    private OutputStreamWriter rootWriter;
    private BufferedReader rootReader;

    private static final long NORMAL_INTERVAL = 2000;
    private static final long HOT_INTERVAL = 5000;

    public TelemetryEngine(Context context) {
        this.context = context;
    }

    public void start() {
        thread = new HandlerThread("TelemetryEngineThread");
        thread.start();
        handler = new Handler(thread.getLooper());

        openRootShell();
        scheduleNext(NORMAL_INTERVAL);
    }

    public void stop() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (thread != null) thread.quitSafely();
        closeRootShell();
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

            int voltageMicroV =
                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE);

            double currentA = currentMicroA / 1_000_000.0;
            double voltageV = voltageMicroV / 1_000_000.0;
            double powerW = currentA * voltageV;

            double batteryTemp = readBatteryTemp();

            boolean isHot = batteryTemp > 45.0;

            // You can later push this data to UI via broadcast or LiveData
            System.out.println("V=" + voltageV +
                    " I=" + currentA +
                    " P=" + powerW +
                    " Temp=" + batteryTemp);

            scheduleNext(isHot ? HOT_INTERVAL : NORMAL_INTERVAL);

        } catch (Exception e) {
            scheduleNext(NORMAL_INTERVAL);
        }
    }

    private double readBatteryTemp() {
        try {
            String temp = readRootFile("/sys/class/power_supply/battery/temp");
            if (temp == null) return 0;
            return Integer.parseInt(temp.trim()) / 10.0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------- Persistent Root Shell ----------

    private void openRootShell() {
        try {
            rootProcess = Runtime.getRuntime().exec("su");
            rootWriter = new OutputStreamWriter(rootProcess.getOutputStream());
            rootReader = new BufferedReader(
                    new InputStreamReader(rootProcess.getInputStream()));
        } catch (Exception ignored) {
        }
    }

    private void closeRootShell() {
        try {
            if (rootWriter != null) rootWriter.close();
            if (rootReader != null) rootReader.close();
            if (rootProcess != null) rootProcess.destroy();
        } catch (Exception ignored) {
        }
    }

    private String readRootFile(String path) {
        try {
            rootWriter.write("cat " + path + "\n");
            rootWriter.flush();
            return rootReader.readLine();
        } catch (Exception e) {
            return null;
        }
    }
}