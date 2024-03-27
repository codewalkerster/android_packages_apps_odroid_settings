package hardkernel.odroid.settings.npu;

import android.os.Bundle;

import androidx.preference.Preference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class NpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "NpuFragment";

    private static final String KEY_NPU_CLOCK = "npu_clock";
    private static final String KEY_NPU_GOVERNOR = "npu_governor";

	private Preference npuClockPref  = null;
	private Preference npuGovernorPref = null;

	private NPU npu;

    public static NpuFragment newInstance() {
        return new NpuFragment();
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
        setPreferencesFromResource(R.xml.npu, null);
        npuClockPref = findPreference(KEY_NPU_CLOCK);
        npuGovernorPref = findPreference(KEY_NPU_GOVERNOR);

        refreshStatus();
    }

    private void refreshStatus() {
        String currentClock;
        String currentGovernor;

        npu = NPU.getNPU(TAG);

        currentClock = npu.frequency.getMax();
        currentGovernor = npu.governor.getCurrent();

        npuClockPref.setSummary(currentClock);
        npuGovernorPref.setSummary(currentGovernor);
    }
}
