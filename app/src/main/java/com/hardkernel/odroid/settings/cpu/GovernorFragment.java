package com.hardkernel.odroid.settings.cpu;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import com.hardkernel.odroid.settings.RadioPreference;

public class GovernorFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "GovernorFragment";

    public static GovernorFragment newInstance() { return new GovernorFragment(); }

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
                String selectedGovernor = radioPreference.getKey();
                CPU cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
                cpu.governor.set(selectedGovernor);
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

        screen.setTitle(R.string.cpu_governor);
        setPreferenceScreen(screen);

        String[] governorList = cpu.governor.getGovernors();

        for (final String governor : governorList) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(governor);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(governor);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (cpu.governor.getCurrent().equals(governor)) {
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }
}
