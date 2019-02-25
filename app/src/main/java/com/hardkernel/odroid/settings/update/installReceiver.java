package com.hardkernel.odroid.settings.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RecoverySystem;
import android.widget.Toast;

import java.io.File;

public class installReceiver extends BroadcastReceiver {
    public static final String INSTALL_NOTIFICATION_ID = "Odroid_Install_ID";
    public static final String INSTALL_NOTIFICATION_VALUE = "GO";
    @Override
    public void onReceive(Context context, Intent intent) {
        String value = intent.getStringExtra(INSTALL_NOTIFICATION_ID);

        File packageFile = (File)intent.getBundleExtra("packageFile").getSerializable("packageFile");

        if (value.contentEquals(INSTALL_NOTIFICATION_VALUE)) {
            try {
                RecoverySystem.installPackage(context, packageFile);
            } catch (Exception e) {
                Toast.makeText(context,
                        "Error while install OTA package: " + e,
                        Toast.LENGTH_LONG).show();

            }
        }
    }
}
