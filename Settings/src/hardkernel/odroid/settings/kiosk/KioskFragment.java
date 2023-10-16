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

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

public class KioskFragment extends SettingsPreferenceFragment {
    private static final String TAG = "KioskFragment";
    private static final String KEY_KIOSK_MODE_SWITCH = "kiosk_mode";
    private static final String PERSIST_KIOSK_MODE = "persist.kiosk_mode";
    private static final String KEY_KIOSK_SELECT_PREF = "kiosk_target";

    private static Boolean kiosk_mode = false;

    private TwoStatePreference kioskModePref;
    private Preference kioskSelectPref = null;

    public static KioskFragment newInstance() {
        return new KioskFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.kiosk, null);
        kioskModePref = (TwoStatePreference) findPreference(KEY_KIOSK_MODE_SWITCH);
        kiosk_mode = EnvProperty.getBoolean(PERSIST_KIOSK_MODE, false);

        kioskSelectPref = findPreference(KEY_KIOSK_SELECT_PREF);

        refreshStatus();
    }

    private void refreshStatus() {
        kioskModePref.setChecked(kiosk_mode);
        kioskSelectPref.setSummary(KioskManager.pkgName());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_KIOSK_MODE_SWITCH:
                kiosk_mode = ((TwoStatePreference) preference).isChecked();
                EnvProperty.set(PERSIST_KIOSK_MODE, kiosk_mode? "true" : "false");
                Toast.makeText(getContext(),
                        R.string.kiosk_mode_message,
                        Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    };
}
