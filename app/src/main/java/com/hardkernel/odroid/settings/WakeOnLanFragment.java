package com.hardkernel.odroid.settings;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;


public class WakeOnLanFragment extends LeanbackAddBackPreferenceFragment {

    private static final String KEY_WAKE_ON_LAN_SWITCH = "wol_switch";
    private static final String KEY_MAC_ADDRESS_PREF = "eth0_address";

    private TwoStatePreference wolPref;
    private Preference macPref;

    private static boolean wolSwitch = false;

    private static final String ETH_MAC_NODE ="/sys/class/net/eth0/address";

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
        macPref = (Preference) findPreference(KEY_MAC_ADDRESS_PREF);

        wolSwitch = (ConfigEnv.getWakeOnLan() == 1);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ETH_MAC_NODE));
            macPref.setSummary(reader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    ConfigEnv.setWakeOnLan(wolSwitch ? 1 : 0);
                    if (wolSwitch) {
                        Toast.makeText(getContext(),
                                "Wake On Lan will be applied after reboot!",
                                Toast.LENGTH_LONG).show();
                    }
                }
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        wolPref.setChecked(wolSwitch);
    }
}
