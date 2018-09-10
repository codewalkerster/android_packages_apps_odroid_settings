package com.hardkernel.odroid.settings.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CpuReceiver extends BroadcastReceiver {
    private final String TAG = "CpuReceiver";
    private final CPU cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences pref = context.getSharedPreferences("cpu", Context.MODE_PRIVATE);
            cpu.governor.set(pref.getString("governor", cpu.governor.getCurrent()));
            cpu.frequency.setScalingMax(pref.getString("frequency", cpu.frequency.getScalingCurrent()));
        }
    }
}
