package hardkernel.odroid.settings.npu;

import hardkernel.odroid.settings.npu.Governor;
import hardkernel.odroid.settings.npu.Frequency;

public class NPU {
    public Governor governor;
    public Frequency frequency;

    private NPU (String name) {
        governor = new Governor(name);
        frequency = new Frequency(name);
    }

    private static NPU npu = null;

    public static NPU getNPU(String name) {
        if (npu == null)
            npu = new NPU(name);
        return npu;
    }
}
