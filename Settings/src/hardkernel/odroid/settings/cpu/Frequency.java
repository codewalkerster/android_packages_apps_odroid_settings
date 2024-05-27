package hardkernel.odroid.settings.cpu;

import android.content.Context;
import android.util.Log;
import hardkernel.odroid.settings.R;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Frequency {
    /* Big cluster */
    private final static String BIG_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy6/scaling_max_freq";
    private final static String BIG_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy6/scaling_min_freq";
    private final static String BIG_SCALING_AVAIL_FREQ = "/sys/devices/system/cpu/cpufreq/policy6/scaling_available_frequencies";
    /* Middle cluster */
    private final static String MIDDLE_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq";
    private final static String MIDDLE_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq";
    private final static String MIDDLE_SCALING_AVAIL_FREQ = "/sys/devices/system/cpu/cpufreq/policy4/scaling_available_frequencies";
    /* Little cluster */
    private final static String LITTLE_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq";
    private final static String LITTLE_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq";
    private final static String LITTLE_SCALING_AVAIL_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies";

    private static String TAG;
    private CPU.Cluster cluster;

    private final int policyMax;

    public Frequency (String tag, CPU.Cluster cluster) {
        TAG = tag;
        this.cluster = cluster;
        switch (cluster) {
            case Big:
                policyMax = Integer.parseInt(getFreqFrom(BIG_SCALING_MAX_FREQ));
                break;
            case Middle:
                policyMax = Integer.parseInt(getFreqFrom(MIDDLE_SCALING_MAX_FREQ));
                break;
            case Little:
                policyMax = Integer.parseInt(getFreqFrom(LITTLE_SCALING_MAX_FREQ));
                break;
            default:
                policyMax = Integer.parseInt(getFreqFrom(LITTLE_SCALING_MAX_FREQ));
        }
    }

    public int getPolicyMax() {
        return policyMax;
    }

    public int getPolicyMin() {
        String minFreq;

        switch (cluster) {
            case Big:
                minFreq = getFreqFrom(BIG_SCALING_MIN_FREQ);
                break;
            case Middle:
                minFreq = getFreqFrom(MIDDLE_SCALING_MIN_FREQ);
                break;
            case Little:
                minFreq = getFreqFrom(LITTLE_SCALING_MIN_FREQ);
                break;
            default:
                minFreq = getFreqFrom(LITTLE_SCALING_MIN_FREQ);
        }
        if (minFreq == null)
            minFreq = "667000";

        return Integer.parseInt(minFreq);
    }

    public String[] getFrequencies() {
        String available_frequencies;

        switch (cluster) {
            case Big:
                available_frequencies = getScalingAvailables(BIG_SCALING_AVAIL_FREQ);
                break;
            case Middle:
                available_frequencies = getScalingAvailables(MIDDLE_SCALING_AVAIL_FREQ);
                break;
            case Little:
                available_frequencies = getScalingAvailables(LITTLE_SCALING_AVAIL_FREQ);
                break;
            default:
                available_frequencies = getScalingAvailables(LITTLE_SCALING_AVAIL_FREQ);
                break;
        }

        String[] frequencies = available_frequencies.split(" ");

        Arrays.sort(frequencies, new Comparator<String>() {
            @Override
            public int compare(String num1, String num2) {
                return Integer.valueOf(num2) - Integer.valueOf(num1);
            }
        });

        return frequencies;
    }

    public String getScalingCurrent() {
        String freq = null;

        switch (cluster) {
            case Big:
                freq = getFreqFrom(BIG_SCALING_MAX_FREQ);
                break;
            case Middle:
                freq = getFreqFrom(MIDDLE_SCALING_MAX_FREQ);
                break;
            case Little:
                freq = getFreqFrom(LITTLE_SCALING_MAX_FREQ);
                break;
            default:
                freq = getFreqFrom(LITTLE_SCALING_MAX_FREQ);
                break;
        }

        return freq;
    }

    public void setScalingMax(String freq) {
        BufferedWriter out;
        FileWriter fileWriter;

        try {
            switch (cluster) {
                case Big:
                    fileWriter = new FileWriter(BIG_SCALING_MAX_FREQ);
                    break;
                case Middle:
                    fileWriter = new FileWriter(MIDDLE_SCALING_MAX_FREQ);
                    break;
                case Little:
                    fileWriter = new FileWriter(LITTLE_SCALING_MAX_FREQ);
                    break;
                default:
                    fileWriter = new FileWriter(LITTLE_SCALING_MAX_FREQ);
                    break;
            }

            out = new BufferedWriter(fileWriter);
            out.write(freq);
            out.newLine();
            out.close();
            Log.e(TAG, "set freq : " + freq);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "set by null value. Check your config.ini");
        }
    }

    private String getFreqFrom(String node) {
        String freq = null;
        try {
            FileReader fileReader = new FileReader(node);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            freq = bufferedReader.readLine();
            bufferedReader.close();
            Log.e(TAG, node + " " + freq);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return freq;
    }

    private String getScalingAvailables(String node) {
        String available_frequencies = null;
        try {
            FileReader fileReader = new FileReader(node);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            available_frequencies = bufferedReader.readLine();
            bufferedReader.close();

            Log.d(TAG, available_frequencies);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return available_frequencies;
    }
}
