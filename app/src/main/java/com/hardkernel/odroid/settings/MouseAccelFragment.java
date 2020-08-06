package com.hardkernel.odroid.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.widget.Toast;

public class MouseAccelFragment extends LeanbackAddBackPreferenceFragment {

    private static final String KEY_MOUSE_ACCELERATION_SWITCH = "mouse_accel_switch";
    private static final String MOUSE_ACCELERATION = "mouse_acceleration";
    private TwoStatePreference mouseAccelPref;

    public static boolean mouseAccel = true;

    public static MouseAccelFragment newInstance() {
        return new MouseAccelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.mouse_accel, null);
        mouseAccelPref = (TwoStatePreference) findPreference(KEY_MOUSE_ACCELERATION_SWITCH);

        mouseAccel = EnvProperty.getBoolean(MOUSE_ACCELERATION, true);
        mouseAccelPref.setChecked(mouseAccel);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_MOUSE_ACCELERATION_SWITCH:
                mouseAccel = mouseAccelPref.isChecked();
                EnvProperty.setAndSave(MOUSE_ACCELERATION, mouseAccel, "Add Mouse Acceleration Option");
                Toast.makeText(getContext(),
                        "Mouse Acceleration will " + (mouseAccel ? "" : "not ")
                                + "be applied after reboot!",
                        Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}