package com.hardkernel.odroid.settings.cpu.governor;

import android.os.Bundle;

import com.hardkernel.odroid.settings.cpu.CPU;

public class LittleCoreGovernorFragment extends GovernorFragment {
    private static final String TAG = "LittleCoreGovernorFragment";

    public static LittleCoreGovernorFragment newInstance() { return new LittleCoreGovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }

}
