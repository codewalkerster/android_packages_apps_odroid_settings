package hardkernel.odroid.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hardkernel.odroid.settings.cpu.CpuReceiver;
import hardkernel.odroid.settings.gpu.GpuReceiver;

public class OdroidSettingsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CpuReceiver.onReceive();
            GpuReceiver.onReceive();
        }
    }
}
