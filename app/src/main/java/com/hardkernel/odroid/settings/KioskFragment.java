package com.hardkernel.odroid.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.widget.Toast;

public class KioskFragment extends LeanbackAddBackPreferenceFragment {

    private static final String KEY_KIOSK_SWITCH = "kiosk_switch";
    private static final String KIOSK_MODE = "kiosk_mode";
    private TwoStatePreference kioskPref;

    public static boolean kioskMode = false;

    public static KioskFragment newInstance() {
        return new KioskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.kisok, null);
        kioskPref = (TwoStatePreference)findPreference(KEY_KIOSK_SWITCH);

        kioskMode = EnvProperty.getBoolean(KIOSK_MODE, false);
        kioskPref.setChecked(kioskMode);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_KIOSK_SWITCH:
                kioskMode = kioskPref.isChecked();
                EnvProperty.setAndSave(KIOSK_MODE, kioskMode, "Kiosk Mode");
                Toast.makeText(getContext(),
                        "KIOSK MODE will " + (kioskMode ? "": "not ")
                                +"applied after reboot!",
                        Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
