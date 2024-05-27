package hardkernel.odroid.settings.cpu;

import android.os.Bundle;
import android.view.View;

import androidx.preference.Preference;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.util.OdroidUtils;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class CpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "CpuFragment";

    private static final String KEY_LITTLE_CORE_CLOCK = "little_core_clock";
    private static final String KEY_LITTLE_CORE_GOVERNOR = "little_core_governor";
    private static final String KEY_MIDDLE_CORE_CLOCK = "middle_core_clock";
    private static final String KEY_MIDDLE_CORE_GOVERNOR = "middle_core_governor";
    private static final String KEY_BIG_CORE_CLOCK = "big_core_clock";
    private static final String KEY_BIG_CORE_GOVERNOR = "big_core_governor";

	private Preference littleCoreClockPref  = null;
	private Preference littleCoreGovernorPref = null;
	private Preference middleCoreClockPref  = null;
	private Preference middleCoreGovernorPref = null;
	private Preference bigCoreClockPref  = null;
	private Preference bigCoreGovernorPref = null;

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

        middleCoreClockPref = findPreference(KEY_MIDDLE_CORE_CLOCK);
        middleCoreGovernorPref = findPreference(KEY_MIDDLE_CORE_GOVERNOR);

        bigCoreClockPref = findPreference(KEY_BIG_CORE_CLOCK);
        bigCoreGovernorPref = findPreference(KEY_BIG_CORE_GOVERNOR);
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

        if (OdroidUtils.isOdroidM2()) {
            /* Cluster 1 */
            cpu = CPU.getCPU(TAG, CPU.Cluster.Middle);

            currentClock = cpu.frequency.getScalingCurrent();
            currentGovernor = cpu.governor.getCurrent();

            middleCoreClockPref.setSummary(currentClock);
            middleCoreGovernorPref.setSummary(currentGovernor);

            /* Cluster 2 */
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);

            currentClock = cpu.frequency.getScalingCurrent();
            currentGovernor = cpu.governor.getCurrent();

            bigCoreClockPref.setSummary(currentClock);
            bigCoreGovernorPref.setSummary(currentGovernor);
        } else {
            middleCoreClockPref.setVisible(false);
            middleCoreGovernorPref.setVisible(false);

            bigCoreClockPref.setVisible(false);
            bigCoreGovernorPref.setVisible(false);
        }
    }
}
