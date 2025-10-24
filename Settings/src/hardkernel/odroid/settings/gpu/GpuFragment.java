/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package hardkernel.odroid.settings.gpu;

import android.os.Bundle;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;

public class GpuFragment extends SettingsPreferenceFragment {
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

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

}
