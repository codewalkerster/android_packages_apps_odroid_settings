package com.hardkernel.odroid.settings.cpu.frequency;

import android.os.Bundle;

import com.hardkernel.odroid.settings.cpu.CPU;

public class BigCoreFrequencyFragment extends FrequencyFragment {
    private static final String TAG = "BigCoreFrequencyFragment";

    public static BigCoreFrequencyFragment newInstance() { return new BigCoreFrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
