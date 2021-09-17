package hardkernel.odroid.settings.gpu;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

import hardkernel.odroid.settings.gpu.GovernorFragment;

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
