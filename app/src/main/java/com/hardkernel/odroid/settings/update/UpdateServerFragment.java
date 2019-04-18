package com.hardkernel.odroid.settings.update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.widget.EditText;

import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import com.hardkernel.odroid.settings.RadioPreference;

public class UpdateServerFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "UpdateServerFragment";
    private Context context = null;

    public static UpdateServerFragment newInstance() { return  new UpdateServerFragment(); }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        screen.setTitle(R.string.update_selected_server);
        setPreferenceScreen(screen);

        screen.addPreference(createRadioPreference(updateManager.KEY_OFFICIAL,
                getString(R.string.update_official_server)));
        screen.addPreference(createRadioPreference(updateManager.KEY_MIRROR,
                getString(R.string.update_mirror_server)));
        screen.addPreference(createRadioPreference(updateManager.KEY_CUSTOM,
                getString(R.string.update_custom_server)));
    }

    private RadioPreference createRadioPreference(String key, String title) {
        final RadioPreference radioPreference = new RadioPreference(context);
        radioPreference.setKey(key);
        radioPreference.setPersistent(false);
        radioPreference.setTitle(title);
        radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
        radioPreference.setChecked(key.equals(updateManager.getServer()));
        if (key.equals(updateManager.KEY_CUSTOM)) {
            radioPreference.setSummary(updateManager.getRemoteURL());
        }

        return radioPreference;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String server = radioPreference.getKey();
                switch (server) {
                    case updateManager.KEY_OFFICIAL:
                        updateManager.setRemoteURL(updateManager.OFFICIAL_URL);
                        updateManager.setServer(updateManager.KEY_OFFICIAL);
                        break;
                    case updateManager.KEY_MIRROR:
                        updateManager.setRemoteURL(updateManager.MIRROR_URL);
                        updateManager.setServer(updateManager.KEY_MIRROR);
                        break;
                    case updateManager.KEY_CUSTOM:
                        updateManager.setServer(updateManager.KEY_CUSTOM);
                        getCustomURL(radioPreference);
                        break;
                }
                radioPreference.setChecked(true);
            } else {
                if (radioPreference.getKey().equals(updateManager.KEY_CUSTOM)) {
                    getCustomURL(radioPreference);
                }
                radioPreference.setChecked(true);
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void getCustomURL(final RadioPreference radioPreference) {
        final EditText text = new EditText(getContext());
        text.setText(updateManager.getRemoteURL());
        new AlertDialog.Builder(getContext())
            .setMessage(R.string.get_custom_url)
            .setView(text)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateManager.setRemoteURL(text.getText().toString());
                    radioPreference.setSummary(updateManager.getRemoteURL());
                }
            })
            .setCancelable(true)
            .create().show();
    }
}
