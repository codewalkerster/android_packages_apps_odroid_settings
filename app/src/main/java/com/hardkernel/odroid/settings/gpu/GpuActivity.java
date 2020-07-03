package com.hardkernel.odroid.settings.gpu;
import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control the GPU settings.
 */
public class GpuActivity extends TvSettingsActivity {
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
            final GpuFragment fragment = GpuFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}