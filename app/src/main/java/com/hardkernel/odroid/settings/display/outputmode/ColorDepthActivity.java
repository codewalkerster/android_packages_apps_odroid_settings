package com.hardkernel.odroid.settings.display.outputmode;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control Color Depth settings.
 */
public class ColorDepthActivity extends TvSettingsActivity {

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
            final ColorDepthFragment fragment = ColorDepthFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}


