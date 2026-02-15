package com.thomas.chargingoverlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView voltageText;
    private TextView currentText;
    private TextView powerText;
    private TextView statusText;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double v = intent.getDoubleExtra("voltage", 0);
            double c = intent.getDoubleExtra("current", 0);
            double p = intent.getDoubleExtra("power", 0);

            statusText.setText("Status: Charging");
            voltageText.setText(String.format("Voltage: %.2f V", v));
            currentText.setText(String.format("Current: %.2f A", c));
            powerText.setText(String.format("Power: %.2f W", p));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        voltageText = findViewById(R.id.voltageText);
        currentText = findViewById(R.id.currentText);
        powerText = findViewById(R.id.powerText);
        statusText = findViewById(R.id.statusText);

        start.setOnClickListener(v ->
                startService(new Intent(this, MonitorService.class)));

        stop.setOnClickListener(v -> {
            stopService(new Intent(this, MonitorService.class));
            statusText.setText("Status: Stopped");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter("UPDATE_STATS"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}