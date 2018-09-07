package com.hardkernel.odroid.settings.update;

import com.hardkernel.odroid.settings.BaseSettingsFragment;
import com.hardkernel.odroid.settings.TvSettingsActivity;

import android.app.Fragment;
import android.content.Intent;

public class UpdateServerActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() { return SettingsFragment.newInstance(); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() { return new SettingsFragment(); }

        @Override
        public void onPreferenceStartInitialScreen() {
            final UpdateServerFragment fragment = UpdateServerFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
