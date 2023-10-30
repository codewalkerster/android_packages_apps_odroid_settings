package hardkernel.odroid.settings.dtbo;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.TvSettingsActivity;
import hardkernel.odroid.settings.overlay.FlavorUtils;

public class DTBOSelectActivity extends TvSettingsActivity {
    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(DTBOSelectFragment.class.getName(), null);
    }
}
