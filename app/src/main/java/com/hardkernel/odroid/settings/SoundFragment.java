/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.hardkernel.odroid.settings;

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.hardkernel.odroid.settings.R;


public class SoundFragment extends LeanbackAddBackPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SoundFragment";
    private static final String KEY_SOUND_SELECT = "sound_select";
    private static final String SOUND_SELECT = "media.audio_hal.device";

    public static SoundFragment newInstance() {
        return new SoundFragment();
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound, null);

        final ListPreference soundSelectPref = (ListPreference) findPreference(KEY_SOUND_SELECT);

        soundSelectPref.setEntries(getArrayString(R.array.sound_select_entries));
        soundSelectPref.setEntryValues(getArrayString(R.array.sound_select_entry_values));
        soundSelectPref.setOnPreferenceChangeListener(this);

        updateFormatPreferencesStates();
    }

    private void updateFormatPreferencesStates() {
        final ListPreference soundSelectPref = (ListPreference) findPreference(KEY_SOUND_SELECT);

        int selected = EnvProperty.getInt(SOUND_SELECT, 0);
        switch (selected) {
            case 0: //Hdmi, lineout I2S
                soundSelectPref.setSummary("HDMI, Lineout");
                break;
            case 1: // SPDIF on 7pin
                soundSelectPref.setSummary("SPDIF");
                break;
            case 8: // I2S on 7pin
                soundSelectPref.setSummary("I2S");
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_SOUND_SELECT)) {
            final String selection = (String)newValue;
            EnvProperty.setAndSave(SOUND_SELECT, selection, "Audio Changed");
            updateFormatPreferencesStates();
            return true;
        }
        return true;
    }
}
