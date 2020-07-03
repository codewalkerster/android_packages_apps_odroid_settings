package com.hardkernel.odroid.settings.gpu;

import com.hardkernel.odroid.settings.ConfigEnv;

public class GpuReceiver {
    private final static String TAG = "GpuReceiver";

    public static void onReceive() {
        GPU gpu = GPU.getGPU(TAG);

        gpu.apply();
    }
}