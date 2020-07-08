package com.hardkernel.odroid.settings.dtbo;

import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class DTBOverlayFragment extends LeanbackAddBackPreferenceFragment {

    private static final String TAG = "DTBOverlayFragment";

    private static final String KEY_OVERLAYS = "overlays";

    public static DTBOverlayFragment newInstance() { return new DTBOverlayFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

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

        overlays.setSummary(Overlay.get());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            case KEY_OVERLAYS:
                getByText(preference, Overlay.get(),"Please Enter DTB Overlays.\n"
                        + "BE CAREFUL TYPO! IT CAN CAUSE WRONG BEHAVIOUR! Separate with space(' ').");
                break;
        }

        return super.onPreferenceTreeClick(preference);
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
                            Overlay.set(result);
                        preference.setSummary(result);
                    }
                })
                .setCancelable(true)
                .create().show();
    }
}