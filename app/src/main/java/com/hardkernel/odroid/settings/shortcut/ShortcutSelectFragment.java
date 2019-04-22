package com.hardkernel.odroid.settings.shortcut;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.KeyEvent;

import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.RadioPreference;

import java.util.ArrayList;

public class ShortcutSelectFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "ShortcutSelectFragment";
    private static final String NO_SHORTCUT = "No shortcut";

    private static PackageManager pm;
    protected int keycode;

    public static ShortcutSelectFragment newInstance() {
        return new ShortcutSelectFragment();
    }

    public ShortcutSelectFragment() {
        keycode = KeyEvent.KEYCODE_F7;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        pm = getContext().getPackageManager();
        updatePreferenceFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceFragment();
    }


    private static ArrayList<String> appPackageList;
    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        String subTitle = themedContext.getString(R.string.app_shortcut);
        String title = subTitle + " F" + ((keycode - KeyEvent.KEYCODE_F1) + 1);

        screen.setTitle(title);
        setPreferenceScreen(screen);

        String currentApp = String.valueOf(ShortcutManager.pkgAt(keycode - KeyEvent.KEYCODE_F7));

        appPackageList = ShortcutManager.getAvailableAppList(getContext());
        final ArrayList<String> appTitles = new ArrayList<>();

        appTitles.add(NO_SHORTCUT);

        for(String appPackage: appPackageList) {
            appTitles.add(appPackage);
        }

        for(String appTitle: appTitles) {
            RadioPreference radio = new RadioPreference(themedContext);
            if (appTitle.equals(NO_SHORTCUT)) {
                radio.setTitle(NO_SHORTCUT);
                radio.setIcon(android.R.drawable.ic_delete);
            } else if(appTitle.equals("home")) {
                radio.setKey("home");
                radio.setTitle("HOME");
                radio.setIcon(android.R.drawable.sym_def_app_icon);
            } else {
                try {
                    ApplicationInfo app = pm.getApplicationInfo(appTitle, PackageManager.GET_META_DATA);
                    radio.setKey(app.packageName);
                    radio.setTitle(app.loadLabel(pm));
                    radio.setIcon(app.loadIcon(pm));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            radio.setLayoutResource(R.layout.preference_reversed_widget);
            radio.setPersistent(false);
            if (currentApp.equals(appTitle))
                radio.setChecked(true);
            screen.addPreference(radio);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        RadioPreference radio = (RadioPreference)preference;
        radio.clearOtherRadioPreferences(getPreferenceScreen());
        String packageName = radio.getKey();

        ShortcutManager.setShortcutPreference(keycode, packageName);

        return true;
    }
}