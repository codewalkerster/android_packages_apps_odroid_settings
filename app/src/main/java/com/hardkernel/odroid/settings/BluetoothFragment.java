package com.hardkernel.odroid.settings;

import android.content.Context;
import android.widget.Toast;

import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;

import android.os.Bundle;

public class BluetoothFragment extends LeanbackAddBackPreferenceFragment {
    private static final String KEY_BT_SERVICE_SWITCH = "bt_service";
    private static final String BT_SERVICE = "persist.feature.disable_bt";

    private TwoStatePreference btServicePref;

    public static boolean btServiceDisable = true;

    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bluetooth, null);
        btServicePref = (TwoStatePreference) findPreference(KEY_BT_SERVICE_SWITCH);

        btServiceDisable = EnvProperty.getBoolean(BT_SERVICE, false);

        btServicePref.setChecked(!btServiceDisable);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case KEY_BT_SERVICE_SWITCH:
                    btServiceDisable = !btServicePref.isChecked();
                    EnvProperty.set(BT_SERVICE, btServiceDisable ? "true" : "false");

                    Toast.makeText(getContext(),
                            "Bluetooth Service status will be applied after reboot",
                            Toast.LENGTH_LONG).show();
                    return true;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }
}
