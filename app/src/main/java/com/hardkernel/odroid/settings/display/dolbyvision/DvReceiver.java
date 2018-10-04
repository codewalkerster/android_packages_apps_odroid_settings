package com.hardkernel.odroid.settings.display.dolbyvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.os.Process;
import android.os.SystemProperties;

import com.hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;

public class DvReceiver extends BroadcastReceiver {
    static final String TAG = "DvReceiver";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d(TAG, "onReceive = " + intent);
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            if (SystemProperties.getBoolean("ro.platform.has.tvuimode", false)) {
            } else {
                SoundParameterSettingManager sound = new SoundParameterSettingManager(context);
                sound.initParameterAfterBoot();
            }
            Intent serviceIntent = new Intent(context, DolbyVisionService.class);
            context.startService(serviceIntent);
        }
    }

}


