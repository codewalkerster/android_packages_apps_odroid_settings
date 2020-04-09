package com.hardkernel.odroid.settings.cpu;

import android.os.Bundle;

import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.hardkernel.odroid.settings.EnvProperty;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.util.DroidUtils;

public class CpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "CpuFragment";

    private static final String KEY_BIG_CORE_CLOCK = "big_core_clock";
	private static final String KEY_BIG_CORE_GOVERNOR = "big_core_governor";
	private static final String KEY_LITTLE_CORE_CLOCK = "little_core_clock";
	private static final String KEY_LITTLE_CORE_GOVERNOR = "little_core_governor";

	private Preference bigCoreClockPref  = null;
	private Preference bigCoreGovernorPref = null;
	private Preference littleCoreClockPref  = null;
	private Preference littleCoreGovernorPref = null;

	private CPU cpu;

    public static CpuFragment newInstance() {
        return new CpuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.cpu, null);
        bigCoreClockPref = findPreference(KEY_BIG_CORE_CLOCK);
        bigCoreGovernorPref = findPreference(KEY_BIG_CORE_GOVERNOR);

		littleCoreClockPref = findPreference(KEY_LITTLE_CORE_CLOCK);
		littleCoreGovernorPref = findPreference(KEY_LITTLE_CORE_GOVERNOR);

		refreshStatus();
    }

	private void refreshStatus() {
        String currentClock;
        String currentGovernor;
        if (DroidUtils.isOdroidC4()) {
            bigCoreClockPref.setVisible(false);
            bigCoreGovernorPref.setVisible(false);
        }
        if (DroidUtils.isOdroidN2()) {
            /* Big Cluster */
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);

            currentClock = cpu.frequency.getScalingCurrent();
            currentGovernor = cpu.governor.getCurrent();

            bigCoreClockPref.setSummary(currentClock);
            bigCoreGovernorPref.setSummary(currentGovernor);
        }
        /* Little Cluster */
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

        currentClock = cpu.frequency.getScalingCurrent();
        currentGovernor = cpu.governor.getCurrent();

		littleCoreClockPref.setSummary(currentClock);
		littleCoreGovernorPref.setSummary(currentGovernor);
	}
}
