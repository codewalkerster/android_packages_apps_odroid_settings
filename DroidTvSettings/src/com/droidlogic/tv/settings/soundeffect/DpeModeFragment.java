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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import android.util.Log;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.app.AudioEffectManager;

public class DpeModeFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DpeModeFragment";

    private static final String KEY_DPE_ENABLED                    = "key_dpe_enabled";
    private static final String KEY_DPE_DETAIL                     = "key_dpe_detail";
    private static final String KEY_INPUTGAIN                      = "key_dpe_inputgain";

    private static final String KEY_PRE_EQ                         = "key_dpe_pre_eq";
    // pre eq band 0
    private static final String KEY_PRE_EQ_BAND_0                  = "key_dpe_pre_eq_band_0";
    private static final String KEY_PRE_EQ_BAND_0_CUTOFFFREQUENCY  = "key_dpe_pre_eq_band_0_cutofffrequency";
    private static final String KEY_PRE_EQ_BAND_0_GAIN             = "key_dpe_pre_eq_band_0_gain";
    // pre eq band 1
    private static final String KEY_PRE_EQ_BAND_1                  = "key_dpe_pre_eq_band_1";
    private static final String KEY_PRE_EQ_BAND_1_CUTOFFFREQUENCY  = "key_dpe_pre_eq_band_1_cutofffrequency";
    private static final String KEY_PRE_EQ_BAND_1_GAIN             = "key_dpe_pre_eq_band_1_gain";
    // pre eq band 2
    private static final String KEY_PRE_EQ_BAND_2                  = "key_dpe_pre_eq_band_2";
    private static final String KEY_PRE_EQ_BAND_2_CUTOFFFREQUENCY  = "key_dpe_pre_eq_band_2_cutofffrequency";
    private static final String KEY_PRE_EQ_BAND_2_GAIN             = "key_dpe_pre_eq_band_2_gain";

    private static final String KEY_MBC                            = "key_dpe_mbc";
    // mbc band 0
    private static final String KEY_MBC_BAND_0                     = "key_dpe_mbc_band_0";
    private static final String KEY_MBC_BAND_0_CUTOFFFREQUENCY     = "key_dpe_mbc_band_0_cutoffFrequency";
    private static final String KEY_MBC_BAND_0_ATTACKTIME          = "key_dpe_mbc_band_0_attacktime";
    private static final String KEY_MBC_BAND_0_RELEASETIME         = "key_dpe_mbc_band_0_releasetime";
    private static final String KEY_MBC_BAND_0_RATIO               = "key_dpe_mbc_band_0_ratio";
    private static final String KEY_MBC_BAND_0_THRESHOLD           = "key_dpe_mbc_band_0_threshold";
    private static final String KEY_MBC_BAND_0_KNEEWIDTH           = "key_dpe_mbc_band_0_kneewidth";
    private static final String KEY_MBC_BAND_0_NOISEGATETHRESHOLD  = "key_dpe_mbc_band_0_noisegatethreshold";
    private static final String KEY_MBC_BAND_0_EXPANDERRATIO       = "key_dpe_mbc_band_0_expanderratio";
    private static final String KEY_MBC_BAND_0_PREGAIN             = "key_dpe_mbc_band_0_pregain";
    private static final String KEY_MBC_BAND_0_POSTGAIN            = "key_dpe_mbc_band_0_postgain";
    // mbc band 1
    private static final String KEY_MBC_BAND_1                     = "key_dpe_mbc_band_1";
    private static final String KEY_MBC_BAND_1_CUTOFFFREQUENCY     = "key_dpe_mbc_band_1_cutoffFrequency";
    private static final String KEY_MBC_BAND_1_ATTACKTIME          = "key_dpe_mbc_band_1_attacktime";
    private static final String KEY_MBC_BAND_1_RELEASETIME         = "key_dpe_mbc_band_1_releasetime";
    private static final String KEY_MBC_BAND_1_RATIO               = "key_dpe_mbc_band_1_ratio";
    private static final String KEY_MBC_BAND_1_THRESHOLD           = "key_dpe_mbc_band_1_threshold";
    private static final String KEY_MBC_BAND_1_KNEEWIDTH           = "key_dpe_mbc_band_1_kneewidth";
    private static final String KEY_MBC_BAND_1_NOISEGATETHRESHOLD  = "key_dpe_mbc_band_1_noisegatethreshold";
    private static final String KEY_MBC_BAND_1_EXPANDERRATIO       = "key_dpe_mbc_band_1_expanderratio";
    private static final String KEY_MBC_BAND_1_PREGAIN             = "key_dpe_mbc_band_1_pregain";
    private static final String KEY_MBC_BAND_1_POSTGAIN            = "key_dpe_mbc_band_1_postgain";
    // mbc band 2
    private static final String KEY_MBC_BAND_2                     = "key_dpe_mbc_band_2";
    private static final String KEY_MBC_BAND_2_CUTOFFFREQUENCY     = "key_dpe_mbc_band_2_cutoffFrequency";
    private static final String KEY_MBC_BAND_2_ATTACKTIME          = "key_dpe_mbc_band_2_attacktime";
    private static final String KEY_MBC_BAND_2_RELEASETIME         = "key_dpe_mbc_band_2_releasetime";
    private static final String KEY_MBC_BAND_2_RATIO               = "key_dpe_mbc_band_2_ratio";
    private static final String KEY_MBC_BAND_2_THRESHOLD           = "key_dpe_mbc_band_2_threshold";
    private static final String KEY_MBC_BAND_2_KNEEWIDTH           = "key_dpe_mbc_band_2_kneewidth";
    private static final String KEY_MBC_BAND_2_NOISEGATETHRESHOLD  = "key_dpe_mbc_band_2_noisegatethreshold";
    private static final String KEY_MBC_BAND_2_EXPANDERRATIO       = "key_dpe_mbc_band_2_expanderratio";
    private static final String KEY_MBC_BAND_2_PREGAIN             = "key_dpe_mbc_band_2_pregain";
    private static final String KEY_MBC_BAND_2_POSTGAIN            = "key_dpe_mbc_band_2_postgain";

    private static final String KEY_POST_EQ                        = "key_dpe_post_eq";
    // post eq band 0
    private static final String KEY_POST_EQ_BAND_0                 = "key_dpe_post_eq_band_0";
    private static final String KEY_POST_EQ_BAND_0_CUTOFFFREQUENCY = "key_dpe_post_eq_band_0_cutofffrequency";
    private static final String KEY_POST_EQ_BAND_0_GAIN            = "key_dpe_post_eq_band_0_gain";
    // post eq band 1
    private static final String KEY_POST_EQ_BAND_1                 = "key_dpe_post_eq_band_1";
    private static final String KEY_POST_EQ_BAND_1_CUTOFFFREQUENCY = "key_dpe_post_eq_band_1_cutofffrequency";
    private static final String KEY_POST_EQ_BAND_1_GAIN            = "key_dpe_post_eq_band_1_gain";
    // post eq band 2
    private static final String KEY_POST_EQ_BAND_2                 = "key_dpe_post_eq_band_2";
    private static final String KEY_POST_EQ_BAND_2_CUTOFFFREQUENCY = "key_dpe_post_eq_band_2_cutofffrequency";
    private static final String KEY_POST_EQ_BAND_2_GAIN            = "key_dpe_post_eq_band_2_gain";


    private static final String KEY_LIMITER                        = "key_dpe_limiter";
    private static final String KEY_LIMITER_ATTACKTIME             = "key_dpe_limiter_attacktime";
    private static final String KEY_LIMITER_RELEASETIME            = "key_dpe_limiter_releasetime";
    private static final String KEY_LIMITER_RATIO                  = "key_dpe_limiter_ratio";
    private static final String KEY_LIMITER_THRESHOLD              = "key_dpe_limiter_threshold";
    private static final String KEY_LIMITER_POSTGAIN               = "key_dpe_limiter_postgain";


    private TwoStatePreference mDpeEnabledPref;
    // private PreferenceCategory mDpeDetailPref;
    private SeekBarPreference mInputgainPref;

    private TwoStatePreference mPreEqPref;

    private TwoStatePreference mPreEqBand0Pref;
    private SeekBarPreference mSeekBarPreEqBand0CutoffFrequency;
    private SeekBarPreference mSeekBarPreEqBand0Gain;

    private TwoStatePreference mPreEqBand1Pref;
    private SeekBarPreference mSeekBarPreEqBand1CutoffFrequency;
    private SeekBarPreference mSeekBarPreEqBand1Gain;

    private TwoStatePreference mPreEqBand2Pref;
    private SeekBarPreference mSeekBarPreEqBand2CutoffFrequency;
    private SeekBarPreference mSeekBarPreEqBand2Gain;

    private TwoStatePreference mMbcPref;

    private TwoStatePreference mMbcBand0Pref;
    private SeekBarPreference mSeekBarMbcBand0CutoffFrequency;
    private SeekBarPreference mSeekBarMbcBand0AttackTime;
    private SeekBarPreference mSeekBarMbcBand0ReleaseTime;
    private SeekBarPreference mSeekBarMbcBand0Ratio;
    private SeekBarPreference mSeekBarMbcBand0Threshold;
    private SeekBarPreference mSeekBarMbcBand0KneeWidth;
    private SeekBarPreference mSeekBarMbcBand0NoiseGateThreshold;
    private SeekBarPreference mSeekBarMbcBand0ExpanderRatio;
    private SeekBarPreference mSeekBarMbcBand0PreGain;
    private SeekBarPreference mSeekBarMbcBand0PostGain;

    private TwoStatePreference mMbcBand1Pref;
    private SeekBarPreference mSeekBarMbcBand1CutoffFrequency;
    private SeekBarPreference mSeekBarMbcBand1AttackTime;
    private SeekBarPreference mSeekBarMbcBand1ReleaseTime;
    private SeekBarPreference mSeekBarMbcBand1Ratio;
    private SeekBarPreference mSeekBarMbcBand1Threshold;
    private SeekBarPreference mSeekBarMbcBand1KneeWidth;
    private SeekBarPreference mSeekBarMbcBand1NoiseGateThreshold;
    private SeekBarPreference mSeekBarMbcBand1ExpanderRatio;
    private SeekBarPreference mSeekBarMbcBand1PreGain;
    private SeekBarPreference mSeekBarMbcBand1PostGain;

    private TwoStatePreference mMbcBand2Pref;
    private SeekBarPreference mSeekBarMbcBand2CutoffFrequency;
    private SeekBarPreference mSeekBarMbcBand2AttackTime;
    private SeekBarPreference mSeekBarMbcBand2ReleaseTime;
    private SeekBarPreference mSeekBarMbcBand2Ratio;
    private SeekBarPreference mSeekBarMbcBand2Threshold;
    private SeekBarPreference mSeekBarMbcBand2KneeWidth;
    private SeekBarPreference mSeekBarMbcBand2NoiseGateThreshold;
    private SeekBarPreference mSeekBarMbcBand2ExpanderRatio;
    private SeekBarPreference mSeekBarMbcBand2PreGain;
    private SeekBarPreference mSeekBarMbcBand2PostGain;


    private TwoStatePreference mPostEqPref;

    private TwoStatePreference mPostEqBand0Pref;
    private SeekBarPreference mSeekBarPostEqBand0CutoffFrequency;
    private SeekBarPreference mSeekBarPostEqBand0Gain;

    private TwoStatePreference mPostEqBand1Pref;
    private SeekBarPreference mSeekBarPostEqBand1CutoffFrequency;
    private SeekBarPreference mSeekBarPostEqBand1Gain;

    private TwoStatePreference mPostEqBand2Pref;
    private SeekBarPreference mSeekBarPostEqBand2CutoffFrequency;
    private SeekBarPreference mSeekBarPostEqBand2Gain;

    private TwoStatePreference mLimiterPref;
    private SeekBarPreference mSeekBarLimiterAttackTime;
    private SeekBarPreference mSeekBarLimiterReleaseTime;
    private SeekBarPreference mSeekBarLimiterRatio;
    private SeekBarPreference mSeekBarLimiterThreshold;
    private SeekBarPreference mSeekBarLimiterPostGain;


    private AudioEffectManager mAudioEffectManager;

    public static DpeModeFragment newInstance() {
        return new DpeModeFragment();
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
        updateDetail();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        boolean enable = false;
        int progress = 0;

        setPreferencesFromResource(R.xml.dpe_audioeffect, null);

        mDpeEnabledPref = (TwoStatePreference) findPreference(KEY_DPE_ENABLED);
        // mDpeDetailPref = (PreferenceCategory) findPreference(KEY_DPE_DETAIL);

        mInputgainPref = (SeekBarPreference) findPreference(KEY_INPUTGAIN);

        // pre eq
        mPreEqPref = (TwoStatePreference) findPreference(KEY_PRE_EQ);

        mPreEqBand0Pref = (TwoStatePreference) findPreference(KEY_PRE_EQ_BAND_0);
        mSeekBarPreEqBand0CutoffFrequency = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_0_CUTOFFFREQUENCY);
        mSeekBarPreEqBand0Gain = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_0_GAIN);

        mPreEqBand1Pref = (TwoStatePreference) findPreference(KEY_PRE_EQ_BAND_1);
        mSeekBarPreEqBand1CutoffFrequency = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_1_CUTOFFFREQUENCY);
        mSeekBarPreEqBand1Gain = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_1_GAIN);

        mPreEqBand2Pref = (TwoStatePreference) findPreference(KEY_PRE_EQ_BAND_2);
        mSeekBarPreEqBand2CutoffFrequency = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_2_CUTOFFFREQUENCY);
        mSeekBarPreEqBand2Gain = (SeekBarPreference) findPreference(KEY_PRE_EQ_BAND_2_GAIN);

        // mbc
        mMbcPref = (TwoStatePreference) findPreference(KEY_MBC);

        mMbcBand0Pref = (TwoStatePreference) findPreference(KEY_MBC_BAND_0);
        mSeekBarMbcBand0CutoffFrequency = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_CUTOFFFREQUENCY);
        mSeekBarMbcBand0AttackTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_ATTACKTIME);
        mSeekBarMbcBand0ReleaseTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_RELEASETIME);
        mSeekBarMbcBand0Ratio = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_RATIO);
        mSeekBarMbcBand0Threshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_THRESHOLD);
        mSeekBarMbcBand0KneeWidth = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_KNEEWIDTH);
        mSeekBarMbcBand0NoiseGateThreshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_NOISEGATETHRESHOLD);
        mSeekBarMbcBand0ExpanderRatio = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_EXPANDERRATIO);
        mSeekBarMbcBand0PreGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_PREGAIN);
        mSeekBarMbcBand0PostGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_0_POSTGAIN);

        mMbcBand1Pref = (TwoStatePreference) findPreference(KEY_MBC_BAND_1);
        mSeekBarMbcBand1CutoffFrequency = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_CUTOFFFREQUENCY);
        mSeekBarMbcBand1AttackTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_ATTACKTIME);
        mSeekBarMbcBand1ReleaseTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_RELEASETIME);
        mSeekBarMbcBand1Ratio = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_RATIO);
        mSeekBarMbcBand1Threshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_THRESHOLD);
        mSeekBarMbcBand1KneeWidth = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_KNEEWIDTH);
        mSeekBarMbcBand1NoiseGateThreshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_NOISEGATETHRESHOLD);
        mSeekBarMbcBand1ExpanderRatio = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_EXPANDERRATIO);
        mSeekBarMbcBand1PreGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_PREGAIN);
        mSeekBarMbcBand1PostGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_1_POSTGAIN);

        mMbcBand2Pref = (TwoStatePreference) findPreference(KEY_MBC_BAND_2);
        mSeekBarMbcBand2CutoffFrequency = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_CUTOFFFREQUENCY);
        mSeekBarMbcBand2AttackTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_ATTACKTIME);
        mSeekBarMbcBand2ReleaseTime = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_RELEASETIME);
        mSeekBarMbcBand2Ratio = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_RATIO);
        mSeekBarMbcBand2Threshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_THRESHOLD);
        mSeekBarMbcBand2KneeWidth = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_KNEEWIDTH);
        mSeekBarMbcBand2NoiseGateThreshold = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_NOISEGATETHRESHOLD);
        mSeekBarMbcBand2ExpanderRatio = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_EXPANDERRATIO);
        mSeekBarMbcBand2PreGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_PREGAIN);
        mSeekBarMbcBand2PostGain = (SeekBarPreference) findPreference(KEY_MBC_BAND_2_POSTGAIN);

        // post eq
        mPostEqPref = (TwoStatePreference) findPreference(KEY_POST_EQ);

        mPostEqBand0Pref = (TwoStatePreference) findPreference(KEY_POST_EQ_BAND_0);
        mSeekBarPostEqBand0CutoffFrequency = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_0_CUTOFFFREQUENCY);
        mSeekBarPostEqBand0Gain = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_0_GAIN);

        mPostEqBand1Pref = (TwoStatePreference) findPreference(KEY_POST_EQ_BAND_1);
        mSeekBarPostEqBand1CutoffFrequency = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_1_CUTOFFFREQUENCY);
        mSeekBarPostEqBand1Gain = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_1_GAIN);

        mPostEqBand2Pref = (TwoStatePreference) findPreference(KEY_POST_EQ_BAND_2);
        mSeekBarPostEqBand2CutoffFrequency = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_2_CUTOFFFREQUENCY);
        mSeekBarPostEqBand2Gain = (SeekBarPreference) findPreference(KEY_POST_EQ_BAND_2_GAIN);

        // limiter
        mLimiterPref = (TwoStatePreference) findPreference(KEY_LIMITER);
        mSeekBarLimiterAttackTime = (SeekBarPreference) findPreference(KEY_LIMITER_ATTACKTIME);
        mSeekBarLimiterReleaseTime = (SeekBarPreference) findPreference(KEY_LIMITER_RELEASETIME);
        mSeekBarLimiterRatio = (SeekBarPreference) findPreference(KEY_LIMITER_RATIO);
        mSeekBarLimiterThreshold = (SeekBarPreference) findPreference(KEY_LIMITER_THRESHOLD);
        mSeekBarLimiterPostGain = (SeekBarPreference) findPreference(KEY_LIMITER_POSTGAIN);

        mDpeEnabledPref.setOnPreferenceChangeListener(this);

        //inputgain
        mInputgainPref.setMin(-10);
        mInputgainPref.setMax(10);
        mInputgainPref.setOnPreferenceChangeListener(this);
        mInputgainPref.setSeekBarIncrement(1);

        // pre eq
        mPreEqPref.setOnPreferenceChangeListener(this);

        mPreEqBand0Pref.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand0CutoffFrequency.setMin(100);
        mSeekBarPreEqBand0CutoffFrequency.setMax(9000);
        mSeekBarPreEqBand0CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand0CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPreEqBand0Gain.setMin(-5);
        mSeekBarPreEqBand0Gain.setMax(5);
        mSeekBarPreEqBand0Gain.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand0Gain.setSeekBarIncrement(1);

        mPreEqBand1Pref.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand1CutoffFrequency.setMin(100);
        mSeekBarPreEqBand1CutoffFrequency.setMax(9000);
        mSeekBarPreEqBand1CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand1CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPreEqBand1Gain.setMin(-5);
        mSeekBarPreEqBand1Gain.setMax(5);
        mSeekBarPreEqBand1Gain.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand1Gain.setSeekBarIncrement(1);

        mPreEqBand2Pref.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand2CutoffFrequency.setMin(100);
        mSeekBarPreEqBand2CutoffFrequency.setMax(9000);
        mSeekBarPreEqBand2CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand2CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPreEqBand2Gain.setMin(-5);
        mSeekBarPreEqBand2Gain.setMax(5);
        mSeekBarPreEqBand2Gain.setOnPreferenceChangeListener(this);
        mSeekBarPreEqBand2Gain.setSeekBarIncrement(1);

        // mbc
        mMbcPref.setOnPreferenceChangeListener(this);

        //mbc band 0
        mMbcBand0Pref.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0CutoffFrequency.setMin(100);
        mSeekBarMbcBand0CutoffFrequency.setMax(9000);
        mSeekBarMbcBand0CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarMbcBand0AttackTime.setMin(0);
        mSeekBarMbcBand0AttackTime.setMax(2000);
        mSeekBarMbcBand0AttackTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0AttackTime.setSeekBarIncrement(10);

        mSeekBarMbcBand0ReleaseTime.setMin(0);
        mSeekBarMbcBand0ReleaseTime.setMax(2000);
        mSeekBarMbcBand0ReleaseTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0ReleaseTime.setSeekBarIncrement(10);

        mSeekBarMbcBand0Ratio.setMin(1);
        mSeekBarMbcBand0Ratio.setMax(20);
        mSeekBarMbcBand0Ratio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0Ratio.setSeekBarIncrement(1);

        mSeekBarMbcBand0Threshold.setMin(-60);
        mSeekBarMbcBand0Threshold.setMax(0);
        mSeekBarMbcBand0Threshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0Threshold.setSeekBarIncrement(1);

        mSeekBarMbcBand0KneeWidth.setMin(0);
        mSeekBarMbcBand0KneeWidth.setMax(10);
        mSeekBarMbcBand0KneeWidth.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0KneeWidth.setSeekBarIncrement(1);

        mSeekBarMbcBand0NoiseGateThreshold.setMin(-90);
        mSeekBarMbcBand0NoiseGateThreshold.setMax(-20);
        mSeekBarMbcBand0NoiseGateThreshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0NoiseGateThreshold.setSeekBarIncrement(1);

        mSeekBarMbcBand0ExpanderRatio.setMin(1);
        mSeekBarMbcBand0ExpanderRatio.setMax(30);
        mSeekBarMbcBand0ExpanderRatio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0ExpanderRatio.setSeekBarIncrement(1);

        mSeekBarMbcBand0PreGain.setMin(-10);
        mSeekBarMbcBand0PreGain.setMax(10);
        mSeekBarMbcBand0PreGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0PreGain.setSeekBarIncrement(1);

        mSeekBarMbcBand0PostGain.setMin(-10);
        mSeekBarMbcBand0PostGain.setMax(10);
        mSeekBarMbcBand0PostGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand0PostGain.setSeekBarIncrement(1);

        //mbc band 1
        mMbcBand1Pref.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1CutoffFrequency.setMin(100);
        mSeekBarMbcBand1CutoffFrequency.setMax(9000);
        mSeekBarMbcBand1CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarMbcBand1AttackTime.setMin(0);
        mSeekBarMbcBand1AttackTime.setMax(2000);
        mSeekBarMbcBand1AttackTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1AttackTime.setSeekBarIncrement(10);

        mSeekBarMbcBand1ReleaseTime.setMin(0);
        mSeekBarMbcBand1ReleaseTime.setMax(2000);
        mSeekBarMbcBand1ReleaseTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1ReleaseTime.setSeekBarIncrement(10);

        mSeekBarMbcBand1Ratio.setMin(1);
        mSeekBarMbcBand1Ratio.setMax(20);
        mSeekBarMbcBand1Ratio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1Ratio.setSeekBarIncrement(1);

        mSeekBarMbcBand1Threshold.setMin(-60);
        mSeekBarMbcBand1Threshold.setMax(0);
        mSeekBarMbcBand1Threshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1Threshold.setSeekBarIncrement(1);

        mSeekBarMbcBand1KneeWidth.setMin(0);
        mSeekBarMbcBand1KneeWidth.setMax(10);
        mSeekBarMbcBand1KneeWidth.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1KneeWidth.setSeekBarIncrement(1);

        mSeekBarMbcBand1NoiseGateThreshold.setMin(-90);
        mSeekBarMbcBand1NoiseGateThreshold.setMax(-20);
        mSeekBarMbcBand1NoiseGateThreshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1NoiseGateThreshold.setSeekBarIncrement(1);

        mSeekBarMbcBand1ExpanderRatio.setMin(0);
        mSeekBarMbcBand1ExpanderRatio.setMax(30);
        mSeekBarMbcBand1ExpanderRatio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1ExpanderRatio.setSeekBarIncrement(1);

        mSeekBarMbcBand1PreGain.setMin(-10);
        mSeekBarMbcBand1PreGain.setMax(10);
        mSeekBarMbcBand1PreGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1PreGain.setSeekBarIncrement(1);

        mSeekBarMbcBand1PostGain.setMin(-10);
        mSeekBarMbcBand1PostGain.setMax(10);
        mSeekBarMbcBand1PostGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand1PostGain.setSeekBarIncrement(1);

        //mbc band 2
        mMbcBand2Pref.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2CutoffFrequency.setMin(100);
        mSeekBarMbcBand2CutoffFrequency.setMax(9000);
        mSeekBarMbcBand2CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarMbcBand2AttackTime.setMin(0);
        mSeekBarMbcBand2AttackTime.setMax(2000);
        mSeekBarMbcBand2AttackTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2AttackTime.setSeekBarIncrement(10);

        mSeekBarMbcBand2ReleaseTime.setMin(0);
        mSeekBarMbcBand2ReleaseTime.setMax(2000);
        mSeekBarMbcBand2ReleaseTime.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2ReleaseTime.setSeekBarIncrement(10);

        mSeekBarMbcBand2Ratio.setMin(1);
        mSeekBarMbcBand2Ratio.setMax(20);
        mSeekBarMbcBand2Ratio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2Ratio.setSeekBarIncrement(1);

        mSeekBarMbcBand2Threshold.setMin(-60);
        mSeekBarMbcBand2Threshold.setMax(0);
        mSeekBarMbcBand2Threshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2Threshold.setSeekBarIncrement(1);

        mSeekBarMbcBand2KneeWidth.setMin(0);
        mSeekBarMbcBand2KneeWidth.setMax(10);
        mSeekBarMbcBand2KneeWidth.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2KneeWidth.setSeekBarIncrement(1);

        mSeekBarMbcBand2NoiseGateThreshold.setMin(-90);
        mSeekBarMbcBand2NoiseGateThreshold.setMax(-20);
        mSeekBarMbcBand2NoiseGateThreshold.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2NoiseGateThreshold.setSeekBarIncrement(1);

        mSeekBarMbcBand2ExpanderRatio.setMin(1);
        mSeekBarMbcBand2ExpanderRatio.setMax(30);
        mSeekBarMbcBand2ExpanderRatio.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2ExpanderRatio.setSeekBarIncrement(1);

        mSeekBarMbcBand2PreGain.setMin(-10);
        mSeekBarMbcBand2PreGain.setMax(10);
        mSeekBarMbcBand2PreGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2PreGain.setSeekBarIncrement(1);

        mSeekBarMbcBand2PostGain.setMin(-10);
        mSeekBarMbcBand2PostGain.setMax(10);
        mSeekBarMbcBand2PostGain.setOnPreferenceChangeListener(this);
        mSeekBarMbcBand2PostGain.setSeekBarIncrement(1);

        // post eq
        mPostEqPref.setOnPreferenceChangeListener(this);

        mPostEqBand0Pref.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand0CutoffFrequency.setMin(100);
        mSeekBarPostEqBand0CutoffFrequency.setMax(9000);
        mSeekBarPostEqBand0CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand0CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPostEqBand0Gain.setMin(-5);
        mSeekBarPostEqBand0Gain.setMax(5);
        mSeekBarPostEqBand0Gain.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand0Gain.setSeekBarIncrement(1);

        mPostEqBand1Pref.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand1CutoffFrequency.setMin(100);
        mSeekBarPostEqBand1CutoffFrequency.setMax(9000);
        mSeekBarPostEqBand1CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand1CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPostEqBand1Gain.setMin(-5);
        mSeekBarPostEqBand1Gain.setMax(5);
        mSeekBarPostEqBand1Gain.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand1Gain.setSeekBarIncrement(1);

        mPostEqBand2Pref.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand2CutoffFrequency.setMin(100);
        mSeekBarPostEqBand2CutoffFrequency.setMax(9000);
        mSeekBarPostEqBand2CutoffFrequency.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand2CutoffFrequency.setSeekBarIncrement(100);

        mSeekBarPostEqBand2Gain.setMin(-5);
        mSeekBarPostEqBand2Gain.setMax(5);
        mSeekBarPostEqBand2Gain.setOnPreferenceChangeListener(this);
        mSeekBarPostEqBand2Gain.setSeekBarIncrement(1);

        // limiter
        mLimiterPref.setOnPreferenceChangeListener(this);
        mSeekBarLimiterAttackTime.setMin(0);
        mSeekBarLimiterAttackTime.setMax(2000);
        mSeekBarLimiterAttackTime.setOnPreferenceChangeListener(this);
        mSeekBarLimiterAttackTime.setSeekBarIncrement(10);

        mSeekBarLimiterReleaseTime.setMin(0);
        mSeekBarLimiterReleaseTime.setMax(2000);
        mSeekBarLimiterReleaseTime.setOnPreferenceChangeListener(this);
        mSeekBarLimiterReleaseTime.setSeekBarIncrement(10);

        mSeekBarLimiterRatio.setMin(1);
        mSeekBarLimiterRatio.setMax(30);
        mSeekBarLimiterRatio.setOnPreferenceChangeListener(this);
        mSeekBarLimiterRatio.setSeekBarIncrement(1);

        mSeekBarLimiterThreshold.setMin(-50);
        mSeekBarLimiterThreshold.setMax(0);
        mSeekBarLimiterThreshold.setOnPreferenceChangeListener(this);
        mSeekBarLimiterThreshold.setSeekBarIncrement(1);

        mSeekBarLimiterPostGain.setMin(-10);
        mSeekBarLimiterPostGain.setMax(10);
        mSeekBarLimiterPostGain.setOnPreferenceChangeListener(this);
        mSeekBarLimiterPostGain.setSeekBarIncrement(1);
    }

    // update pre eq param
    private void updatePreEqBand0Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPreEqBand0CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0_CUTOFFFREQUENCY));
        mSeekBarPreEqBand0Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0_GAIN));
        mSeekBarPreEqBand0CutoffFrequency.setAdjustable(enable);
        mSeekBarPreEqBand0Gain.setAdjustable(enable);
        mSeekBarPreEqBand0CutoffFrequency.setVisible(isVisible);
        mSeekBarPreEqBand0Gain.setVisible(isVisible);
        mSeekBarPreEqBand0CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_0_cutofffrequency) : "");
        mSeekBarPreEqBand0Gain.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_0_gain) : "");
    }

    private void updatePreEqBand1Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPreEqBand1CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1_CUTOFFFREQUENCY));
        mSeekBarPreEqBand1Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1_GAIN));
        mSeekBarPreEqBand1CutoffFrequency.setAdjustable(enable);
        mSeekBarPreEqBand1Gain.setAdjustable(enable);
        mSeekBarPreEqBand1CutoffFrequency.setVisible(isVisible);
        mSeekBarPreEqBand1Gain.setVisible(isVisible);
        mSeekBarPreEqBand1CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_1_cutofffrequency) : "");
        mSeekBarPreEqBand1Gain.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_1_gain) : "");
    }

    private void updatePreEqBand2Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPreEqBand2CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2_CUTOFFFREQUENCY));
        mSeekBarPreEqBand2Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2_GAIN));
        mSeekBarPreEqBand2CutoffFrequency.setAdjustable(enable);
        mSeekBarPreEqBand2Gain.setAdjustable(enable);
        mSeekBarPreEqBand2CutoffFrequency.setVisible(isVisible);
        mSeekBarPreEqBand2Gain.setVisible(isVisible);
        mSeekBarPreEqBand2CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_2_cutofffrequency) : "");
        mSeekBarPreEqBand2Gain.setTitle(enable ? getShowString(R.string.title_dpe_pre_eq_band_2_gain) : "");
    }


    // update post eq param
    private void updatePostEqBand0Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPostEqBand0CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0_CUTOFFFREQUENCY));
        mSeekBarPostEqBand0Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0_GAIN));
        mSeekBarPostEqBand0CutoffFrequency.setAdjustable(enable);
        mSeekBarPostEqBand0Gain.setAdjustable(enable);
        mSeekBarPostEqBand0CutoffFrequency.setVisible(isVisible);
        mSeekBarPostEqBand0Gain.setVisible(isVisible);
        mSeekBarPostEqBand0CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_0_cutofffrequency) : "");
        mSeekBarPostEqBand0Gain.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_0_gain) : "");
    }

    private void updatePostEqBand1Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPostEqBand1CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1_CUTOFFFREQUENCY));
        mSeekBarPostEqBand1Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1_GAIN));
        mSeekBarPostEqBand1CutoffFrequency.setAdjustable(enable);
        mSeekBarPostEqBand1Gain.setAdjustable(enable);
        mSeekBarPostEqBand1CutoffFrequency.setVisible(isVisible);
        mSeekBarPostEqBand1Gain.setVisible(isVisible);
        mSeekBarPostEqBand1CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_1_cutofffrequency) : "");
        mSeekBarPostEqBand1Gain.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_1_gain) : "");
    }

    private void updatePostEqBand2Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        mSeekBarPostEqBand2CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2_CUTOFFFREQUENCY));
        mSeekBarPostEqBand2Gain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2_GAIN));
        mSeekBarPostEqBand2CutoffFrequency.setAdjustable(enable);
        mSeekBarPostEqBand2Gain.setAdjustable(enable);
        mSeekBarPostEqBand2CutoffFrequency.setVisible(isVisible);
        mSeekBarPostEqBand2Gain.setVisible(isVisible);
        mSeekBarPostEqBand2CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_2_cutofffrequency) : "");
        mSeekBarPostEqBand2Gain.setTitle(enable ? getShowString(R.string.title_dpe_post_eq_band_2_gain) : "");
    }

    //update mbc param
    private void updateMbcBand0Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        //mbc band 0
        mSeekBarMbcBand0CutoffFrequency.setVisible(isVisible);
        mSeekBarMbcBand0AttackTime.setVisible(isVisible);
        mSeekBarMbcBand0ReleaseTime.setVisible(isVisible);
        mSeekBarMbcBand0Ratio.setVisible(isVisible);
        mSeekBarMbcBand0Threshold.setVisible(isVisible);
        mSeekBarMbcBand0KneeWidth.setVisible(isVisible);
        mSeekBarMbcBand0NoiseGateThreshold.setVisible(isVisible);
        mSeekBarMbcBand0ExpanderRatio.setVisible(isVisible);
        mSeekBarMbcBand0PreGain.setVisible(isVisible);
        mSeekBarMbcBand0PostGain.setVisible(isVisible);

        mSeekBarMbcBand0CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_cutoffFrequency) : "");
        mSeekBarMbcBand0AttackTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_attacktime) : "");
        mSeekBarMbcBand0ReleaseTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_releasetime) : "");
        mSeekBarMbcBand0Ratio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_ratio) : "");
        mSeekBarMbcBand0Threshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_threshold) : "");
        mSeekBarMbcBand0KneeWidth.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_kneewidth) : "");
        mSeekBarMbcBand0NoiseGateThreshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_noisegatethreshold) : "");
        mSeekBarMbcBand0ExpanderRatio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_expanderratio) : "");
        mSeekBarMbcBand0PreGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_pregain) : "");
        mSeekBarMbcBand0PostGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_0_postgain) : "");

        mSeekBarMbcBand0CutoffFrequency.setAdjustable(enable);
        mSeekBarMbcBand0AttackTime.setAdjustable(enable);
        mSeekBarMbcBand0ReleaseTime.setAdjustable(enable);
        mSeekBarMbcBand0Ratio.setAdjustable(enable);
        mSeekBarMbcBand0Threshold.setAdjustable(enable);
        mSeekBarMbcBand0KneeWidth.setAdjustable(enable);
        mSeekBarMbcBand0NoiseGateThreshold.setAdjustable(enable);
        mSeekBarMbcBand0ExpanderRatio.setAdjustable(enable);
        mSeekBarMbcBand0PreGain.setAdjustable(enable);
        mSeekBarMbcBand0PostGain.setAdjustable(enable);

        mSeekBarMbcBand0CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_CUTOFFFREQUENCY));
        mSeekBarMbcBand0AttackTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_ATTACKTIME));
        mSeekBarMbcBand0ReleaseTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_RELEASETIME));
        mSeekBarMbcBand0Ratio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_RATIO));
        mSeekBarMbcBand0Threshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_THRESHOLD));
        mSeekBarMbcBand0KneeWidth.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_KNEEWIDTH));
        mSeekBarMbcBand0NoiseGateThreshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_NOISEGATETHRESHOLD));
        mSeekBarMbcBand0ExpanderRatio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_EXPANDERRATIO));
        mSeekBarMbcBand0PreGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_PREGAIN));
        mSeekBarMbcBand0PostGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_POSTGAIN));
    }

    private void updateMbcBand1Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        //mbc band 1
        mSeekBarMbcBand1CutoffFrequency.setVisible(isVisible);
        mSeekBarMbcBand1AttackTime.setVisible(isVisible);
        mSeekBarMbcBand1ReleaseTime.setVisible(isVisible);
        mSeekBarMbcBand1Ratio.setVisible(isVisible);
        mSeekBarMbcBand1Threshold.setVisible(isVisible);
        mSeekBarMbcBand1KneeWidth.setVisible(isVisible);
        mSeekBarMbcBand1NoiseGateThreshold.setVisible(isVisible);
        mSeekBarMbcBand1ExpanderRatio.setVisible(isVisible);
        mSeekBarMbcBand1PreGain.setVisible(isVisible);
        mSeekBarMbcBand1PostGain.setVisible(isVisible);

        mSeekBarMbcBand1CutoffFrequency.setAdjustable(enable);
        mSeekBarMbcBand1AttackTime.setAdjustable(enable);
        mSeekBarMbcBand1ReleaseTime.setAdjustable(enable);
        mSeekBarMbcBand1Ratio.setAdjustable(enable);
        mSeekBarMbcBand1Threshold.setAdjustable(enable);
        mSeekBarMbcBand1KneeWidth.setAdjustable(enable);
        mSeekBarMbcBand1NoiseGateThreshold.setAdjustable(enable);
        mSeekBarMbcBand1ExpanderRatio.setAdjustable(enable);
        mSeekBarMbcBand1PreGain.setAdjustable(enable);
        mSeekBarMbcBand1PostGain.setAdjustable(enable);

        mSeekBarMbcBand1CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_cutoffFrequency) : "");
        mSeekBarMbcBand1AttackTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_attacktime) : "");
        mSeekBarMbcBand1ReleaseTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_releasetime) : "");
        mSeekBarMbcBand1Ratio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_ratio) : "");
        mSeekBarMbcBand1Threshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_threshold) : "");
        mSeekBarMbcBand1KneeWidth.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_kneewidth) : "");
        mSeekBarMbcBand1NoiseGateThreshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_noisegatethreshold) : "");
        mSeekBarMbcBand1ExpanderRatio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_expanderratio) : "");
        mSeekBarMbcBand1PreGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_pregain) : "");
        mSeekBarMbcBand1PostGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_1_postgain) : "");

        mSeekBarMbcBand1CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_CUTOFFFREQUENCY));
        mSeekBarMbcBand1AttackTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_ATTACKTIME));
        mSeekBarMbcBand1ReleaseTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_RELEASETIME));
        mSeekBarMbcBand1Ratio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_RATIO));
        mSeekBarMbcBand1Threshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_THRESHOLD));
        mSeekBarMbcBand1KneeWidth.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_KNEEWIDTH));
        mSeekBarMbcBand1NoiseGateThreshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_NOISEGATETHRESHOLD));
        mSeekBarMbcBand1ExpanderRatio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_EXPANDERRATIO));
        mSeekBarMbcBand1PreGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_PREGAIN));
        mSeekBarMbcBand1PostGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_POSTGAIN));
    }

    private void updateMbcBand2Param(boolean param, boolean enable) {
        boolean isVisible = false;
        isVisible = param && enable;
        //mbc band 2
        mSeekBarMbcBand2CutoffFrequency.setVisible(isVisible);
        mSeekBarMbcBand2AttackTime.setVisible(isVisible);
        mSeekBarMbcBand2ReleaseTime.setVisible(isVisible);
        mSeekBarMbcBand2Ratio.setVisible(isVisible);
        mSeekBarMbcBand2Threshold.setVisible(isVisible);
        mSeekBarMbcBand2KneeWidth.setVisible(isVisible);
        mSeekBarMbcBand2NoiseGateThreshold.setVisible(isVisible);
        mSeekBarMbcBand2ExpanderRatio.setVisible(isVisible);
        mSeekBarMbcBand2PreGain.setVisible(isVisible);
        mSeekBarMbcBand2PostGain.setVisible(isVisible);

        mSeekBarMbcBand2CutoffFrequency.setAdjustable(enable);
        mSeekBarMbcBand2AttackTime.setAdjustable(enable);
        mSeekBarMbcBand2ReleaseTime.setAdjustable(enable);
        mSeekBarMbcBand2Ratio.setAdjustable(enable);
        mSeekBarMbcBand2Threshold.setAdjustable(enable);
        mSeekBarMbcBand2KneeWidth.setAdjustable(enable);
        mSeekBarMbcBand2NoiseGateThreshold.setAdjustable(enable);
        mSeekBarMbcBand2ExpanderRatio.setAdjustable(enable);
        mSeekBarMbcBand2PreGain.setAdjustable(enable);
        mSeekBarMbcBand2PostGain.setAdjustable(enable);

        mSeekBarMbcBand2CutoffFrequency.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_cutoffFrequency) : "");
        mSeekBarMbcBand2AttackTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_attacktime) : "");
        mSeekBarMbcBand2ReleaseTime.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_releasetime) : "");
        mSeekBarMbcBand2Ratio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_ratio) : "");
        mSeekBarMbcBand2Threshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_threshold) : "");
        mSeekBarMbcBand2KneeWidth.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_kneewidth) : "");
        mSeekBarMbcBand2NoiseGateThreshold.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_noisegatethreshold) : "");
        mSeekBarMbcBand2ExpanderRatio.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_expanderratio) : "");
        mSeekBarMbcBand2PreGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_pregain) : "");
        mSeekBarMbcBand2PostGain.setTitle(enable ? getShowString(R.string.title_dpe_mbc_band_2_postgain) : "");

        mSeekBarMbcBand2CutoffFrequency.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_CUTOFFFREQUENCY));
        mSeekBarMbcBand2AttackTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_ATTACKTIME));
        mSeekBarMbcBand2ReleaseTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_RELEASETIME));
        mSeekBarMbcBand2Ratio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_RATIO));
        mSeekBarMbcBand2Threshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_THRESHOLD));
        mSeekBarMbcBand2KneeWidth.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_KNEEWIDTH));
        mSeekBarMbcBand2NoiseGateThreshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_NOISEGATETHRESHOLD));
        mSeekBarMbcBand2ExpanderRatio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_EXPANDERRATIO));
        mSeekBarMbcBand2PreGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_PREGAIN));
        mSeekBarMbcBand2PostGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_POSTGAIN));
    }


    // limiter param
    private void updateLimiterParam(boolean param) {
        boolean enable = param;
        mSeekBarLimiterAttackTime.setVisible(enable);
        mSeekBarLimiterReleaseTime.setVisible(enable);
        mSeekBarLimiterRatio.setVisible(enable);
        mSeekBarLimiterThreshold.setVisible(enable);
        mSeekBarLimiterPostGain.setVisible(enable);

        mSeekBarLimiterAttackTime.setAdjustable(enable);
        mSeekBarLimiterReleaseTime.setAdjustable(enable);
        mSeekBarLimiterRatio.setAdjustable(enable);
        mSeekBarLimiterThreshold.setAdjustable(enable);
        mSeekBarLimiterPostGain.setAdjustable(enable);

        mSeekBarLimiterAttackTime.setTitle(enable ? getShowString(R.string.title_dpe_limiter_attacktime) : "");
        mSeekBarLimiterReleaseTime.setTitle(enable ? getShowString(R.string.title_dpe_limiter_releasetime) : "");
        mSeekBarLimiterRatio.setTitle(enable ? getShowString(R.string.title_dpe_limiter_ratio) : "");
        mSeekBarLimiterThreshold.setTitle(enable ? getShowString(R.string.title_dpe_limiter_threshold) : "");
        mSeekBarLimiterPostGain.setTitle(enable ? getShowString(R.string.title_dpe_limiter_postgain) : "");

        mSeekBarLimiterAttackTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_ATTACKTIME));
        mSeekBarLimiterReleaseTime.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_RELEASETIME));
        mSeekBarLimiterRatio.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_RATIO));
        mSeekBarLimiterThreshold.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_THRESHOLD));
        mSeekBarLimiterPostGain.setValue(mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_POSTGAIN));
    }


    private String getShowString(int resid) {
        return getActivity().getResources().getString(resid);
    }

    private void updateDetail() {
        boolean enable = false;
        boolean enablePreEqBand0 = false, enablePreEqBand1 = false, enablePreEqBand2 = false;
        boolean enableMbcBand0 = false, enableMbcBand1 = false, enableMbcBand2 = false;
        boolean enablePostEqBand0 = false, enablePostEqBand1 = false, enablePostEqBand2 = false;
        boolean enableLimiter = false;
        boolean isVisible = false;
        int val = 0, progress = 0;
        int mode = 0;

        mode = mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_ENABLED);
        if (mode == AudioEffectManager.DPE_OFF) {
            mDpeEnabledPref.setChecked(false);
            mInputgainPref.setVisible(false);

            //pre eq
            mPreEqPref.setVisible(false);
            mPreEqBand0Pref.setVisible(false);
            mSeekBarPreEqBand0CutoffFrequency.setVisible(false);
            mSeekBarPreEqBand0Gain.setVisible(false);
            mPreEqBand1Pref.setVisible(false);
            mSeekBarPreEqBand1CutoffFrequency.setVisible(false);
            mSeekBarPreEqBand1Gain.setVisible(false);
            mPreEqBand2Pref.setVisible(false);
            mSeekBarPreEqBand2CutoffFrequency.setVisible(false);
            mSeekBarPreEqBand2Gain.setVisible(false);

            //post eq
            mPostEqPref.setVisible(false);
            mPostEqBand0Pref.setVisible(false);
            mSeekBarPostEqBand0CutoffFrequency.setVisible(false);
            mSeekBarPostEqBand0Gain.setVisible(false);
            mPostEqBand1Pref.setVisible(false);
            mSeekBarPostEqBand1CutoffFrequency.setVisible(false);
            mSeekBarPostEqBand1Gain.setVisible(false);
            mPostEqBand2Pref.setVisible(false);
            mSeekBarPostEqBand2CutoffFrequency.setVisible(false);
            mSeekBarPostEqBand2Gain.setVisible(false);

            //mbc
            mMbcPref.setVisible(false);
            mMbcBand0Pref.setVisible(false);
            mSeekBarMbcBand0CutoffFrequency.setVisible(false);
            mSeekBarMbcBand0AttackTime.setVisible(false);
            mSeekBarMbcBand0ReleaseTime.setVisible(false);
            mSeekBarMbcBand0Ratio.setVisible(false);
            mSeekBarMbcBand0Threshold.setVisible(false);
            mSeekBarMbcBand0KneeWidth.setVisible(false);
            mSeekBarMbcBand0NoiseGateThreshold.setVisible(false);
            mSeekBarMbcBand0ExpanderRatio.setVisible(false);
            mSeekBarMbcBand0PreGain.setVisible(false);
            mSeekBarMbcBand0PostGain.setVisible(false);

            mMbcBand1Pref.setVisible(false);
            mSeekBarMbcBand1CutoffFrequency.setVisible(false);
            mSeekBarMbcBand1AttackTime.setVisible(false);
            mSeekBarMbcBand1ReleaseTime.setVisible(false);
            mSeekBarMbcBand1Ratio.setVisible(false);
            mSeekBarMbcBand1Threshold.setVisible(false);
            mSeekBarMbcBand1KneeWidth.setVisible(false);
            mSeekBarMbcBand1NoiseGateThreshold.setVisible(false);
            mSeekBarMbcBand1ExpanderRatio.setVisible(false);
            mSeekBarMbcBand1PreGain.setVisible(false);
            mSeekBarMbcBand1PostGain.setVisible(false);

            mMbcBand2Pref.setVisible(false);
            mSeekBarMbcBand2CutoffFrequency.setVisible(false);
            mSeekBarMbcBand2AttackTime.setVisible(false);
            mSeekBarMbcBand2ReleaseTime.setVisible(false);
            mSeekBarMbcBand2Ratio.setVisible(false);
            mSeekBarMbcBand2Threshold.setVisible(false);
            mSeekBarMbcBand2KneeWidth.setVisible(false);
            mSeekBarMbcBand2NoiseGateThreshold.setVisible(false);
            mSeekBarMbcBand2ExpanderRatio.setVisible(false);
            mSeekBarMbcBand2PreGain.setVisible(false);
            mSeekBarMbcBand2PostGain.setVisible(false);

            //limiter
            mLimiterPref.setVisible(false);
            mSeekBarLimiterAttackTime.setVisible(false);
            mSeekBarLimiterReleaseTime.setVisible(false);
            mSeekBarLimiterRatio.setVisible(false);
            mSeekBarLimiterThreshold.setVisible(false);
            mSeekBarLimiterPostGain.setVisible(false);
            //mDpeDetailPref.setTitle("");
            return;
        } else if (mode != AudioEffectManager.DPE_OFF) {
            mDpeEnabledPref.setChecked(true);
            isVisible = true;
        }

        val = mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_INPUTGAIN);
        mInputgainPref.setValue(val);
        mInputgainPref.setAdjustable(true);
        mInputgainPref.setVisible(isVisible);
        mInputgainPref.setTitle(isVisible ? getShowString(R.string.title_dpe_inputgain) : "");

        enable = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_PRE_EQ) != AudioEffectManager.DPE_PRE_EQ_OFF);
        mPreEqPref.setChecked(enable);
        mPreEqPref.setVisible(isVisible);

        mPreEqBand0Pref.setVisible(enable);
        enablePreEqBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0) == 1);
        mPreEqBand0Pref.setChecked(enablePreEqBand0);
        updatePreEqBand0Param(enablePreEqBand0, enable);

        mPreEqBand1Pref.setVisible(enable);
        enablePreEqBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1) == 1);
        mPreEqBand1Pref.setChecked(enablePreEqBand1);
        updatePreEqBand1Param(enablePreEqBand1, enable);

        mPreEqBand2Pref.setVisible(enable);
        enablePreEqBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2) == 1);
        mPreEqBand2Pref.setChecked(enablePreEqBand2);
        updatePreEqBand2Param(enablePreEqBand2, enable);

        enable = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_MBC) != AudioEffectManager.DPE_MBC_OFF);
        mMbcPref.setChecked(enable);
        mMbcPref.setVisible(isVisible);

        mMbcBand0Pref.setVisible(enable);
        enableMbcBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0) == 1);
        mMbcBand0Pref.setChecked(enableMbcBand0);
        updateMbcBand0Param(enableMbcBand0, enable);

        mMbcBand1Pref.setVisible(enable);
        enableMbcBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1) == 1);
        mMbcBand1Pref.setChecked(enableMbcBand1);
        updateMbcBand1Param(enableMbcBand1, enable);

        mMbcBand2Pref.setVisible(enable);
        enableMbcBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2) == 1);
        mMbcBand2Pref.setChecked(enableMbcBand2);
        updateMbcBand2Param(enableMbcBand2, enable);

        enable = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_POST_EQ) != AudioEffectManager.DPE_POST_EQ_OFF);
        mPostEqPref.setChecked(enable);
        mPostEqPref.setVisible(isVisible);

        mPostEqBand0Pref.setVisible(enable);
        enablePostEqBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0) == 1);
        mPostEqBand0Pref.setChecked(enablePostEqBand0);
        updatePostEqBand0Param(enablePostEqBand0, enable);

        mPostEqBand1Pref.setVisible(enable);
        enablePostEqBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1) == 1);
        mPostEqBand1Pref.setChecked(enablePostEqBand1);
        updatePostEqBand1Param(enablePostEqBand1, enable);

        mPostEqBand2Pref.setVisible(enable);
        enablePostEqBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2) == 1);
        mPostEqBand2Pref.setChecked(enablePostEqBand2);
        updatePostEqBand2Param(enablePostEqBand2, enable);

        enable = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_LIMITER) != AudioEffectManager.DPE_LIMITER_OFF);
        mLimiterPref.setChecked(enable);
        mLimiterPref.setVisible(isVisible);
        enableLimiter = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_LIMITER) == 1);
        mLimiterPref.setChecked(enableLimiter);
        updateLimiterParam(enableLimiter);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean isChecked;
        switch (preference.getKey()) {
            case KEY_DPE_ENABLED:
                isChecked = mDpeEnabledPref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_ENABLED, isChecked ? 1 : 0);
                mInputgainPref.setVisible(isChecked);
                mInputgainPref.setTitle(isChecked ? getShowString(R.string.title_dpe_inputgain) : "");

                //pre eq
                mPreEqPref.setVisible(isChecked);
                mPreEqPref.setChecked(false);
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_PRE_EQ, 0);
                if (isChecked == false) {
                    mPreEqBand0Pref.setVisible(false);
                    mSeekBarPreEqBand0CutoffFrequency.setVisible(false);
                    mSeekBarPreEqBand0Gain.setVisible(false);

                    mPreEqBand1Pref.setVisible(false);
                    mSeekBarPreEqBand1CutoffFrequency.setVisible(false);
                    mSeekBarPreEqBand1Gain.setVisible(false);

                    mPreEqBand2Pref.setVisible(false);
                    mSeekBarPreEqBand2CutoffFrequency.setVisible(false);
                    mSeekBarPreEqBand2Gain.setVisible(false);
                }

                //post eq
                mPostEqPref.setVisible(isChecked);
                mPostEqPref.setChecked(false);
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_POST_EQ, 0);
                if (isChecked == false) {
                    mPostEqBand0Pref.setVisible(false);
                    mSeekBarPostEqBand0CutoffFrequency.setVisible(false);
                    mSeekBarPostEqBand0Gain.setVisible(false);

                    mPostEqBand1Pref.setVisible(false);
                    mSeekBarPostEqBand1CutoffFrequency.setVisible(false);
                    mSeekBarPostEqBand1Gain.setVisible(false);

                    mPostEqBand2Pref.setVisible(false);
                    mSeekBarPostEqBand2CutoffFrequency.setVisible(false);
                    mSeekBarPostEqBand2Gain.setVisible(false);
                }

                //mbc
                mMbcPref.setVisible(isChecked);
                mMbcPref.setChecked(false);
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_MBC, 0);
                if (isChecked == false) {
                    mMbcBand0Pref.setVisible(false);
                    mSeekBarMbcBand0CutoffFrequency.setVisible(false);
                    mSeekBarMbcBand0AttackTime.setVisible(false);
                    mSeekBarMbcBand0ReleaseTime.setVisible(false);
                    mSeekBarMbcBand0Ratio.setVisible(false);
                    mSeekBarMbcBand0Threshold.setVisible(false);
                    mSeekBarMbcBand0KneeWidth.setVisible(false);
                    mSeekBarMbcBand0NoiseGateThreshold.setVisible(false);
                    mSeekBarMbcBand0ExpanderRatio.setVisible(false);
                    mSeekBarMbcBand0PreGain.setVisible(false);
                    mSeekBarMbcBand0PostGain.setVisible(false);

                    mMbcBand1Pref.setVisible(false);
                    mSeekBarMbcBand1CutoffFrequency.setVisible(false);
                    mSeekBarMbcBand1AttackTime.setVisible(false);
                    mSeekBarMbcBand1ReleaseTime.setVisible(false);
                    mSeekBarMbcBand1Ratio.setVisible(false);
                    mSeekBarMbcBand1Threshold.setVisible(false);
                    mSeekBarMbcBand1KneeWidth.setVisible(false);
                    mSeekBarMbcBand1NoiseGateThreshold.setVisible(false);
                    mSeekBarMbcBand1ExpanderRatio.setVisible(false);
                    mSeekBarMbcBand1PreGain.setVisible(false);
                    mSeekBarMbcBand1PostGain.setVisible(false);

                    mMbcBand2Pref.setVisible(false);
                    mSeekBarMbcBand2CutoffFrequency.setVisible(false);
                    mSeekBarMbcBand2AttackTime.setVisible(false);
                    mSeekBarMbcBand2ReleaseTime.setVisible(false);
                    mSeekBarMbcBand2Ratio.setVisible(false);
                    mSeekBarMbcBand2Threshold.setVisible(false);
                    mSeekBarMbcBand2KneeWidth.setVisible(false);
                    mSeekBarMbcBand2NoiseGateThreshold.setVisible(false);
                    mSeekBarMbcBand2ExpanderRatio.setVisible(false);
                    mSeekBarMbcBand2PreGain.setVisible(false);
                    mSeekBarMbcBand2PostGain.setVisible(false);
                }

                //limiter
                mLimiterPref.setVisible(isChecked);
                mLimiterPref.setChecked(false);
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_LIMITER, 0);
                if (isChecked == false) {
                    mSeekBarLimiterAttackTime.setVisible(false);
                    mSeekBarLimiterReleaseTime.setVisible(false);
                    mSeekBarLimiterRatio.setVisible(false);
                    mSeekBarLimiterThreshold.setVisible(false);
                    mSeekBarLimiterPostGain.setVisible(false);
                }

                break;
            case KEY_PRE_EQ:
                isChecked = mPreEqPref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_PRE_EQ, isChecked ? 1 : 0);

                mPreEqBand0Pref.setVisible(isChecked);
                boolean enablePreEqBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0) == 1);
                mPreEqBand0Pref.setChecked(enablePreEqBand0);
                updatePreEqBand0Param(enablePreEqBand0, isChecked);

                mPreEqBand1Pref.setVisible(isChecked);
                boolean enablePreEqBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1) == 1);
                mPreEqBand1Pref.setChecked(enablePreEqBand1);
                updatePreEqBand1Param(enablePreEqBand1, isChecked);

                mPreEqBand2Pref.setVisible(isChecked);
                boolean enablePreEqBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2) == 1);
                mPreEqBand2Pref.setChecked(enablePreEqBand2);
                updatePreEqBand2Param(enablePreEqBand2, isChecked);
                break;
            case KEY_PRE_EQ_BAND_0:
                isChecked = mPreEqBand0Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0, isChecked ? 1 : 0);
                mSeekBarPreEqBand0CutoffFrequency.setVisible(isChecked);
                mSeekBarPreEqBand0Gain.setVisible(isChecked);
                break;
            case KEY_PRE_EQ_BAND_1:
                isChecked = mPreEqBand1Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1, isChecked ? 1 : 0);
                mSeekBarPreEqBand1CutoffFrequency.setVisible(isChecked);
                mSeekBarPreEqBand1Gain.setVisible(isChecked);
                break;
            case KEY_PRE_EQ_BAND_2:
                isChecked = mPreEqBand2Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2, isChecked ? 1 : 0);
                mSeekBarPreEqBand2CutoffFrequency.setVisible(isChecked);
                mSeekBarPreEqBand2Gain.setVisible(isChecked);
                break;
            case KEY_POST_EQ:
                isChecked = mPostEqPref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_POST_EQ, isChecked ? 1 : 0);

                mPostEqBand0Pref.setVisible(isChecked);
                boolean enablePostEqBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0) == 1);
                mPostEqBand0Pref.setChecked(enablePostEqBand0);
                updatePostEqBand0Param(enablePostEqBand0, isChecked);

                mPostEqBand1Pref.setVisible(isChecked);
                boolean enablePostEqBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1) == 1);
                mPostEqBand1Pref.setChecked(enablePostEqBand1);
                updatePostEqBand1Param(enablePostEqBand1, isChecked);

                mPostEqBand2Pref.setVisible(isChecked);
                boolean enablePostEqBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2) == 1);
                mPostEqBand2Pref.setChecked(enablePostEqBand2);
                updatePostEqBand2Param(enablePostEqBand2, isChecked);
                break;
            case KEY_POST_EQ_BAND_0:
                isChecked = mPostEqBand0Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0, isChecked ? 1 : 0);
                mSeekBarPostEqBand0CutoffFrequency.setVisible(isChecked);
                mSeekBarPostEqBand0Gain.setVisible(isChecked);
                break;
            case KEY_POST_EQ_BAND_1:
                isChecked = mPostEqBand1Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1, isChecked ? 1 : 0);
                mSeekBarPostEqBand1CutoffFrequency.setVisible(isChecked);
                mSeekBarPostEqBand1Gain.setVisible(isChecked);
                break;
            case KEY_POST_EQ_BAND_2:
                isChecked = mPostEqBand2Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2, isChecked ? 1 : 0);
                mSeekBarPostEqBand2CutoffFrequency.setVisible(isChecked);
                mSeekBarPostEqBand2Gain.setVisible(isChecked);
                break;
            case KEY_MBC:
                isChecked = mMbcPref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_MBC, isChecked ? 1 : 0);

                mMbcBand0Pref.setVisible(isChecked);
                boolean enableMbcBand0 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0) == 1);
                mMbcBand0Pref.setChecked(enableMbcBand0);
                updateMbcBand0Param(enableMbcBand0, isChecked);

                mMbcBand1Pref.setVisible(isChecked);
                boolean enableMbcBand1 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1) == 1);
                mMbcBand1Pref.setChecked(enableMbcBand1);
                updateMbcBand1Param(enableMbcBand1, isChecked);

                mMbcBand2Pref.setVisible(isChecked);
                boolean enableMbcBand2 = (mAudioEffectManager.getDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2) == 1);
                mMbcBand2Pref.setChecked(enableMbcBand2);
                updateMbcBand2Param(enableMbcBand2, isChecked);
                break;
            case KEY_MBC_BAND_0:
                isChecked = mMbcBand0Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0, isChecked ? 1 : 0);
                mSeekBarMbcBand0CutoffFrequency.setVisible(isChecked);
                mSeekBarMbcBand0AttackTime.setVisible(isChecked);
                mSeekBarMbcBand0ReleaseTime.setVisible(isChecked);
                mSeekBarMbcBand0Ratio.setVisible(isChecked);
                mSeekBarMbcBand0Threshold.setVisible(isChecked);
                mSeekBarMbcBand0KneeWidth.setVisible(isChecked);
                mSeekBarMbcBand0NoiseGateThreshold.setVisible(isChecked);
                mSeekBarMbcBand0ExpanderRatio.setVisible(isChecked);
                mSeekBarMbcBand0PreGain.setVisible(isChecked);
                mSeekBarMbcBand0PostGain.setVisible(isChecked);
                break;
            case KEY_MBC_BAND_1:
                isChecked = mMbcBand1Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1, isChecked ? 1 : 0);
                mSeekBarMbcBand1CutoffFrequency.setVisible(isChecked);
                mSeekBarMbcBand1AttackTime.setVisible(isChecked);
                mSeekBarMbcBand1ReleaseTime.setVisible(isChecked);
                mSeekBarMbcBand1Ratio.setVisible(isChecked);
                mSeekBarMbcBand1Threshold.setVisible(isChecked);
                mSeekBarMbcBand1KneeWidth.setVisible(isChecked);
                mSeekBarMbcBand1NoiseGateThreshold.setVisible(isChecked);
                mSeekBarMbcBand1ExpanderRatio.setVisible(isChecked);
                mSeekBarMbcBand1PreGain.setVisible(isChecked);
                mSeekBarMbcBand1PostGain.setVisible(isChecked);
                break;
            case KEY_MBC_BAND_2:
                isChecked = mMbcBand2Pref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2, isChecked ? 1 : 0);
                mSeekBarMbcBand2CutoffFrequency.setVisible(isChecked);
                mSeekBarMbcBand2AttackTime.setVisible(isChecked);
                mSeekBarMbcBand2ReleaseTime.setVisible(isChecked);
                mSeekBarMbcBand2Ratio.setVisible(isChecked);
                mSeekBarMbcBand2Threshold.setVisible(isChecked);
                mSeekBarMbcBand2KneeWidth.setVisible(isChecked);
                mSeekBarMbcBand2NoiseGateThreshold.setVisible(isChecked);
                mSeekBarMbcBand2ExpanderRatio.setVisible(isChecked);
                mSeekBarMbcBand2PreGain.setVisible(isChecked);
                mSeekBarMbcBand2PostGain.setVisible(isChecked);
                break;
            case KEY_LIMITER:
                isChecked = mLimiterPref.isChecked();
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_LIMITER, isChecked ? 1 : 0);
                boolean enableLimiter = (mAudioEffectManager.getDpeParam(AudioEffectManager.CMD_DPE_LIMITER) == 1);
                mLimiterPref.setChecked(enableLimiter);
                updateLimiterParam(enableLimiter);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_INPUTGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.CMD_DPE_INPUTGAIN, (int)newValue);
                break;

            //pre eq
            case KEY_PRE_EQ_BAND_0_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_PRE_EQ_BAND_0_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND0_GAIN, (int)newValue);
                break;
            case KEY_PRE_EQ_BAND_1_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_PRE_EQ_BAND_1_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND1_GAIN, (int)newValue);
                break;
            case KEY_PRE_EQ_BAND_2_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_PRE_EQ_BAND_2_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_PRE_EQ_BAND2_GAIN, (int)newValue);
                break;

            //post eq
            case KEY_POST_EQ_BAND_0_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_POST_EQ_BAND_0_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND0_GAIN, (int)newValue);
                break;
            case KEY_POST_EQ_BAND_1_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_POST_EQ_BAND_1_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND1_GAIN, (int)newValue);
                break;
            case KEY_POST_EQ_BAND_2_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_POST_EQ_BAND_2_GAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_POST_EQ_BAND2_GAIN, (int)newValue);
                break;

            //mbc band 0
            case KEY_MBC_BAND_0_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_MBC_BAND_0_ATTACKTIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_ATTACKTIME, (int)newValue);
                break;
            case KEY_MBC_BAND_0_RELEASETIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_RELEASETIME, (int)newValue);
                break;
            case KEY_MBC_BAND_0_RATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_RATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_0_THRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_THRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_0_KNEEWIDTH:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_KNEEWIDTH, (int)newValue);
                break;
            case KEY_MBC_BAND_0_NOISEGATETHRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_NOISEGATETHRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_0_EXPANDERRATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_EXPANDERRATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_0_PREGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_PREGAIN, (int)newValue);
                break;
            case KEY_MBC_BAND_0_POSTGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND0_POSTGAIN, (int)newValue);
                break;

            //mbc band 1
            case KEY_MBC_BAND_1_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_MBC_BAND_1_ATTACKTIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_ATTACKTIME, (int)newValue);
                break;
            case KEY_MBC_BAND_1_RELEASETIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_RELEASETIME, (int)newValue);
                break;
            case KEY_MBC_BAND_1_RATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_RATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_1_THRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_THRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_1_KNEEWIDTH:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_KNEEWIDTH, (int)newValue);
                break;
            case KEY_MBC_BAND_1_NOISEGATETHRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_NOISEGATETHRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_1_EXPANDERRATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_EXPANDERRATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_1_PREGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_PREGAIN, (int)newValue);
                break;
            case KEY_MBC_BAND_1_POSTGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND1_POSTGAIN, (int)newValue);
                break;

            //mbc band 2
            case KEY_MBC_BAND_2_CUTOFFFREQUENCY:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_CUTOFFFREQUENCY, (int)newValue);
                break;
            case KEY_MBC_BAND_2_ATTACKTIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_ATTACKTIME, (int)newValue);
                break;
            case KEY_MBC_BAND_2_RELEASETIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_RELEASETIME, (int)newValue);
                break;
            case KEY_MBC_BAND_2_RATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_RATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_2_THRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_THRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_2_KNEEWIDTH:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_KNEEWIDTH, (int)newValue);
                break;
            case KEY_MBC_BAND_2_NOISEGATETHRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_NOISEGATETHRESHOLD, (int)newValue);
                break;
            case KEY_MBC_BAND_2_EXPANDERRATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_EXPANDERRATIO, (int)newValue);
                break;
            case KEY_MBC_BAND_2_PREGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_PREGAIN, (int)newValue);
                break;
            case KEY_MBC_BAND_2_POSTGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_MBC_BAND2_POSTGAIN, (int)newValue);
                break;

            //limiter
            case KEY_LIMITER_ATTACKTIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_ATTACKTIME, (int)newValue);
                break;
            case KEY_LIMITER_RELEASETIME:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_RELEASETIME, (int)newValue);
                break;
            case KEY_LIMITER_RATIO:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_RATIO, (int)newValue);
                break;
            case KEY_LIMITER_THRESHOLD:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_THRESHOLD, (int)newValue);
                break;
            case KEY_LIMITER_POSTGAIN:
                mAudioEffectManager.setDpeParam(AudioEffectManager.SUBCMD_DPE_LIMITER_POSTGAIN, (int)newValue);
                break;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
