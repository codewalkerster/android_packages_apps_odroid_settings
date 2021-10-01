package hardkernel.odroid.settings.cpu;

import hardkernel.odroid.settings.ConfigEnv;

public class CpuReceiver  {
    private final static String TAG = "CpuReceiver";

    public static void onReceive () {
        CPU cpu;
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

        cpu.governor.set(ConfigEnv.getCpuGovernor());
        cpu.frequency.setScalingMax(ConfigEnv.getCpuFreq());
    }
}
