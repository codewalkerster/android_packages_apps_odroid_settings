package com.hardkernel.odroid.settings.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CheckVersionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            updateManager.setPreference(context.
                    getSharedPreferences(updateManager.SHPREF_UPDATE_SERVER, Context.MODE_PRIVATE));

            if (!updateManager.isCheckAtBoot())
                return;
            UpdatePackage.checkLatestVersion(context);
        }
    }
}