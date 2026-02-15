package com.thomas.chargingoverlay;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvVoltage, tvCurrent, tvPower, tvTemp;
    private TextView tvCycle, tvHealth, tvCapacity;
    private TextView tvDirectInput, tvStep, tvTimeToFull;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        start.setOnClickListener(v -> MonitorService.start(this));
        stop.setOnClickListener(v -> MonitorService.stop(this));

        // Static
        tvCycle = findViewById(R.id.tvCycle);
        tvHealth = findViewById(R.id.tvHealth);
        tvCapacity = findViewById(R.id.tvCapacity);

        // Live
        tvVoltage = findViewById(R.id.tvVoltage);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvPower = findViewById(R.id.tvPower);
        tvTemp = findViewById(R.id.tvTemp);
        tvDirectInput = findViewById(R.id.tvDirectInput);
        tvStep = findViewById(R.id.tvStep);
        tvTimeToFull = findViewById(R.id.tvTimeToFull);

        startUpdating();
    }

    private void startUpdating() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateBatteryInfo();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateBatteryInfo() {

        String voltage = MonitorService.readSys("/sys/class/power_supply/battery/voltage_now");
        String current = MonitorService.readSys("/sys/class/power_supply/battery/current_now");
        String power = MonitorService.readSys("/sys/class/power_supply/battery/power_now");
        String temp = MonitorService.readSys("/sys/class/power_supply/battery/temp");

        String cycle = MonitorService.readSys("/sys/class/power_supply/battery/battery_cycle");
        String full = MonitorService.readSys("/sys/class/power_supply/battery/charge_full");
        String design = MonitorService.readSys("/sys/class/power_supply/battery/charge_full_design");

        String direct = MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_iin");
        String step = MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_step");
        String ttf = MonitorService.readSys("/sys/class/power_supply/battery/time_to_full_now");

        tvVoltage.setText("Voltage: " + formatVoltage(voltage));
        tvCurrent.setText("Current: " + formatCurrent(current));
        tvPower.setText("Power: " + formatPower(voltage, current));
        tvTemp.setText("Temperature: " + formatTemp(temp));

        tvCycle.setText("Cycle Count: " + cycle);
        tvCapacity.setText("Design vs Real: " + full + " / " + design);
        tvHealth.setText("Health: " + calculateHealth(full, design) + "%");

        tvDirectInput.setText("Direct Input Current: " + direct);
        tvStep.setText("Charging Step: " + step);
        tvTimeToFull.setText("Time To Full: " + ttf);
    }

    private String formatVoltage(String raw) {
        try {
            double v = Double.parseDouble(raw) / 1000000.0;
            return String.format("%.2f V", v);
        } catch (Exception e) { return "N/A"; }
    }

    private String formatCurrent(String raw) {
        try {
            double c = Double.parseDouble(raw);
            if (Math.abs(c) > 100000)
                c = c / 1000.0;
            return String.format("%.0f mA", c);
        } catch (Exception e) { return "N/A"; }
    }

    private String formatPower(String voltageRaw, String currentRaw) {
        try {
            double v = Double.parseDouble(voltageRaw) / 1000000.0;
            double c = Double.parseDouble(currentRaw);
            if (Math.abs(c) > 100000)
                c = c / 1000.0;
            double w = (v * c) / 1000.0;
            return String.format("%.2f W", w);
        } catch (Exception e) { return "N/A"; }
    }

    private String formatTemp(String raw) {
        try {
            double t = Double.parseDouble(raw) / 10.0;
            return String.format("%.1f °C", t);
        } catch (Exception e) { return "N/A"; }
    }

    private int calculateHealth(String full, String design) {
        try {
            double f = Double.parseDouble(full);
            double d = Double.parseDouble(design);
            return (int)((f / d) * 100);
        } catch (Exception e) { return 0; }
    }
}