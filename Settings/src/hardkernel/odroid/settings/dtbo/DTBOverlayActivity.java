package hardkernel.odroid.settings.dtbo;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;
import hardkernel.odroid.settings.overlay.FlavorUtils;

public class DTBOverlayActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(DTBOverlayFragment.class.getName(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() { return new SettingsFragment(); }

        @Override
        public void onPreferenceStartInitialScreen() {
            final DTBOverlayFragment fragment = DTBOverlayFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
