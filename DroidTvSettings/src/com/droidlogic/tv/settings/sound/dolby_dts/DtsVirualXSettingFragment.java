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

package com.droidlogic.tv.settings.soundeffect;

import android.os.Bundle;
import android.text.TextUtils;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import com.droidlogic.app.AudioEffectManager;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;

public class DtsVirualXSettingFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{
    private static final String TAG = "DtsVirualXSettingFragment";

    private static final String KEY_DTS_DIALOG_ENHANCEMENT            = "key_dts_dialog_enhancement";
    private static final String KEY_DTS_TRU_VOLUME                    = "key_tv_dts_tru_volume";
    private static final String KEY_DTS_BASS_ENHANCEMENT              = "key_tv_dts_bass_enhancement";

    private AudioEffectManager mAudioEffectManager;

    private ListPreference mDialogEnhancePref;
    private ListPreference mTruVolumePref;
    private ListPreference mBassEnhancePref;
    private boolean mNeedFreshUI = false;

    public static DtsVirualXSettingFragment newInstance() {
        return new DtsVirualXSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mAudioEffectManager = AudioEffectManager.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        if (mNeedFreshUI) {
            int truVolumeStatus = mAudioEffectManager.getDtsVirtualSurround();
            mTruVolumePref.setValue(truVolumeStatus + "");
            mTruVolumePref.setSummary(mTruVolumePref.getEntries()[truVolumeStatus]);

            int dialogEnhanceIndex = mAudioEffectManager.getDtsDialogClarityMode();
            mDialogEnhancePref.setValue(dialogEnhanceIndex + "");
            mDialogEnhancePref.setSummary(mDialogEnhancePref.getEntries()[dialogEnhanceIndex]);

            int bassEnhancerMode = mAudioEffectManager.getDtsBassEnhancement();
            mBassEnhancePref.setValue(bassEnhancerMode + "");
            mBassEnhancePref.setSummary(mBassEnhancePref.getEntries()[bassEnhancerMode]);
        }
        mNeedFreshUI = true;
        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tv_sound_dts_virtualx_setting, null);

        mTruVolumePref = (ListPreference) findPreference(KEY_DTS_TRU_VOLUME);
        mTruVolumePref.setOnPreferenceChangeListener(this);
        int truVolumeStatus = mAudioEffectManager.getDtsVirtualSurround();
        mTruVolumePref.setValue(truVolumeStatus + "");
        mTruVolumePref.setSummary(mTruVolumePref.getEntries()[truVolumeStatus]);

        mDialogEnhancePref = (ListPreference) findPreference(KEY_DTS_DIALOG_ENHANCEMENT);
        mDialogEnhancePref.setOnPreferenceChangeListener(this);
        int dialogEnhanceIndex = mAudioEffectManager.getDtsDialogClarityMode();
        mDialogEnhancePref.setValue(dialogEnhanceIndex + "");
        mDialogEnhancePref.setSummary(mDialogEnhancePref.getEntries()[dialogEnhanceIndex]);

        mBassEnhancePref =  (ListPreference) findPreference(KEY_DTS_BASS_ENHANCEMENT);
        mBassEnhancePref.setOnPreferenceChangeListener(this);
        int bassEnhancerMode = mAudioEffectManager.getDtsBassEnhancement();
        mBassEnhancePref.setValue(bassEnhancerMode + "");
        mBassEnhancePref.setSummary(mBassEnhancePref.getEntries()[bassEnhancerMode]);
        mNeedFreshUI = false;
        logDebug(TAG, false, "isSupportVirtualX:" + mAudioEffectManager.isSupportVirtualX());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), KEY_DTS_DIALOG_ENHANCEMENT)) {
            mAudioEffectManager.setDtsDialogClarityMode(selection);
        } else if (TextUtils.equals(preference.getKey(), KEY_DTS_TRU_VOLUME)) {
            mAudioEffectManager.setDtsVirtualSurround(selection);
        } else if (TextUtils.equals(preference.getKey(), KEY_DTS_BASS_ENHANCEMENT)) {
            mAudioEffectManager.setDtsBassEnhancement(selection==1? true : false);
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        /*if (key != null) {
            switch (key) {
                case KEY_TV_DTS_TRUVOLUMEHD_EFFECT:
                    mAudioEffectManager.setDtsTruVolumeHdEnable(mTruVolumeHdPref.isChecked());
                    mTruVolumeHdPref.setChecked(mAudioEffectManager.getDtsTruVolumeHdEnable());
                    break;
            }
        }*/
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
