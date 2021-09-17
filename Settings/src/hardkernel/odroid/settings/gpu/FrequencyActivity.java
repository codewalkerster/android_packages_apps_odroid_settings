package hardkernel.odroid.settings.gpu;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

import hardkernel.odroid.settings.gpu.FrequencyFragment;

public class FrequencyActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() { return SettingsFragment.newInstance(); }

    public static class SettingsFragment extends BaseSettingsFragment {
        public static SettingsFragment newInstance() { return new SettingsFragment(); }

        @Override
        public void onPreferenceStartInitialScreen() {
            final FrequencyFragment fragment = FrequencyFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
