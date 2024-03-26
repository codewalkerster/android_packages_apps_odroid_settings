package hardkernel.odroid.settings.display;

import androidx.fragment.app.Fragment;
import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

/**
 * @author GaoFei
 */
public class DisplayActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return DisplayFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final DisplayFragment fragment = DisplayFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
