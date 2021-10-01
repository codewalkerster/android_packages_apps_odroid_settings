package hardkernel.odroid.settings.gpu;

import hardkernel.odroid.settings.ConfigEnv;

public class GpuReceiver  {
    private final static String TAG = "GpuReceiver";

    public static void onReceive () {
        GPU gpu;
        gpu = GPU.getGPU("gpu");

        gpu.governor.set(ConfigEnv.getGpuGovernor());
        gpu.frequency.setMax(ConfigEnv.getGpuFreq());
    }
}
