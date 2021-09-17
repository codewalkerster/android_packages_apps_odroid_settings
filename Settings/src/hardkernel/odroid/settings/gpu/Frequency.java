package hardkernel.odroid.settings.gpu;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Frequency {
    private final static String MAX_FREQ = "/sys/class/devfreq/fde60000.gpu/max_freq";
    private final static String MIN_FREQ = "/sys/class/devfreq/fde60000.gpu/min_freq";
    private final static String CUR_FREQ = "/sys/class/devfreq/fde60000.gpu/cur_freq";
    private final static String AVAILABLE_FREQ = "/sys/class/devfreq/fde60000.gpu/available_frequencies";

    private static String TAG;

    private final int policyMax;
    private final int policyMin;

    public Frequency (String tag) {
        TAG = tag;
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
