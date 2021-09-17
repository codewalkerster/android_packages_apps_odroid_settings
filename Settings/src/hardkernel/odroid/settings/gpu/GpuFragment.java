package hardkernel.odroid.settings.gpu;

import android.os.Bundle;

import androidx.preference.Preference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class GpuFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GpuFragment";

    private static final String KEY_GPU_CLOCK = "gpu_clock";
    private static final String KEY_GPU_GOVERNOR = "gpu_governor";

	private Preference gpuClockPref  = null;
	private Preference gpuGovernorPref = null;

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
        gpuClockPref = findPreference(KEY_GPU_CLOCK);
        gpuGovernorPref = findPreference(KEY_GPU_GOVERNOR);

        refreshStatus();
    }

    private void refreshStatus() {
        String currentClock;
        String currentGovernor;

        gpu = GPU.getGPU(TAG);

        currentClock = gpu.frequency.getMax();
        currentGovernor = gpu.governor.getCurrent();

        gpuClockPref.setSummary(currentClock);
        gpuGovernorPref.setSummary(currentGovernor);
    }
}
