package com.thomas.chargingoverlay;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView voltage, current, power, temp, cpuTemp;
    private TextView cycleCount, healthPercent, designCapacity;
    private TextView directCurrent, chargingStep, timeToFull;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons (MATCH XML IDs)
        Button start = findViewById(R.id.startButton);
        Button stop = findViewById(R.id.stopButton);

        // Overview
        cycleCount = findViewById(R.id.cycleCount);
        healthPercent = findViewById(R.id.healthPercent);
        designCapacity = findViewById(R.id.designCapacity);

        // Live Data
        voltage = findViewById(R.id.voltage);
        current = findViewById(R.id.current);
        power = findViewById(R.id.power);
        temp = findViewById(R.id.temp);
        cpuTemp = findViewById(R.id.cpuTemp);
        directCurrent = findViewById(R.id.directCurrent);
        chargingStep = findViewById(R.id.chargingStep);
        timeToFull = findViewById(R.id.timeToFull);

        start.setOnClickListener(v -> startMonitoring());
        stop.setOnClickListener(v -> stopMonitoring());

        loadStaticInfo();
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateLiveData();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopMonitoring() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void loadStaticInfo() {
        cycleCount.setText("Cycle Count: " +
                MonitorService.readSys("/sys/class/power_supply/battery/battery_cycle"));

        String design = MonitorService.readSys("/sys/class/power_supply/battery/charge_full_design");
        String real = MonitorService.readSys("/sys/class/power_supply/battery/charge_full");

        designCapacity.setText("Design vs Real: " + design + " / " + real);

        healthPercent.setText("Health: 100%");
    }

    private void updateLiveData() {

        try {
            double voltageV = Double.parseDouble(
                    MonitorService.readSys("/sys/class/power_supply/battery/voltage_now")
            ) / 1_000_000.0;

            double currentA = Double.parseDouble(
                    MonitorService.readSys("/sys/class/power_supply/battery/current_now")
            ) / 1_000_000.0;

            double powerW = voltageV * currentA;

            double battTemp = Double.parseDouble(
                    MonitorService.readSys("/sys/class/power_supply/battery/temp")
            ) / 10.0;

            voltage.setText(String.format("Voltage: %.2f V", voltageV));
            current.setText(String.format("Current: %.2f A", currentA));
            power.setText(String.format("Power: %.2f W", powerW));
            temp.setText(String.format("Battery Temp: %.1f °C", battTemp));

        } catch (Exception ignored) {}

        cpuTemp.setText("CPU Temp: " +
                MonitorService.readSys("/sys/class/thermal/thermal_zone0/temp"));

        directCurrent.setText("Direct Input Current: " +
                MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_iin"));

        chargingStep.setText("Charging Step: " +
                MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_step"));

        timeToFull.setText("Time To Full: " +
                MonitorService.readSys("/sys/class/power_supply/battery/time_to_full_now"));
    }
}