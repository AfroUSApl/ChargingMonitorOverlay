package com.thomas.chargingoverlay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private TextView status;
    private TextView voltage;
    private TextView current;
    private TextView power;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        status = findViewById(R.id.statusText);
        voltage = findViewById(R.id.voltageText);
        current = findViewById(R.id.currentText);
        power = findViewById(R.id.powerText);

        start.setOnClickListener(v -> {
            startService(new Intent(this, MonitorService.class));
            updateUI();
        });

        stop.setOnClickListener(v -> {
            stopService(new Intent(this, MonitorService.class));
            status.setText("Status: Stopped");
        });
    }

    private void updateUI() {

        double voltageRaw = readSysfs("/sys/class/power_supply/battery/voltage_now");
        double currentRaw = readSysfs("/sys/class/power_supply/battery/current_now");

        if (voltageRaw == 0) {
            status.setText("Status: No kernel data");
            return;
        }

        double volts = voltageRaw / 1000000.0;
        double amps = currentRaw / 1000000.0;
        double watts = volts * amps;

        status.setText("Status: Charging");
        voltage.setText("Voltage: " + String.format("%.2f V", volts));
        current.setText("Current: " + String.format("%.2f A", amps));
        power.setText("Power: " + String.format("%.2f W", watts));
    }

    private double readSysfs(String path) {
        try {

            Process process = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "cat " + path});

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            reader.close();
            process.waitFor();

            if (line != null && !line.isEmpty()) {
                return Double.parseDouble(line.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}