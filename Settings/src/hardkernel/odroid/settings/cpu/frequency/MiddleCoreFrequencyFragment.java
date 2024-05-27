package hardkernel.odroid.settings.cpu.frequency;

import android.os.Bundle;

import hardkernel.odroid.settings.cpu.CPU;

public class MiddleCoreFrequencyFragment extends FrequencyFragment {
    private static final String TAG = "MiddleCoreFrequencyFragment";

    public static MiddleCoreFrequencyFragment newInstance() { return new MiddleCoreFrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Middle);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
