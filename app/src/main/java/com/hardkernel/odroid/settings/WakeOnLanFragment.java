package com.hardkernel.odroid.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;


public class WakeOnLanFragment extends LeanbackPreferenceFragment {

    private static final String KEY_WAKE_ON_LAN_SWITCH = "wol_switch";

    private TwoStatePreference wolPref;

    private static boolean wolSwitch = false;

    public static WakeOnLanFragment newInstance() {
        return new WakeOnLanFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceFragment();
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wol, null);
        wolPref = (TwoStatePreference) findPreference(KEY_WAKE_ON_LAN_SWITCH);

        wolSwitch = (bootini.getWakeOnLan() == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) {
            return super.onPreferenceTreeClick(preference);
        }
        switch (key) {
            case KEY_WAKE_ON_LAN_SWITCH:
                if (wolPref.isChecked() != wolSwitch) {
                    wolSwitch = wolPref.isChecked();
                    bootini.setWakeOnLan(wolSwitch ? 1 : 0);
                }
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        wolPref.setChecked(wolSwitch);
    }
}
