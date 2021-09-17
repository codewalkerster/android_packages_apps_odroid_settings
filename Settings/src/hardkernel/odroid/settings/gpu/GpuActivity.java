package hardkernel.odroid.settings.gpu;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

/**
 * @author Luke.go
 */

public class GpuActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return GpuFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {
        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final GpuFragment fragment = GpuFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
