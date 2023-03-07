package com.hardkernel.odroid.settings.cpungpu;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control the CPU and GPU settings.
 */
public class CpunGpuActivity extends TvSettingsActivity {
	@Override
	protected Fragment createSettingsFragment() {
		return SettingsFragment.newInstance();
	}

	public static class SettingsFragment extends BaseSettingsFragment {

		public static SettingsFragment newInstance() {
			return new SettingsFragment();
		}

		@Override
		public void onPreferenceStartInitialScreen() {
			final CpunGpuFragment fragment = CpunGpuFragment.newInstance();
			startPreferenceFragment(fragment);
		}
	}
}
