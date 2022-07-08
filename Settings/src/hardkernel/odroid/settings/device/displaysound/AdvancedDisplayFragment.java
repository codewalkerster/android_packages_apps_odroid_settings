/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License.
 */

package hardkernel.odroid.settings.device.displaysound;

import static android.provider.Settings.Secure.MINIMAL_POST_PROCESSING_ALLOWED;

import static hardkernel.odroid.settings.util.InstrumentationUtils.logToggleInteracted;

import android.app.tvsettings.TvSettingsEnums;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

/**
 * The "Advanced display settings" screen in TV Settings.
 */
@Keep
public class AdvancedDisplayFragment extends SettingsPreferenceFragment {
    private static final String KEY_GAME_MODE = "game_mode";
    private static final String KEY_NATIVE_UI = "native_ui";

    private static final String PERSIST_NATIVE_UI = "persist.framebuffer.support4kUI";

    private static Boolean native_ui = false;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.advanced_display, null);
        SwitchPreference gameModePreference = findPreference(KEY_GAME_MODE);
        gameModePreference.setChecked(getGameModeStatus() == 1);

        native_ui = EnvProperty.getBoolean(PERSIST_NATIVE_UI, false);
        SwitchPreference nativeUiPreference = findPreference(KEY_NATIVE_UI);
        nativeUiPreference.setChecked(native_ui);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_GAME_MODE)) {
            logToggleInteracted(
                    TvSettingsEnums.DISPLAY_SOUND_ADVANCED_DISPLAY_GAME_MODE,
                    ((SwitchPreference) preference).isChecked());
            setGameModeStatus(((SwitchPreference) preference).isChecked() ? 1 : 0);
       } else if (TextUtils.equals(preference.getKey(), KEY_NATIVE_UI)) {
            native_ui = ((SwitchPreference) preference).isChecked();
            EnvProperty.set(PERSIST_NATIVE_UI, native_ui);
            Toast.makeText (getContext(),
                    R.string.native_uit_message,
                    Toast.LENGTH_LONG).show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private int getGameModeStatus() {
        return Settings.Secure.getInt(getActivity().getContentResolver(),
                MINIMAL_POST_PROCESSING_ALLOWED,
                1);
    }

    private void setGameModeStatus(int state) {
        Settings.Secure.putInt(getActivity().getContentResolver(), MINIMAL_POST_PROCESSING_ALLOWED,
                state);
    }

    @Override
    protected int getPageId() {
        return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_DISPLAY;
    }
}
