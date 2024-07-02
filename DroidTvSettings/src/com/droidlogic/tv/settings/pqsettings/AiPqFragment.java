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

package com.droidlogic.tv.settings.pqsettings;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;

import com.droidlogic.app.SystemControlManager;

public class AiPqFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AiPqFragment";

    private static final String KEY_LEVEL_AIPQ = "ai_pq_level";
    public static final String KEY_ENABLE_AIPQ_INFO = "ai_pq_info_enable";
    private static final String KEY_LEVEL_AISR = "ai_sr_level";
    private static final String KEY_ENABLE_AI_COLOR = "ai_color_enable";
    private static final String KEY_ENABLE_AISR_DEMO = "ai_pq_aisr_demo_switch";
    private static final String SYSFS_DEBUG_VDETECT = "/sys/module/decoder_common/parameters/debug_vdetect";
    private static final String SYSFS_ADD_VDETECT = "/sys/class/vdetect/tv_add_vdetect";

    private static final String PROP_AIPQ_ENABLE = "persist.vendor.sys.aipq.info";
    private static final String SAVE_AIPQ = "AIPQ";
    private static final int AIPQ_ENABLE = 1;
    private static final int AIPQ_DISABLE = 2;
    private static final int isSave = 1;

    private  ListPreference mAipqLevelPref;
    private TwoStatePreference mEnableAipqInfoPref;
    private ListPreference mAisrLevelPref;
    private ListPreference mEnableAiColorPref;
    private TwoStatePreference mEnableAisrDemoPref;

    private PQSettingsManager mPQSettingsManager;
    private SystemControlManager mSystemControlManager;
    private AisrDemoLineView mLineView;

    public static AiPqFragment newInstance() {
        return new AiPqFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        mSystemControlManager = SystemControlManager.getInstance();
        mLineView = AisrDemoLineView.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }


    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.aipq, null);

        mAipqLevelPref = (ListPreference) findPreference(KEY_LEVEL_AIPQ);
        if (mPQSettingsManager.hasAipqFunc()) {
            mAipqLevelPref.setValueIndex(mPQSettingsManager.getAipqModeLevel());
            mAipqLevelPref.setOnPreferenceChangeListener(this);
        } else {
            mAipqLevelPref.setEnabled(false);
        }

        mAisrLevelPref = (ListPreference) findPreference(KEY_LEVEL_AISR);
        if (mPQSettingsManager.hasAisrFunc()) {
            mAisrLevelPref.setValueIndex(mPQSettingsManager.getAisrModeLevel());
            mAisrLevelPref.setOnPreferenceChangeListener(this);
        } else {
            mAisrLevelPref.setEnabled(false);
        }

        mEnableAipqInfoPref = (TwoStatePreference) findPreference(KEY_ENABLE_AIPQ_INFO);
        mEnableAipqInfoPref.setOnPreferenceChangeListener(this);
        Log.i(TAG, "init Aipqinfo: " + mPQSettingsManager.getAipqInfo(PROP_AIPQ_ENABLE));
        mEnableAipqInfoPref.setChecked(mPQSettingsManager.getAipqInfo(PROP_AIPQ_ENABLE));
        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_PQ)) {
            mAipqLevelPref.setEnabled(false);
            mEnableAipqInfoPref.setEnabled(false);
        }

        mEnableAiColorPref = (ListPreference) findPreference(KEY_ENABLE_AI_COLOR);
        if (mPQSettingsManager.hasAiColorFunc()) {
            mEnableAiColorPref.setOnPreferenceChangeListener(this);
            mEnableAiColorPref.setValueIndex(mPQSettingsManager.getAiColor());
        } else {
            mEnableAiColorPref.setEnabled(false);
        }

        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_COLOR)) {
            mEnableAiColorPref.setEnabled(false);
        }

        mEnableAisrDemoPref = (TwoStatePreference) findPreference(KEY_ENABLE_AISR_DEMO);
        mEnableAisrDemoPref.setOnPreferenceChangeListener(this);
        mEnableAisrDemoPref.setChecked(mPQSettingsManager.getAisreDemoEnabled() >= 1 ? true : false);

        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_SR)) {
            mAisrLevelPref.setEnabled(false);
            mEnableAisrDemoPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_LEVEL_AIPQ)) {
            if (Integer.parseInt((String) newValue) > 0) {
                mEnableAipqInfoPref.setEnabled(true);
            } else {
                mEnableAipqInfoPref.setEnabled(false);
                mEnableAipqInfoPref.setChecked(false);
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "false");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.Global.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_DISABLE);
            }
            mPQSettingsManager.setAipqModeLevel(Integer.parseInt((String) newValue), isSave);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AIPQ_INFO)) {
            if ((boolean) newValue) {
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "true");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.Global.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_ENABLE);
            } else {
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "false");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.Global.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_DISABLE);
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_LEVEL_AISR)) {
            mPQSettingsManager.setAisrModeLevel(Integer.parseInt((String) newValue), isSave);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AI_COLOR)) {
            mPQSettingsManager.setAiColor(Integer.parseInt((String) newValue), isSave);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AISR_DEMO)) {
            turnOnAiSRDemo((boolean) newValue);
        }
        return true;
    }

    private void turnOnAiSRDemo(boolean enabled) {
        mPQSettingsManager.setAisreDemoEnabled(enabled);
        if (enabled) {
            mLineView.showLine();
        } else {
            mLineView.hideLine();
        }
    }
}