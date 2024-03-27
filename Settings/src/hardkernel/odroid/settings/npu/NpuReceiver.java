package hardkernel.odroid.settings.npu;

import hardkernel.odroid.settings.ConfigEnv;

public class NpuReceiver  {
    private final static String TAG = "NpuReceiver";

    public static void onReceive () {
        NPU npu;
        npu = NPU.getNPU("npu");

        npu.governor.set(ConfigEnv.getNpuGovernor());
        npu.frequency.setMax(ConfigEnv.getNpuFreq());
    }
}
