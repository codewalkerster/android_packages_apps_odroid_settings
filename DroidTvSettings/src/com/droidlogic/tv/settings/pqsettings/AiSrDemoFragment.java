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

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.R;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

public class AiSrDemoFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AiSrDemoFragment";

    private static final String KEY_ENABLE_AISR_DEMO = "ai_pq_aisr_demo_switch";
    private static final String KEY_ENABLE_AISR_DEMO_LEFT = "ai_pq_aisr_demo_switch_left";
    private static final String KEY_ENABLE_AISR_DEMO_RIGHT = "ai_pq_aisr_demo_switch_right";

    private TwoStatePreference mEnableAisrDemoPref;
    private TwoStatePreference mEnableAisrDemoLeftPref;
    private TwoStatePreference mEnableAisrDemoRightPref;

    private PQSettingsManager mPQSettingsManager;
    private SystemControlManager mSystemControlManager;
    private AisrDemoLineView mLineView;

    public static AiSrDemoFragment newInstance() {
        return new AiSrDemoFragment();
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
        setPreferencesFromResource(R.xml.aisr_demo, null);
        mEnableAisrDemoPref = (TwoStatePreference) findPreference(KEY_ENABLE_AISR_DEMO);
        mEnableAisrDemoPref.setOnPreferenceChangeListener(this);
        boolean demoEnable = mPQSettingsManager.getAisreDemoEnabled() >= 1;
        mEnableAisrDemoPref.setChecked(demoEnable);

        mEnableAisrDemoLeftPref = (TwoStatePreference) findPreference(KEY_ENABLE_AISR_DEMO_LEFT);
        mEnableAisrDemoLeftPref.setOnPreferenceChangeListener(this);
        if (demoEnable && mSystemControlManager.GetPQModuleDemoAisrWin() == 1) {
            mEnableAisrDemoLeftPref.setChecked(true);
        } else {
            mEnableAisrDemoLeftPref.setChecked(false);
        }

        mEnableAisrDemoRightPref = (TwoStatePreference) findPreference(KEY_ENABLE_AISR_DEMO_RIGHT);
        mEnableAisrDemoRightPref.setOnPreferenceChangeListener(this);
        if (demoEnable && mSystemControlManager.GetPQModuleDemoAisrWin() == 0) {
            mEnableAisrDemoRightPref.setChecked(true);
        } else {
            mEnableAisrDemoRightPref.setChecked(false);
        }

        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_SR)) {
            mEnableAisrDemoPref.setEnabled(false);
            mEnableAisrDemoLeftPref.setEnabled(false);
            mEnableAisrDemoRightPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AISR_DEMO)) {
            turnOnAiSRDemo((boolean) newValue);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AISR_DEMO_LEFT)) {
            if ((boolean) newValue) {
                mEnableAisrDemoLeftPref.setChecked(true);
                mSystemControlManager.SetPQModuleDemoAisrWin(1);
                mEnableAisrDemoRightPref.setChecked(false);
            } else {
                mEnableAisrDemoLeftPref.setChecked(false);
                mEnableAisrDemoRightPref.setChecked(true);
                mSystemControlManager.SetPQModuleDemoAisrWin(0);
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AISR_DEMO_RIGHT)) {
            if ((boolean) newValue) {
                mEnableAisrDemoRightPref.setChecked(true);
                mSystemControlManager.SetPQModuleDemoAisrWin(0);
                mEnableAisrDemoLeftPref.setChecked(false);
            } else {
                mEnableAisrDemoRightPref.setChecked(false);
                mEnableAisrDemoLeftPref.setChecked(true);
                mSystemControlManager.SetPQModuleDemoAisrWin(1);
            }
        }
        return true;
    }

    private void turnOnAiSRDemo(boolean enabled) {
        mPQSettingsManager.setAisreDemoEnabled(enabled);
        if (enabled) {
            mEnableAisrDemoLeftPref.setEnabled(true);
            mEnableAisrDemoRightPref.setEnabled(true);
            mLineView.showLine();
        } else {
            mEnableAisrDemoLeftPref.setEnabled(false);
            mEnableAisrDemoRightPref.setEnabled(false);
            mLineView.hideLine();
        }
    }
}