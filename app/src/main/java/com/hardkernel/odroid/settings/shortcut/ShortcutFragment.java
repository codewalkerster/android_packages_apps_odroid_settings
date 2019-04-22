package com.hardkernel.odroid.settings.shortcut;

import android.os.Bundle;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import android.support.v7.preference.Preference;

public class ShortcutFragment extends LeanbackAddBackPreferenceFragment {
    private static final String KEY_F7 = "shortcut_f7";
    private static final String KEY_F8 = "shortcut_f8";
    private static final String KEY_F9 = "shortcut_f9";
    private static final String KEY_F10 = "shortcut_f10";

    private Preference f7Pref = null;
    private Preference f8Pref = null;
    private Preference f9Pref = null;
    private Preference f10Pref = null;


    public static ShortcutFragment newInstance() {
        return new ShortcutFragment();
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
        setPreferencesFromResource(R.xml.shortcut, null);

        f7Pref = findPreference(KEY_F7);
        f8Pref = findPreference(KEY_F8);
        f9Pref = findPreference(KEY_F9);
        f10Pref = findPreference(KEY_F10);

        refreshStatus();
    }

    private void refreshStatus() {
        f7Pref.setSummary(ShortcutManager.pkgNameAt(0));
        f8Pref.setSummary(ShortcutManager.pkgNameAt(1));
        f9Pref.setSummary(ShortcutManager.pkgNameAt(2));
        f10Pref.setSummary(ShortcutManager.pkgNameAt(3));
    }

    private String appName(String name) {
        return name != null ? name : "No shortcut";
    }
}