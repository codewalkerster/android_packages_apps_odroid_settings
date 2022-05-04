package hardkernel.odroid.settings.camera;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import hardkernel.odroid.settings.R;

import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class CameraFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "CameraFragment";

    private final String KEY_IRFILTER = "irfilter";

    public static CameraFragment newInstance() { return new CameraFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.camera);
        setPreferenceScreen(screen);

        final SwitchPreference irFilterPreference = new SwitchPreference(themedContext);
        irFilterPreference.setKey(KEY_IRFILTER);
        irFilterPreference.setPersistent(false);
        irFilterPreference.setTitle(themedContext.getString(R.string.irFilter));

        irFilterPreference.setChecked(OV5647.getIrFilterStateFromSysfs());

        screen.addPreference(irFilterPreference);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof SwitchPreference) {
            Boolean irFilter = ((SwitchPreference) preference).isChecked();

            OV5647.setIrFilterStateToSysfs(irFilter);
            OV5647.setIrFilterStateToProperty(irFilter);
        }

        return super.onPreferenceTreeClick(preference);
    }
}