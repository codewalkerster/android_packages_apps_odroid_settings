package hardkernel.odroid.settings.cpu;

import android.os.Bundle;

import androidx.preference.Preference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class CpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "CpuFragment";

    private static final String KEY_LITTLE_CORE_CLOCK = "little_core_clock";
    private static final String KEY_LITTLE_CORE_GOVERNOR = "little_core_governor";

	private Preference littleCoreClockPref  = null;
	private Preference littleCoreGovernorPref = null;

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
        littleCoreClockPref = findPreference(KEY_LITTLE_CORE_CLOCK);
        littleCoreGovernorPref = findPreference(KEY_LITTLE_CORE_GOVERNOR);

        refreshStatus();
    }

    private void refreshStatus() {
        String currentClock;
        String currentGovernor;

        /* Cluster 0 */
        cpu = CPU.getCPU(TAG, CPU.Cluster.Little);

        currentClock = cpu.frequency.getScalingCurrent();
        currentGovernor = cpu.governor.getCurrent();

        littleCoreClockPref.setSummary(currentClock);
        littleCoreGovernorPref.setSummary(currentGovernor);
    }
}
