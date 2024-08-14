package hardkernel.odroid.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfigEnv {
    private final static String TAG = "ConfigEnv";
    private final static String path = "/fat/config.ini";

    public static String getLittleCpuFreq() {
        String value = getValue("little_cpu_max_freq");
        if (value.length() > 4)
            return value;
        return value + "000";
    }

    public static String getMiddleCpuFreq() {
        String value = getValue("middle_cpu_max_freq");
        if (value.length() > 4)
            return value;
        return value + "000";
    }

    public static String getBigCpuFreq() {
        String value = getValue("big_cpu_max_freq");
        if (value.length() > 4)
            return value;
        return value + "000";
    }

    public static String getLittleCpuGovernor() {
        return getValue("little_cpu_governor");
    }

    public static String getMiddleCpuGovernor() {
        return getValue("middle_cpu_governor");
    }

    public static String getBigCpuGovernor() {
        return getValue("big_cpu_governor");
    }

    public static String getGpuFreq() {
        String value = getValue("gpu_max_freq");
        if (value.length() > 4)
            return value;
        return value + "000000";
    }

    public static String getGpuGovernor() {
        return getValue("gpu_governor");
    }

    public static String getNpuFreq() {
        String value = getValue("npu_max_freq");
        if (value.length() > 4)
            return value;
        return value + "000000";
    }

    public static String getNpuGovernor() {
        return getValue("npu_governor");
    }

    public static String getOverlay() {
        String overlays = getValue("overlays");

        if (overlays == null) {
            overlays = "";
            setOverlay(overlays);
        }

        return overlays;
    }

    public static String getOverlaySize() {
        String size = getValue("overlays_resize");

        if (size == null) {
            size = "16384";
            setOverlaySize(size);
        }

        return size;
    }

    private static String getValue(String keyWord) {
        return _getValue(keyWord + "=");
    }

    private static String _getValue(String startTerm) {
        File boot_ini = new File(path);
        if (boot_ini.exists()) {
            try {
                String line;
                FileReader fileReader = new FileReader(boot_ini);
                BufferedReader reader = new BufferedReader(fileReader);
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(startTerm)) {
                        return line.substring(line.indexOf("\"") +1,
                                line.lastIndexOf("\""));
                    }
                }
                reader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void setLittleCpuFreq(String freq) {
        setValue("little_cpu_max_freq", freq.substring(0, freq.length() - 3));
    }

    public static void setMiddleCpuFreq(String freq) {
        setValue("middle_cpu_max_freq", freq.substring(0, freq.length() - 3));
    }

    public static void setBigCpuFreq(String freq) {
        setValue("big_cpu_max_freq", freq.substring(0, freq.length() - 3));
    }

    public static void setLittleCpuGovernor(String governor) {
        setValue("little_cpu_governor", governor);
    }

    public static void setMiddleCpuGovernor(String governor) {
        setValue("middle_cpu_governor", governor);
    }

    public static void setBigCpuGovernor(String governor) {
        setValue("big_cpu_governor", governor);
    }

    public static void setGpuFreq(String freq) {
        setValue("gpu_max_freq", freq.substring(0, freq.length() - 6));
    }

    public static void setGpuGovernor(String governor) {
        setValue("gpu_governor", governor);
    }

    public static void setNpuFreq(String freq) {
        setValue("npu_max_freq", freq.substring(0, freq.length() - 6));
    }

    public static void setNpuGovernor(String governor) {
        setValue("npu_governor", governor);
    }

    public static void setOverlay(String overlay) {
        setValue("overlays", overlay);
    }

    public static void setOverlaySize(String size) {
        setValue("overlays_resize", size);
    }

    private static void setValue (String keyWord, String val) {
        _setValue(keyWord + "=", val);
    }

    private static void _setValue (String startTerm,String val) {
        boolean isSet = false;
        try {
            File boot_ini = new File(path);
            FileReader fileReader = new FileReader(boot_ini);
            BufferedReader reader = new BufferedReader(fileReader);

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(startTerm)) {
                    line = startTerm + "\"" + val + "\"";
                    isSet = true;
                }
                lines.add(line + "\n");
            }

            if (isSet == false) {
                line = startTerm + "\"" + val + "\"";
                lines.add(line + "\n");
            }

            fileReader.close();
            reader.close();

            FileWriter fileWriter = new FileWriter(boot_ini);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (String newline : lines)
                writer.write(newline);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
