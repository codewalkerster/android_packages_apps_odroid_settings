package com.hardkernel.odroid.settings.cpu.frequency;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.widget.Toast;

import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

import com.hardkernel.odroid.settings.RadioPreference;
import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.cpu.CPU;
import com.hardkernel.odroid.settings.util.DroidUtils;

public class FrequencyFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "FrequencyFragment";
    public static CPU cpu = null;

    public static FrequencyFragment newInstance() { return new FrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (cpu == null) {
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        }
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        if (cpu.cluster == CPU.Cluster.Big)
            screen.setTitle(R.string.big_core_clock);
        else if (cpu.cluster == CPU.Cluster.Little)
            screen.setTitle(R.string.little_core_clock);
        else
            screen.setTitle(R.string.cpu);

        setPreferenceScreen(screen);

        String[] frequencyList = cpu.frequency.getFrequencies(getContext());

        for (final String frequency : frequencyList) {
            if (Integer.parseInt(frequency) < cpu.frequency.getPolicyMin())
                continue;

            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(frequency);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(getFrequencyTitle(frequency));
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (cpu.frequency.getScalingCurrent().equals(frequency)) {
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }

    private String getFrequencyTitle(String frequency) {
        if (cpu.cluster == CPU.Cluster.Big) {
            if(DroidUtils.isOdroidN2Plus()) {
                if (Integer.valueOf(frequency) > 2208000)
                    return frequency + " (Overclocking)";
                else
                    return frequency;
            }
            if(DroidUtils.isOdroidN2()) {
                if(Integer.valueOf(frequency) > 1800000)
                    return frequency + " (Overclocking)";
                else
                    return frequency;
            }
        } else { //Little
            if(DroidUtils.isOdroidN2Plus()) {
                if (Integer.valueOf(frequency) > 1908000)
                    return frequency + " (Overclocking)";
                else
                    return frequency;
            }
            if(DroidUtils.isOdroidN2()) {
                if(Integer.valueOf(frequency) > 1896000)
                    return frequency + " (Overclocking)";
                else
                    return frequency;
            }
        }
        return frequency;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedFrequency = radioPreference.getKey();
                if (cpu.frequency.getPolicyMax()
                        < Integer.valueOf(selectedFrequency)) {
                    Toast.makeText(getContext(),
                            "Selected Clock will be applied after reboot",
                            Toast.LENGTH_LONG).show();
                }
                cpu.frequency.setScalingMax(selectedFrequency);
                saveFrequency(selectedFrequency);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveFrequency(String frequency) {
        if (cpu.cluster == CPU.Cluster.Little)
            ConfigEnv.setLittleCoreFreq(frequency);
        else if (cpu.cluster == CPU.Cluster.Big)
            ConfigEnv.setBigCoreFreq(frequency);
    }
}