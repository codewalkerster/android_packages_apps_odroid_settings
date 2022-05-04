package hardkernel.odroid.settings.camera;
import android.app.Fragment;
import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;

public class CameraActivity extends TvSettingsActivity {
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
            final CameraFragment fragment = CameraFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
