package hardkernel.odroid.settings.cpu.governor;

import android.os.Bundle;

import hardkernel.odroid.settings.cpu.CPU;

public class MiddleCoreGovernorFragment extends GovernorFragment {
    private static final String TAG = "MiddleCoreGovernorFragment";

    public static MiddleCoreGovernorFragment newInstance() { return new MiddleCoreGovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        cpu = CPU.getCPU(TAG, CPU.Cluster.Middle);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
