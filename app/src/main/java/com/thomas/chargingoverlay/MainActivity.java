
package com.thomas.chargingoverlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60,120,60,60);

        TextView tv = new TextView(this);
        tv.setText("Charging Overlay v1.5 (Root)\n\nGrant overlay permission, then START.");
        tv.setTextSize(18);

        Button start = new Button(this);
        start.setText("START OVERLAY");
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                startForegroundService(new Intent(this, OverlayService.class));
            }
        });

        Button stop = new Button(this);
        stop.setText("STOP OVERLAY");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));

        layout.addView(tv);
        layout.addView(start);
        layout.addView(stop);

        setContentView(layout);
    }
}
