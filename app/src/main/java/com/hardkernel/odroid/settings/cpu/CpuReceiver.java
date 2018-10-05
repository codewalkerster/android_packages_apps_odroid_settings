package com.hardkernel.odroid.settings.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CpuReceiver extends BroadcastReceiver {
    private final String TAG = "CpuReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            CPU cpu;
            SharedPreferences pref = context.getSharedPreferences("cpu", Context.MODE_PRIVATE);
            cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
            cpu.governor.set(pref.getString("little_core_governor", cpu.governor.getCurrent()));
            cpu.frequency.setScalingMax(pref.getString("little_core_frequency", cpu.frequency.getScalingCurrent()));

            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
            cpu.governor.set(pref.getString("big_core_governor", cpu.governor.getCurrent()));
            cpu.frequency.setScalingMax(pref.getString("big_core_frequency", cpu.frequency.getScalingCurrent()));
        }
    }
}
