package hardkernel.odroid.settings.cpu;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

/**
 * @author Luke.go
 */

public class CpuActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return CpuFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {
        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final CpuFragment fragment = CpuFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
