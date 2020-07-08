package com.hardkernel.odroid.settings.cpu;

import android.content.Context;
import android.util.Log;

import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.util.DroidUtils;

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
    private final static String BIG_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy2/scaling_max_freq";
    private final static String BIG_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy2/scaling_min_freq";
    /* Little cluster */
    private final static String LITTLE_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq";
    private final static String LITTLE_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq";

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

    public String[] getFrequencies(Context context) {
        String[] frequencies = (String[]) getScalingAvailables(context).toArray();

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
        }
    }

    private String getFreqFrom(String node) {
        String freq = null;

        FileReader fileReader;

        try {
            fileReader = new FileReader(node);

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

    private List<String> getScalingAvailables(Context context) {
        int id = 0;
        if (DroidUtils.isOdroidN2()) {
            if (DroidUtils.isOdroidN2Plus())
                id = (CPU.Cluster.Big == cluster)?
                        R.array.n2_plus_big
                        : R.array.n2_plus_little;
            else
                id = (CPU.Cluster.Big == cluster)?
                    R.array.n2_big
                    : R.array.n2_little;
        } else if (DroidUtils.isOdroidC4())
            id = R.array.c4;

        return Arrays.asList(context.getResources().getStringArray(id));
    }
}