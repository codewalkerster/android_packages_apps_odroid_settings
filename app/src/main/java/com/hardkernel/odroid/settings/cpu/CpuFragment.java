package com.hardkernel.odroid.settings.cpu;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.hardkernel.odroid.settings.R;


public class CpuFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "CpuFragment";

	private static final String KEY_CPU_CLOCK = "cpu_clock";
	private static final String KEY_CPU_GOVERNOR = "cpu_governor";

	private Preference cpuClockPref  = null;
	private Preference cpuGovernorPref = null;

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

		cpuClockPref = findPreference(KEY_CPU_CLOCK);
		cpuGovernorPref = findPreference(KEY_CPU_GOVERNOR);

		refreshStatus();
    }

	private void refreshStatus() {
        /* Little Cluster */
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

        String currentClock = cpu.frequency.getScalingCurrent();
        String currentGovernor = cpu.governor.getCurrent();

		cpuClockPref.setSummary(currentClock);
		cpuGovernorPref.setSummary(currentGovernor);
	}
}
