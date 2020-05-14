package com.hardkernel.odroid.settings.cpu;

import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.util.DroidUtils;

public class CpuReceiver  {
    private final static String TAG = "CpuReceiver";

    public static void onReceive () {
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