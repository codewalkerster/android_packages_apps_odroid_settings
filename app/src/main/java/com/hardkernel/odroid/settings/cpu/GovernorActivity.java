package com.hardkernel.odroid.settings.cpu;

import android.app.Fragment;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

public class GovernorActivity extends TvSettingsActivity{
    @Override
    protected Fragment createSettingsFragment() { return SettingsFragment.newInstance(); }

    public static class SettingsFragment extends BaseSettingsFragment {
        public static SettingsFragment newInstance() { return new SettingsFragment(); }

        @Override
        public void onPreferenceStartInitialScreen() {
            final GovernorFragment fragment = GovernorFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
