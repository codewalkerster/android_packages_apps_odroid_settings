package hardkernel.odroid.settings.gpu;

import hardkernel.odroid.settings.gpu.Governor;
import hardkernel.odroid.settings.gpu.Frequency;

public class GPU {
    public Governor governor;
    public Frequency frequency;

    private GPU (String name) {
        governor = new Governor(name);
        frequency = new Frequency(name);
    }

    private static GPU gpu = null;

    public static GPU getGPU(String name) {
        if (gpu == null)
            gpu = new GPU(name);
        return gpu;
    }
}
