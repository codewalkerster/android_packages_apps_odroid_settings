package com.hardkernel.odroid.settings.cpu;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Frequency {
    /* Big cluster */
    private final static String BIG_SCALING_AVAILABLE_FREQ = "/sys/devices/system/cpu/cpufreq/policy2/scaling_available_frequencies";
    private final static String BIG_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy2/scaling_max_freq";
    private final static String BIG_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy2/scaling_min_freq";
    /* Little cluster */
    private final static String LITTLE_SCALING_AVAILABLE_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies";
    private final static String LITTLE_SCALING_MAX_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq";
    private final static String LITTLE_SCALING_MIN_FREQ = "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq";

    private static String TAG;
    private CPU.Cluster cluster;

    public Frequency (String tag, CPU.Cluster cluster) {
        TAG = tag;
        this.cluster = cluster;
    }

    public int getPolicyMin() {
        FileReader reader;
        String minFreq;
        try {
            switch (cluster) {
                case Big:
                    reader = new FileReader(BIG_SCALING_MIN_FREQ);
                    break;
                case Little:
                    reader = new FileReader(LITTLE_SCALING_MIN_FREQ);
                    break;
                default:
                    reader = new FileReader(BIG_SCALING_MIN_FREQ);
            }
            BufferedReader bufferedReader = new BufferedReader(reader);
            minFreq = bufferedReader.readLine();
        } catch (Exception e) {
            minFreq = "667000";
        }

        return Integer.parseInt(minFreq);
    }

    public String[] getFrequencies() {
        String available_frequencies = getScalingAvailables();
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
        FileReader fileReader;

        try {
            switch (cluster) {
                case Big:
                    fileReader = new FileReader(BIG_SCALING_MAX_FREQ);
                    break;
                case Little:
                    fileReader = new FileReader(LITTLE_SCALING_MAX_FREQ);
                    break;
                default:
                    fileReader = new FileReader(BIG_SCALING_MAX_FREQ);
                    break;
            }

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            freq = bufferedReader.readLine();
            bufferedReader.close();
            Log.e(TAG, freq);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
                    fileWriter = new FileWriter(BIG_SCALING_MAX_FREQ);
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

    private String getScalingAvailables() {
        String available_frequencies = null;
        try {
            FileReader fileReader;

            switch (cluster) {
                case Big:
                    fileReader = new FileReader(BIG_SCALING_AVAILABLE_FREQ);
                    break;
                case Little:
                    fileReader = new FileReader(LITTLE_SCALING_AVAILABLE_FREQ);
                    break;
                default:
                    fileReader = new FileReader(BIG_SCALING_AVAILABLE_FREQ);
                    break;
            }

            BufferedReader bufferedReader= new BufferedReader(fileReader);
            available_frequencies = bufferedReader.readLine();
            bufferedReader.close();
            Log.e(TAG, available_frequencies);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return available_frequencies;
    }

}
