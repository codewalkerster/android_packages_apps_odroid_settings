package com.hardkernel.odroid.settings.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.util.DroidUtils;

public class CpuReceiver extends BroadcastReceiver {
    private final String TAG = "CpuReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            CPU cpu;
            cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
            cpu.governor.set(ConfigEnv.getLittleCoreGovernor());
            cpu.frequency.setScalingMax(ConfigEnv.getLittleCoreClock());

            if (DroidUtils.isOdroidN2()) {
                cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
                cpu.governor.set(ConfigEnv.getBigCoreGovernor());
                cpu.frequency.setScalingMax(ConfigEnv.getBigCoreClock());
            }
        }
    }
}