package com.hardkernel.odroid.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.widget.EditText;
import android.widget.Toast;

public class AmblightFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "AmblightFragment";

    private static final String KEY_BOBLIGHT_ON_SWITCH = "boblight_on_switch";
    private static final String KEY_BOBLIGHT_CONFIG_PATH = "boblight_config_path";

    private static final String BOBLIGHT_ENABLE = "persist.vendor.boblightd.enable";
    private static final String BOBLIGHT_CONFIG_PATH = "persist.vendor.boblightd.config";

    private static final String DEFAULT_BOBLIGHT_CONFIG = "/etc/boblight.conf";

    private TwoStatePreference boblightOnPref;
    private Preference boblightConfigPref;

    public static boolean boblightOn = false;

    public static AmblightFragment newInstance() {
        return new AmblightFragment();
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
        setPreferencesFromResource(R.xml.amblight, null);
        boblightOnPref = (TwoStatePreference) findPreference(KEY_BOBLIGHT_ON_SWITCH);
        boblightConfigPref = findPreference(KEY_BOBLIGHT_CONFIG_PATH);

        refreshStatus();
    }

    private void refreshStatus() {
        boblightOn = EnvProperty.getBoolean(BOBLIGHT_ENABLE, false);
        boblightOnPref.setChecked(boblightOn);

        boblightConfigPref.setSummary(getConfigPath());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_BOBLIGHT_ON_SWITCH:
                boblightOn = boblightOnPref.isChecked();
                EnvProperty.set(BOBLIGHT_ENABLE, String.valueOf(boblightOn));
                Toast.makeText(getContext(),
                        "Boblight Service will be "
                        + (boblightOn ? "enabled" : "disabled")
                        + " after reboot!",
                        Toast.LENGTH_LONG).show();
                return true;
            case KEY_BOBLIGHT_CONFIG_PATH:
                getByText(preference, getConfigPath(),
                        "Please Enter LED String configuration file path.\n");
        }
        return super.onPreferenceTreeClick(preference);
    }

    private String getConfigPath() {
        return EnvProperty.get(BOBLIGHT_CONFIG_PATH, DEFAULT_BOBLIGHT_CONFIG);
    }

    private void getByText(final Preference preference, String defaultValue, String message) {
        final EditText text = new EditText(getContext());
        text.setText(defaultValue);
        text.setSingleLine();

        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String result = text.getText().toString();
                        EnvProperty.set(BOBLIGHT_CONFIG_PATH, result);
                        preference.setSummary(result);
                    }
                })
                .setCancelable(true)
                .create().show();
    }
}
