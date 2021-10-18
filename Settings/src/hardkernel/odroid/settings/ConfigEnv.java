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
    private final static String path = "/fat/env.ini";

    public static String getCpuFreq() {
        return getValue("cpu_max_freq");
    }

    public static String getCpuGovernor() {
        return getValue("cpu_governor");
    }

    public static String getGpuFreq() {
        return getValue("gpu_max_freq");
    }

    public static String getGpuGovernor() {
        return getValue("gpu_governor");
    }

    public static String getHeartBeat() {
        return getValue("heartbeat");
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

    public static void setCpuFreq(String freq) {
        setValue("cpu_max_freq", freq);
    }

    public static void setCpuGovernor(String governor) {
        setValue("cpu_governor", governor);
    }

    public static void setGpuFreq(String freq) {
        setValue("gpu_max_freq", freq);
    }

    public static void setGpuGovernor(String governor) {
        setValue("gpu_governor", governor);
    }

    public static void setHeartBeat(String mode) {
        setValue("heartbeat", mode);
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
