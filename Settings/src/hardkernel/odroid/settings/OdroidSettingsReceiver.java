package hardkernel.odroid.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import hardkernel.odroid.settings.cpu.CpuReceiver;
import hardkernel.odroid.settings.display.rotation.RotationReceiver;
import hardkernel.odroid.settings.gpu.GpuReceiver;
import hardkernel.odroid.settings.shortcut.ShortcutManager;
import hardkernel.odroid.settings.kiosk.KioskManager;

public class OdroidSettingsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        skipUserSetup(context);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CpuReceiver.onReceive();
            GpuReceiver.onReceive();
            RotationReceiver.onReceive(context);
            ShortcutManager.onReceive(context);
            KioskManager.onReceive(context);
        }
    }

    private void skipUserSetup(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TV_USER_SETUP_COMPLETE, 0) == 0) {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.TV_USER_SETUP_COMPLETE, 1);
        }
    }
}
