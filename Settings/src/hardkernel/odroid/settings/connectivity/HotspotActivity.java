package hardkernel.odroid.settings.connectivity;

import androidx.fragment.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

public class HotspotActivity extends TvSettingsActivity {

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
            final HotspotFragment fragment = HotspotFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
