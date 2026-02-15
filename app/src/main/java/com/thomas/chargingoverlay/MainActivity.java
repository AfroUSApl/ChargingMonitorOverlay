
package com.thomas.chargingoverlay;

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

        start.setOnClickListener(v -> MonitorService.start(this));
        stop.setOnClickListener(v -> MonitorService.stop(this));
    }
}
