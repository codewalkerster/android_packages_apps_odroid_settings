package com.hardkernel.odroid.settings;

import android.app.Fragment;

public class IPSettingsActivity extends TvSettingsActivity {
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
            final IPSettingsFragment fragment = IPSettingsFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
