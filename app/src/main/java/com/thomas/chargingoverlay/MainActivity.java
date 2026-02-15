package com.thomas.chargingoverlay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText, voltageText, currentText, powerText;
    private Handler handler = new Handler();
    private Runnable updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        statusText = findViewById(R.id.statusText);
        voltageText = findViewById(R.id.voltageText);
        currentText = findViewById(R.id.currentText);
        powerText = findViewById(R.id.powerText);

        start.setOnClickListener(v -> {
            startService(new Intent(this, MonitorService.class));
            startLiveMonitor();
        });

        stop.setOnClickListener(v -> {
            stopService(new Intent(this, MonitorService.class));
            stopLiveMonitor();
        });
    }

    private void startLiveMonitor() {
        updater = new Runnable() {
            @Override
            public void run() {

                String voltageStr = SysReader.read("/sys/class/power_supply/battery/voltage_now");
                String currentStr = SysReader.read("/sys/class/power_supply/battery/current_now");

                if (voltageStr != null && currentStr != null) {
                    try {
                        double voltage = Double.parseDouble(voltageStr) / 1000000.0;
                        double current = Double.parseDouble(currentStr) / 1000000.0;
                        double power = voltage * current;

                        statusText.setText("Status: Charging");
                        voltageText.setText("Voltage: " + String.format("%.2f V", voltage));
                        currentText.setText("Current: " + String.format("%.2f A", current));
                        powerText.setText("Power: " + String.format("%.2f W", power));

                    } catch (Exception e) {
                        statusText.setText("Status: Parse error");
                    }
                } else {
                    statusText.setText("Status: Read failed (root?)");
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(updater);
    }

    private void stopLiveMonitor() {
        if (updater != null) {
            handler.removeCallbacks(updater);
        }

        statusText.setText("Status: Stopped");
        voltageText.setText("Voltage: --");
        currentText.setText("Current: --");
        powerText.setText("Power: --");
    }
}