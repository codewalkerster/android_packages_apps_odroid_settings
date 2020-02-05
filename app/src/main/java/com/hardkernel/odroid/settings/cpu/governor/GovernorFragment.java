package com.hardkernel.odroid.settings.cpu.governor;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import com.hardkernel.odroid.settings.RadioPreference;
import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.cpu.CPU;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class GovernorFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GovernorFragment";
    public static CPU cpu = null;

    public static GovernorFragment newInstance() { return new GovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (cpu == null)
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        if (cpu.cluster == CPU.Cluster.Big)
            screen.setTitle(R.string.big_core_governor);
        else if (cpu.cluster == CPU.Cluster.Little)
            screen.setTitle(R.string.little_core_governor);
        else
            screen.setTitle(R.string.cpu);

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

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedGovernor = radioPreference.getKey();
                cpu.governor.set(selectedGovernor);
                saveGovernor(selectedGovernor);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveGovernor(String governor) {
        if (cpu.cluster == CPU.Cluster.Little)
            ConfigEnv.setLittleCoreGovernor(governor);
        else if (cpu.cluster == CPU.Cluster.Big)
            ConfigEnv.setBigCoreGovernor(governor);
    }
}