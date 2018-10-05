package com.hardkernel.odroid.settings.cpu.frequency;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;

import com.hardkernel.odroid.settings.R;

import com.hardkernel.odroid.settings.RadioPreference;
import com.hardkernel.odroid.settings.cpu.CPU;

public class FrequencyFragment extends LeanbackPreferenceFragment {
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

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedFrequency = radioPreference.getKey();
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
        Context context = getContext();
        String target;
        if (cpu.cluster == CPU.Cluster.Little)
            target = "little_core";
        else if (cpu.cluster == CPU.Cluster.Big)
            target = "big_core";
        else
            target = null;
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("cpu", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(target+"_frequency", frequency);
        editor.commit();
    }
}
