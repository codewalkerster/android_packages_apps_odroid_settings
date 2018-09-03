package com.hardkernel.odroid.settings.cpu;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import com.hardkernel.odroid.settings.RadioPreference;

public class FrequencyFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "FrequencyFragment";

    public static FrequencyFragment newInstance() { return new FrequencyFragment(); }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        updatePreferenceFragment();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedFrequency = radioPreference.getKey();
                CPU cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
                cpu.frequency.setScalingMax(selectedFrequency);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        CPU cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.cpu_clock);
        setPreferenceScreen(screen);

        String[] frequencyList = cpu.frequency.getFrequencies();

        for (final String frequency : frequencyList) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(frequency);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(frequency);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (cpu.frequency.getScalingCurrent().equals(frequency)) {
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }
}
