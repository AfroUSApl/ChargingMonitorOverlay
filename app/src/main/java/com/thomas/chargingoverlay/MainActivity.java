
package com.thomas.chargingoverlay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startButton);
        Button stop = findViewById(R.id.stopButton);

        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            } else {
                startService(new Intent(this, OverlayService.class));
            }
        });

        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
    }
}
