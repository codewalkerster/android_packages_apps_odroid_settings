package com.hardkernel.odroid.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class bootini {
    final static String path = "/odm/boot.ini";

    public static String getBigCoreClock() {
        return getValue("setenv max_freq_a73 ") + "000";
    }

    public static String getLittleCoreClock() {
        return getValue("setenv max_freq_a53 ") + "000";
    }

    public static String getBigCoreGovernor() {
        return getValue("setenv governor_a73 ");
    }

    public static String getLittleCoreGovernor() {
        return getValue("setenv governor_a53 ");
    }

    /** Not used,
     *  It works on systemcontrol. read from /proc/cmdline
     */
    public static String getHdmiMode() {
        return getValue("setenv hdmimode ");
    }

    public static int getDisplayZoomrate() {
        return Integer.parseInt(getValue("setenv zoom_rate "));
    }

    private static String getValue(String startTerm) {
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

    public static void setBigCoreFreq(String freq) {
        setValue("setenv max_freq_a73 ", freq.substring(0, freq.length()-3));
    }

    public static void setLittleCoreFreq(String freq) {
        setValue("setenv max_freq_a53 ", freq.substring(0, freq.length()-3));
    }

    public static void setBigCoreGovernor(String governor) {
        setValue("setenv governor_a73 ", governor);
    }

    public static void setLittleCoreGovernor(String governor) {
        setValue("setenv governor_a53 ", governor);
    }

    public static void setHdmiMode(String mode) {
        setValue("setenv hdmimode ", mode);
    }

    public static void setDisplayZoom(int rate) {
        setValue("setenv zoom_rate ", String.valueOf(rate));
    }

    private static void setValue (String startTerm,String val) {
        try {
            File boot_ini = new File(path);
            FileReader fileReader = new FileReader(boot_ini);
            BufferedReader reader = new BufferedReader(fileReader);

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(startTerm))
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
