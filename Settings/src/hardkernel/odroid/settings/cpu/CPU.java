package hardkernel.odroid.settings.cpu;

public class CPU {
    public enum Cluster {
        Big,
        Middle,
        Little
    }
    public Governor governor;
    public Frequency frequency;
    public Cluster cluster;

    private CPU (String tag, Cluster cluster) {
        this.cluster = cluster;
        governor = new Governor(tag, cluster);
        frequency = new Frequency(tag, cluster);
    }

    private static CPU cpu_big = null;
    private static CPU cpu_middle = null;
    private static CPU cpu_little = null;

    public static CPU getCPU(String tag, Cluster cluster) {
        switch (cluster) {
            case Big:
                if (cpu_big == null)
                    cpu_big = new CPU(tag, cluster);
                return cpu_big;
            case Middle:
                if (cpu_middle == null)
                    cpu_middle = new CPU(tag, cluster);
                return cpu_middle;
            case Little:
                if (cpu_little == null)
                    cpu_little = new CPU(tag, cluster);
                return cpu_little;
            default:
                return null;
        }
    }
}
