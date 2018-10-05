package com.hardkernel.odroid.settings.cpu.frequency;

import android.os.Bundle;

import com.hardkernel.odroid.settings.cpu.CPU;

public class LittleCoreFrequencyFragment extends FrequencyFragment {
    private static final String TAG = "LittleCoreFrequencyFragment";

    public static LittleCoreFrequencyFragment newInstance() { return new LittleCoreFrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
