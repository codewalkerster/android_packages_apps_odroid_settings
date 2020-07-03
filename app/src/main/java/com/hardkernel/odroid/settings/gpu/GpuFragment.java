package com.hardkernel.odroid.settings.gpu;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

public class GpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GpuFragment";

    private static final String KEY_SCALE_MODE = "gpu_scale_mode";

    private Preference sclaeModePref = null;
    private GPU gpu;

    public static GpuFragment newInstance() {
        return new GpuFragment();
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
        setPreferencesFromResource(R.xml.gpu, null);
        sclaeModePref = findPreference(KEY_SCALE_MODE);

        refreshStatus();
    }

    private void refreshStatus() {
        gpu = GPU.getGPU(TAG);
        gpu.initList(getContext());

        sclaeModePref.setSummary(gpu.getScaleMode());
    }
}
