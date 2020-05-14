package com.hardkernel.odroid.settings.update;

import android.content.Context;

public class CheckVersionReceiver {
    public static void onReceive(Context context) {
        updateManager.setPreference(context.
                getSharedPreferences(updateManager.SHPREF_UPDATE_SERVER, Context.MODE_PRIVATE));
        updateManager.initServer();
        updateManager.initURL();

        if (!updateManager.isCheckAtBoot())
            return;
        UpdatePackage.checkLatestVersion(context);
    }
}