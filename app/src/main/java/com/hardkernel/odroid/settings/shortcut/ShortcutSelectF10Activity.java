package com.hardkernel.odroid.settings.shortcut;

import android.app.Fragment;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

public class ShortcutSelectF10Activity extends TvSettingsActivity {
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
            final ShortcutF10SelectFragment fragment = ShortcutF10SelectFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
