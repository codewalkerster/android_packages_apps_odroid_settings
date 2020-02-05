package com.hardkernel.odroid.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.widget.Toast;

public class HeartBeatFragment extends LeanbackAddBackPreferenceFragment {

    private static final String KEY_HEARTBEAT_SWITCH = "heartbeat_switch";

    private TwoStatePreference heartBeatPref;

    private static boolean HeartBeatSwitch = false;

    public static HeartBeatFragment newInstance() {
        return new HeartBeatFragment();
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
        setPreferencesFromResource(R.xml.heartbeat, null);
        heartBeatPref = (TwoStatePreference) findPreference(KEY_HEARTBEAT_SWITCH);

        String heartbeatState = ConfigEnv.getHeartBeat();
        if (heartbeatState == null) {
            heartBeatPref.setEnabled(false);
            heartbeatState = "1";
        }
        HeartBeatSwitch = (heartbeatState.compareTo("1") == 0);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_HEARTBEAT_SWITCH:
                if (heartBeatPref.isChecked() != HeartBeatSwitch) {
                    HeartBeatSwitch = heartBeatPref.isChecked();
                    ConfigEnv.setHeartBeat(HeartBeatSwitch ? "1" : "0");
                    Toast.makeText(getContext(),
                            "HeartBeat will turn " + (HeartBeatSwitch ? "on" : "off")
                                    + " after reboot!",
                            Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        heartBeatPref.setChecked(HeartBeatSwitch);
    }
}
