package com.hardkernel.odroid.settings.cpu.governor;

import android.os.Bundle;

import com.hardkernel.odroid.settings.cpu.CPU;

public class BigCoreGovernorFragment extends GovernorFragment {
    private static final String TAG = "BigCoreGovernorFragment";

    public static BigCoreGovernorFragment newInstance() { return new BigCoreGovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
