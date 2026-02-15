
package com.thomas.chargingoverlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            context.startForegroundService(new Intent(context, OverlayService.class));
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            context.stopService(new Intent(context, OverlayService.class));
        }
    }
}
