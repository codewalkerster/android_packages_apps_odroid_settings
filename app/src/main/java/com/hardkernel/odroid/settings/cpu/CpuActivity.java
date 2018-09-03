package com.hardkernel.odroid.settings.cpu;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control the CPU settings.
 */
public class CpuActivity extends TvSettingsActivity {
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
			final CpuFragment fragment = CpuFragment.newInstance();
			startPreferenceFragment(fragment);
		}
	}
}
