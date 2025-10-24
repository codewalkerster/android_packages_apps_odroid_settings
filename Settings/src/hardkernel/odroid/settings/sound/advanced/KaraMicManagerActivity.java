package hardkernel.odroid.settings.soundeffect;

import androidx.fragment.app.Fragment;

import hardkernel.odroid.settings.TvSettingsActivity;
import hardkernel.odroid.settings.overlay.FlavorUtils;

/**
 * Activity that allows the enabling and disabling of sound effects.
 */
public class KaraMicManagerActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(KaraMicManagerFragment.class.getName(), null);
    }

}
