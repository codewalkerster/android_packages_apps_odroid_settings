package com.hardkernel.odroid.settings;

import android.app.Fragment;

public class HeartBeatActivity extends TvSettingsActivity {
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
            final HeartBeatFragment fragment = HeartBeatFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
