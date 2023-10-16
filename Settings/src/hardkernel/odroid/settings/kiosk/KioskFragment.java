package hardkernel.odroid.settings.kiosk;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

public class KioskFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "KioskFragment";
    private static final String KEY_KIOSK_MODE_SWITCH = "kiosk_mode";
    private static final String PERSIST_KIOSK_MODE = "persist.kiosk_mode";
    private static final String KEY_KIOSK_SELECT_PREF = "kiosk_target";

    private static Boolean kiosk_mode = false;

    private TwoStatePreference kioskModePref;
    private Preference kioskSelectPref = null;

    public static KioskFragment newInstance() {
        return new KioskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.kiosk, null);
        kioskModePref = (TwoStatePreference) findPreference(KEY_KIOSK_MODE_SWITCH);
        kiosk_mode = EnvProperty.getBoolean(PERSIST_KIOSK_MODE, false);

        kioskSelectPref = findPreference(KEY_KIOSK_SELECT_PREF);

        refreshStatus();
    }

    private void refreshStatus() {
        kioskModePref.setChecked(kiosk_mode);
        kioskSelectPref.setSummary(KioskManager.pkgName());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_KIOSK_MODE_SWITCH:
                kiosk_mode = ((TwoStatePreference) preference).isChecked();
                EnvProperty.set(PERSIST_KIOSK_MODE, kiosk_mode);
                Toast.makeText(getContext(),
                        R.string.kiosk_mode_message,
                        Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    };
}
