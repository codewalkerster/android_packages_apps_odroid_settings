package hardkernel.odroid.settings.dtbo;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;

import android.os.Bundle;
import androidx.preference.Preference;

public class DTBOverlayFragment extends SettingsPreferenceFragment {

    private static final String TAG = "DTBOverlayFragment";

    private static final String KEY_OVERLAYS = "overlays";

    public static DTBOverlayFragment newInstance() {
        return new DTBOverlayFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        refreshStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        setPreferencesFromResource(R.xml.overlay, null);
        Preference overlays = findPreference(KEY_OVERLAYS);

        overlays.setSummary(Overlay.getCurrent());
    }
}
