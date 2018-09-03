package com.hardkernel.odroid.settings.rotation;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;

public class RotationActivity extends TvSettingsActivity{
    @Override
    protected Fragment createSettingsFragment() { return SettingsFragment.newInstance(); }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() { return new SettingsFragment(); }

        @Override
        public void onPreferenceStartInitialScreen() {
            final RotationFragment fragment = RotationFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
