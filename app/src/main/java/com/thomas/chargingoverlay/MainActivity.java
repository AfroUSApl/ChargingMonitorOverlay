package com.thomas.chargingoverlay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        start.setOnClickListener(v ->
                startService(new Intent(this, MonitorService.class)));

        stop.setOnClickListener(v ->
                stopService(new Intent(this, MonitorService.class)));
    }
}