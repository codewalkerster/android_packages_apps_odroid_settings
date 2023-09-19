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

package com.droidlogic.tv.settings;

import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.tv.TvInputManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.app.AudioEffectManager;


public class QuickSettingFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickSettingFragment";

    private static final String KEY_MORE_SETTINGS = "key_quick_setting_more_settings";

    private Preference mMoreSettings;

    private AudioEffectManager mAudioEffectManager;

    public static QuickSettingFragment newInstance() {
        return new QuickSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mAudioEffectManager == null) {
            mAudioEffectManager = AudioEffectManager.getInstance(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMoreSettings = (Preference) findPreference(KEY_MORE_SETTINGS);
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        if (!isTv) {
            mMoreSettings.setVisible(false);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.quick_setting, null);
        mMoreSettings = (Preference) findPreference(KEY_MORE_SETTINGS);
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        if (!isTv) {
            mMoreSettings.setVisible(false);
        }
    }

    private String getShowString(int resid) {
        return getActivity().getResources().getString(resid);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
