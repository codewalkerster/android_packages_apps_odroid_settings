package hardkernel.odroid.settings.shortcut;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

public class ShortcutSelectF8Activity extends TvSettingsActivity {
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
            final ShortcutF8SelectFragment fragment = ShortcutF8SelectFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
