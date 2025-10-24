/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package hardkernel.odroid.settings.kiosk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;

import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.RadioPreference;

import java.util.ArrayList;

public class KioskSelectFragment extends SettingsPreferenceFragment {
    private static final String TAG = "KioskSelectFragment";
    private static final String NO_KIOSK = "No kiosk target";

    private static PackageManager pm;

    public static KioskSelectFragment newInstance() {
        return new KioskSelectFragment();
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
        String title= themedContext.getString(R.string.kiosk_select);

        screen.setTitle(title);
        setPreferenceScreen(screen);

        String currentApp = String.valueOf(KioskManager.pkg());

        appPackageList = KioskManager.getAvailableAppList(getContext());
        final ArrayList<String> appTitles = new ArrayList<>();

        appTitles.add(NO_KIOSK);

        for(String appPackage: appPackageList) {
            appTitles.add(appPackage);
        }

        for(String appTitle: appTitles) {
            RadioPreference radio = new RadioPreference(themedContext);
            if (appTitle.equals(NO_KIOSK)) {
                radio.setTitle(NO_KIOSK);
                radio.setIcon(android.R.drawable.ic_delete);
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

        KioskManager.setKioskPreference(packageName);

        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

}
