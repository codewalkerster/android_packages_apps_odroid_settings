package hardkernel.odroid.settings.cpu;

import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.util.OdroidUtils;

public class CpuReceiver  {
    private final static String TAG = "CpuReceiver";

    public static void onReceive () {
        CPU cpu;
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

        cpu.governor.set(ConfigEnv.getLittleCpuGovernor());
        cpu.frequency.setScalingMax(ConfigEnv.getLittleCpuFreq());

        if (OdroidUtils.isOdroidM2()) {
            cpu = CPU.getCPU(TAG, CPU.Cluster.Middle);

            cpu.governor.set(ConfigEnv.getMiddleCpuGovernor());
            cpu.frequency.setScalingMax(ConfigEnv.getMiddleCpuFreq());

            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);

            cpu.governor.set(ConfigEnv.getBigCpuGovernor());
            cpu.frequency.setScalingMax(ConfigEnv.getBigCpuFreq());
        }
    }
}
