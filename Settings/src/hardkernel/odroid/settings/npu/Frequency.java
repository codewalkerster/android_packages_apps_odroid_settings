package hardkernel.odroid.settings.npu;

import android.util.Log;

import hardkernel.odroid.settings.OdroidSettingsApplication;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.util.OdroidUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Frequency {
    private String MAX_FREQ;
    private String MIN_FREQ;
    private String CUR_FREQ;
    private String AVAILABLE_FREQ;

    private static String TAG;

    private final int policyMax;
    private final int policyMin;

    public Frequency (String tag) {
        TAG = tag;
        init_sys_node();
        policyMax = Integer.parseInt(getFreqFrom(MAX_FREQ));
        policyMin = Integer.parseInt(getFreqFrom(MIN_FREQ));
    }

    public int getPolicyMax() {
        return policyMax;
    }

    public int getPolicyMin() {
        return policyMin;
    }

    public String[] getFrequencies() {
        String available_frequencies = getAvailables();
        String[] frequencies = available_frequencies.split(" ");

        Arrays.sort(frequencies, new Comparator<String>() {
            @Override
            public int compare(String num1, String num2) {
                return Integer.valueOf(num2) - Integer.valueOf(num1);
            }
        });

        return frequencies;
    }

    public String getCurrent() {
        return getFreqFrom(CUR_FREQ);
    }

    public String getMax() {
        return getFreqFrom(MAX_FREQ);
    }

    public void setMax(String freq) {
        try {
            FileWriter fileWriter = new FileWriter(MAX_FREQ);
            BufferedWriter out = new BufferedWriter(fileWriter);

            out.write(freq);
            out.newLine();
            out.close();
            Log.d(TAG, "set freq : " + freq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init_sys_node() {
        String sys_node;
        if (OdroidUtils.isOdroidM2()) {
            sys_node = OdroidSettingsApplication
                .getRes().getString(R.string.m2_npu);
        } else {
            sys_node = "/sys/class/devfreq/fdab0000.npu";
        }
            MAX_FREQ = sys_node + "/max_freq";
            MIN_FREQ = sys_node + "/min_freq";
            CUR_FREQ = sys_node + "/cur_freq";
            AVAILABLE_FREQ = sys_node + "/available_frequencies";
    }

    private String getFreqFrom(String node) {
        String freq = null;

        try {
            FileReader fileReader = new FileReader(node);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            freq = bufferedReader.readLine();
            bufferedReader.close();

            Log.d(TAG, node + " " + freq);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return freq;
    }

    private String getAvailables() {
        String available_frequencies = null;
        try {
            FileReader fileReader = new FileReader(AVAILABLE_FREQ);
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
