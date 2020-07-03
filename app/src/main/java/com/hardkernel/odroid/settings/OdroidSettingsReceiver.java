package com.hardkernel.odroid.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hardkernel.odroid.settings.cpu.CpuReceiver;
import com.hardkernel.odroid.settings.display.outputmode.AutoFramerateManager;
import com.hardkernel.odroid.settings.display.position.DisplayPositionReceiver;
import com.hardkernel.odroid.settings.shortcut.ShortcutManager;
import com.hardkernel.odroid.settings.update.updateManager;
import com.hardkernel.odroid.settings.gpu.GpuReceiver;

public class OdroidSettingsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CpuReceiver.onReceive();
            DisplayPositionReceiver.onReceive(context);
            ShortcutManager.onReceived(context);
            updateManager.onReceived(context);
            AutoFramerateManager.onReceived();
            GpuReceiver.onReceive();
        }
    }
}