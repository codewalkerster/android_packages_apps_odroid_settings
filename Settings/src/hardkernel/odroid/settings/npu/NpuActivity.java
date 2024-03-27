package hardkernel.odroid.settings.npu;

import androidx.fragment.app.Fragment;
import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

/**
 * @author Luke.go
 */

public class NpuActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return NpuFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {
        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final NpuFragment fragment = NpuFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
