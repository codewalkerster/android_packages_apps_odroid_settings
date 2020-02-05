package com.hardkernel.odroid.settings.display.position;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.droidlogic.app.DisplayPositionManager;
import com.hardkernel.odroid.settings.ConfigEnv;

public class DisplayPositionReceiver extends BroadcastReceiver {
    private final String TAG = "DisplayPositionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals((Intent.ACTION_BOOT_COMPLETED))) {
            DisplayPositionManager mDisplayPositionManager;
            mDisplayPositionManager = new DisplayPositionManager (context);

            mDisplayPositionManager.zoomByPercent(ConfigEnv.getDisplayZoomrate());
        }
    }
}
