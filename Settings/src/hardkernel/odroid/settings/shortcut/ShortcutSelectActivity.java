package hardkernel.odroid.settings.shortcut;

import android.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

public class ShortcutSelectActivity extends TvSettingsActivity {

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
            final ShortcutSelectFragment fragment = ShortcutSelectFragment.newInstance();
            startPreferenceFragment(fragment);
        }

    }
}
