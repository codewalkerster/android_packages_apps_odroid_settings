package com.hardkernel.odroid.settings.cpungpu;

import android.os.Bundle;

import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.hardkernel.odroid.settings.EnvProperty;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.util.DroidUtils;

import com.hardkernel.odroid.settings.cpu.CPU;
import com.hardkernel.odroid.settings.gpu.GPU;

public class CpunGpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "CpunGpuFragment";

    private static final String KEY_BIG_CORE_CLOCK = "big_core_clock";
    private static final String KEY_BIG_CORE_GOVERNOR = "big_core_governor";
    private static final String KEY_LITTLE_CORE_CLOCK = "little_core_clock";
    private static final String KEY_LITTLE_CORE_GOVERNOR = "little_core_governor";
    private static final String KEY_SCALE_MODE = "gpu_scale_mode";

    private Preference bigCoreClockPref  = null;
    private Preference bigCoreGovernorPref = null;
    private Preference littleCoreClockPref  = null;
    private Preference littleCoreGovernorPref = null;
    private Preference sclaeModePref = null;

    private CPU cpu;
    private GPU gpu;

    public static CpunGpuFragment newInstance() {
        return new CpunGpuFragment();
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
        setPreferencesFromResource(R.xml.cpungpu, null);
        bigCoreClockPref = findPreference(KEY_BIG_CORE_CLOCK);
        bigCoreGovernorPref = findPreference(KEY_BIG_CORE_GOVERNOR);

        littleCoreClockPref = findPreference(KEY_LITTLE_CORE_CLOCK);
        littleCoreGovernorPref = findPreference(KEY_LITTLE_CORE_GOVERNOR);

        sclaeModePref = findPreference(KEY_SCALE_MODE);

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

        gpu = GPU.getGPU(TAG);
        gpu.initList(getContext());

        sclaeModePref.setSummary(gpu.getScaleMode());
    }
}
