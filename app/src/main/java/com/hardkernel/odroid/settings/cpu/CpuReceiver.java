package com.hardkernel.odroid.settings.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hardkernel.odroid.settings.bootini;

public class CpuReceiver extends BroadcastReceiver {
    private final String TAG = "CpuReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            CPU cpu;
            cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
            cpu.governor.set(bootini.getLittleCoreGovernor());
            cpu.frequency.setScalingMax(bootini.getLittleCoreClock());

            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
            cpu.governor.set(bootini.getBigCoreGovernor());
            cpu.frequency.setScalingMax(bootini.getBigCoreClock());
        }
    }
}