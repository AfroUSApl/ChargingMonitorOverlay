package com.thomas.chargingoverlay;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private TextView voltage, current, power, temp;
    private TextView cycleCount, healthPercent, designCapacity;
    private TextView directCurrent, chargingStep, timeToFull;

    private Handler handler = new Handler();
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        voltage = findViewById(R.id.voltage);
        current = findViewById(R.id.current);
        power = findViewById(R.id.power);
        temp = findViewById(R.id.temp);

        cycleCount = findViewById(R.id.cycleCount);
        healthPercent = findViewById(R.id.healthPercent);
        designCapacity = findViewById(R.id.designCapacity);

        directCurrent = findViewById(R.id.directCurrent);
        chargingStep = findViewById(R.id.chargingStep);
        timeToFull = findViewById(R.id.timeToFull);

        loadStaticData();

        start.setOnClickListener(v -> {
            running = true;
            updateLoop();
        });

        stop.setOnClickListener(v -> running = false);
    }

    private void updateLoop() {
        handler.postDelayed(() -> {
            if (!running) return;

            try {
                double volt = readLong("/sys/class/power_supply/battery/voltage_now") / 1000000.0;
                double curr = readLong("/sys/class/power_supply/battery/current_now") / 1000.0;
                double pow = volt * curr;
                double temperature = readLong("/sys/class/power_supply/battery/temp") / 10.0;

                long dirCurr = readLong("/sys/class/power_supply/battery/direct_charging_iin");
                long step = readLong("/sys/class/power_supply/battery/direct_charging_step");
                long ttf = readLong("/sys/class/power_supply/battery/time_to_full_now");

                voltage.setText(String.format("Voltage: %.2f V", volt));
                current.setText(String.format("Current: %.2f A", curr));
                power.setText(String.format("Power: %.2f W", pow));
                temp.setText(String.format("Temp: %.1f °C", temperature));

                directCurrent.setText("Direct Input: " + dirCurr + " mA");
                chargingStep.setText("Charging Step: " + step);
                timeToFull.setText("Time to Full: " + ttf + " min");

            } catch (Exception ignored) {}

            updateLoop();

        }, 1000);
    }

    private void loadStaticData() {
        try {
            long cycles = readLong("/sys/class/power_supply/battery/battery_cycle");
            long full = readLong("/sys/class/power_supply/battery/charge_full");
            long design = readLong("/sys/class/power_supply/battery/charge_full_design");

            double health = (double) full / design * 100.0;

            cycleCount.setText("Cycle Count: " + cycles);
            healthPercent.setText(String.format("Battery Health: %.1f %%", health));
            designCapacity.setText("Design Capacity: " + design + " uAh");

        } catch (Exception ignored) {}
    }

    private long readLong(String path) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su","-c","cat " + path});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            br.close();
            return Long.parseLong(line.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}