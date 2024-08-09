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
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.TwoStatePreference;
import androidx.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.media.AudioDeviceCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.app.SystemControlManager;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;


public class SoundProcessingFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SoundProcessingFragment";
    private static final String KEY_EQUALIZER_SETTINGS                      = "key_equalizer_settings";
    private static final String TV_TREBLE_BASS_SETTINGS                     = "treble_bass_effect_settings";
    private static final String TV_BALANCE_SETTINGS                         = "balance_effect_settings";
    private static final String TV_VIRTUAL_SURROUND_SETTINGS                = "tv_sound_virtual_surround";
    private static final String KEY_DOLBY_DAP_EFFECT                        = "key_dolby_dap_effect";
    private static final String KEY_DOLBY_DAP_EFFECT_2_4                    = "key_dolby_audio_processing_2_4";
    private static final String KEY_DUAL_EFFECT                             = "key_sound_dual_effect";
    private static final String KEY_DUAL_EFFECT_PROCESSING                  = "key_dual_effect_processing";
    private static final String KEY_BASIC_EFFECT_PROCESSING                 = "key_basic_effect_processing";
    private static final String KEY_DTS_VX                                  = "key_dts_virtualx_settings";
    private static final String KEY_DPE                                     = "key_dpe_audio_effect";
    private static final String KEY_DUAL_EFFECTS_CATEGORY                   = "key_dolby_dts_effects_category";
    private static final String KEY_BASIC_EFFECTS_CATEGORY                  = "key_basic_effects_category";

    private TwoStatePreference mDualEffectProcessing;
    private ListPreference mDualEffectPref;
    private TwoStatePreference mBasicProcessingPref;
    private PreferenceCategory mDualEffectsCategoryPref;
    private PreferenceCategory mBasicEffectsCategoryPref;
    private Preference mDap24Pref;
    private Preference mVirtualxPref;

    private AudioEffectManager mAudioEffectManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private DroidAudioManager mDroidAudioManager = null;
    private AudioManager mAudioManager;
    private SystemControlManager mSystemControl;
    private Context mContext = null;

    private static final int UI_LOAD_TIMEOUT = 50;//100ms
    private static final int LOAD_UI = 0;
    private static final int AUDIO_ONLY_INT = 0;

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_UI:
                    if (!initView()) {
                        myHandler.sendEmptyMessageDelayed(LOAD_UI, UI_LOAD_TIMEOUT);
                    } else {
                        myHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        mDroidAudioManager = DroidAudioManager.getInstance(getActivity());
        mAudioEffectManager = ((TvSettingsActivity)getActivity()).getAudioEffectManager();
        mSoundParameterSettingManager = ((TvSettingsActivity)getActivity()).getSoundParameterSettingManager();
    }

    public static SoundProcessingFragment newInstance() {
        return new SoundProcessingFragment();
    }

    @Override
    public void onResume() {
        refreshBasicEffectPref();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        mContext = getActivity();
        mSystemControl = SystemControlManager.getInstance();
        mDroidAudioManager = DroidAudioManager.getInstance(mContext);
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound_processing, null);
        //myHandler.sendEmptyMessage(LOAD_UI);
        initView();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        String key = preference.getKey();
        if (TextUtils.equals(preference.getKey(), KEY_DUAL_EFFECT_PROCESSING)) {
            boolean isDualEffectChecked =  mDualEffectProcessing.isChecked();
            boolean isBasicEffectChecked = mBasicProcessingPref.isChecked();
            checkDualEffectProcessing(isDualEffectChecked);
            refreshDualEffectPref();
            if (isDualEffectChecked && isBasicEffectChecked) { //Turn off BASIC
                mAudioEffectManager.setBasicEffectMode(AudioEffectManager.BASIC_EFFECT_MODE_OFF);
                refreshBasicEffectPref();
            }
        } else if (TextUtils.equals(key, KEY_BASIC_EFFECT_PROCESSING)) {
            boolean isDualEffectChecked =  mDualEffectProcessing.isChecked();
            boolean isBasicEffectChecked = mBasicProcessingPref.isChecked();
            int mode = isBasicEffectChecked ? AudioEffectManager.BASIC_EFFECT_MODE_ON : AudioEffectManager.BASIC_EFFECT_MODE_OFF;
            mAudioEffectManager.setBasicEffectMode(mode);
            refreshBasicEffectPref();
            if (isDualEffectChecked && isBasicEffectChecked) { //Turn off Dual Effect
                checkDualEffectProcessing(false);
                refreshDualEffectPref();
            }
        } else if (TextUtils.equals(preference.getKey(), TV_VIRTUAL_SURROUND_SETTINGS)) {
            final SwitchPreference virtualSurroundPref = (SwitchPreference) findPreference(TV_VIRTUAL_SURROUND_SETTINGS);
            mAudioEffectManager.setVirtualSurround(virtualSurroundPref.isChecked()? 1 : 0);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), KEY_DUAL_EFFECT)) {
            mAudioEffectManager.setDualEffectMode(selection);
            refreshDualEffectPref();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value;
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    private boolean initView() {
        mDualEffectsCategoryPref = (PreferenceCategory) findPreference(KEY_DUAL_EFFECTS_CATEGORY);

        mBasicEffectsCategoryPref = (PreferenceCategory) findPreference(KEY_BASIC_EFFECTS_CATEGORY);

        initDualEffectPref();

        refreshDualEffectPref();

        refreshBasicEffectPref();

        logDebug(TAG, true, "[initView] done! ");
        return true;
    }

    private void refreshBasicEffectPref() {
        int onBasicEffectsCount = getOnBasicAudioEffectsCount();
        int mode = mAudioEffectManager.getBasicEffectMode();
        boolean isBasicProcessingOn = (mode == AudioEffectManager.BASIC_EFFECT_MODE_ON ? true : false);
        mBasicProcessingPref = (TwoStatePreference) findPreference(KEY_BASIC_EFFECT_PROCESSING);
        mBasicProcessingPref.setChecked(isBasicProcessingOn);

        final Preference eqSetting = (Preference) findPreference(KEY_EQUALIZER_SETTINGS);
        if (isBasicProcessingOn && mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_HPEQ_UI_ID)) {
            eqSetting.setVisible(true);
        } else {
            eqSetting.setVisible(false);
        }

        final Preference treblebass = (Preference) findPreference(TV_TREBLE_BASS_SETTINGS);
        if (isBasicProcessingOn && mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_TREBLEBASS_UI_ID)) {
            treblebass.setVisible(true);
            String treblebass_summary = getShowString(R.string.tv_treble, mAudioEffectManager.getTrebleStatus()) + " " +
            getShowString(R.string.tv_bass, mAudioEffectManager.getBassStatus());
            treblebass.setSummary(treblebass_summary);
        } else {
            treblebass.setVisible(false);
        }

        final SwitchPreference virtualsurround = (SwitchPreference) findPreference(TV_VIRTUAL_SURROUND_SETTINGS);
        if (isBasicProcessingOn && mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_VIRTUAL_SURROUND_UI_ID)) {
            int status = mAudioEffectManager.getVirtualSurroundStatus();
            virtualsurround.setChecked(status == 1 ? true : false);
            virtualsurround.setVisible(true);
            logDebug(TAG, true, "virtualsurround status:" + status);
        } else {
            virtualsurround.setVisible(false);
        }

        final Preference dpe = (Preference) findPreference(KEY_DPE);
        if (isBasicProcessingOn && mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_DPE_UI_ID)) {
            dpe.setVisible(true);
        } else {
            dpe.setVisible(false);
        }

        final Preference balance = (Preference) findPreference(TV_BALANCE_SETTINGS);
        if (mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_BALANCE_UI_ID)) {
            balance.setVisible(true);
            balance.setSummary(getShowString(R.string.tv_balance_effect, mAudioEffectManager.getBalanceStatus()));
        } else {
            balance.setVisible(false);
        }

        if (onBasicEffectsCount == 0) {
            mBasicEffectsCategoryPref.setVisible(false);
        } else {
            mBasicEffectsCategoryPref.setVisible(true);
        }
        logDebug(TAG, true, "refreshBasicEffectPref() mode:" + mode + " onBasicEffects:" + onBasicEffectsCount);
    }

    private void initDualEffectPref() {
        mDap24Pref = (Preference) findPreference(KEY_DOLBY_DAP_EFFECT_2_4);;
        mVirtualxPref = (Preference) findPreference(KEY_DTS_VX);

        mDualEffectProcessing =  (TwoStatePreference) findPreference(KEY_DUAL_EFFECT_PROCESSING);
        mDualEffectPref = (ListPreference) findPreference(KEY_DUAL_EFFECT);
        mDualEffectPref.setOnPreferenceChangeListener(this);

        mAudioEffectManager.initDualEffectMode();
        logDebug(TAG, true, "initDualEffectPref()");
    }

    private void checkDualEffectProcessing(boolean isChecked) {
        boolean isVXOn = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_VIRTUALX_UI_ID);
        boolean isDapOn = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_DAP2_UI_ID);
        int curMode = mAudioEffectManager.getDualEffectMode();
        int newMode = AudioEffectManager.EFFECT_MODE_OFF;

        if (isVXOn && isDapOn) {
            newMode = isChecked ?  AudioEffectManager.EFFECT_MODE_AUTO : AudioEffectManager.EFFECT_MODE_OFF;
        } else if (isVXOn) {
            newMode = isChecked ? AudioEffectManager.EFFECT_MODE_DTS : AudioEffectManager.EFFECT_MODE_OFF;
        } else if (isDapOn) {
            newMode = isChecked ? AudioEffectManager.EFFECT_MODE_DOLBY : AudioEffectManager.EFFECT_MODE_OFF;
        }  else {
            newMode = AudioEffectManager.EFFECT_MODE_OFF;
        }

        mAudioEffectManager.setDualEffectMode(newMode);

        logDebug(TAG, true,"checkDualEffectProcessing() newMode: " + newMode + " oldMode:" + curMode);
    }

    private void refreshDualEffectPref() {
        String[] entry = getArrayString(R.array.sound_dual_effect_entries);
        String[] entryValue = getArrayString(R.array.sound_dual_effect_entry_values);
        List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
        List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
        boolean isVXOn = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_VIRTUALX_UI_ID);
        boolean isDapOn = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.EFFECT_DAP2_UI_ID);
        boolean isDualEffectVisible = true;
        //check dual effect change and refresh the setting when UI fresh

        int mode = mAudioEffectManager.getDualEffectMode();

        if (!isVXOn && !isDapOn) {
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_AUTO]);
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_DTS]);
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_DOLBY]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_AUTO]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_DTS]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_DOLBY]);
            isDualEffectVisible = false;
            //DAP & DTS both off
            mDap24Pref.setVisible(false);
            mVirtualxPref.setVisible(false);
            mDualEffectProcessing.setTitle("ALL off");
            mDualEffectProcessing.setVisible(false);
            mDualEffectPref.setVisible(isDualEffectVisible);
            mDualEffectsCategoryPref.setVisible(false);
        } else if (!isVXOn) {
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_AUTO]);
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_DTS]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_AUTO]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_DTS]);
            isDualEffectVisible = false;
            mDualEffectPref.setTitle("Dolby Audio Processing");
            //Only DTS sound on, Using DualEffectProcessing UI to replace DualEffect UI
            mDualEffectProcessing.setTitle("Dolby Audio Processing");
            mVirtualxPref.setVisible(false);
            mDualEffectProcessing.setVisible(true);
            mDualEffectPref.setVisible(isDualEffectVisible);
            mDualEffectsCategoryPref.setTitle("Dolby");
            mDualEffectsCategoryPref.setVisible(true);
            if (mode == AudioEffectManager.EFFECT_MODE_OFF) {
                mDap24Pref.setVisible(false);
            } else {
                mDap24Pref.setVisible(true);
            }
        } else if (!isDapOn) {
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_DOLBY]);
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_AUTO]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_DOLBY]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_AUTO]);
            isDualEffectVisible = false;
            mDualEffectPref.setTitle("DTS Audio Processing");
            //if DAP Off mean MS12 config with Y then disable dolby setting UI
            //if config Y then dialog enhancer impl in Advanced
            mDap24Pref.setVisible(false);
            mDualEffectProcessing.setTitle("DTS Audio Processing");
            mDualEffectProcessing.setVisible(true);
            mDualEffectPref.setVisible(isDualEffectVisible);
            mDualEffectsCategoryPref.setTitle("DTS");
            mDualEffectsCategoryPref.setVisible(true);
            if (mode == AudioEffectManager.EFFECT_MODE_OFF) {
                mVirtualxPref.setVisible(false);
            } else {
                mVirtualxPref.setVisible(true);
            }
        } else {
            //DAP and Virtualx both on, then using DualEffectProcessing to control OFF
            //DualEffect to control mode switch
            entryList.remove(entry[AudioEffectManager.EFFECT_MODE_OFF]);
            entryValueList.remove(entryValue[AudioEffectManager.EFFECT_MODE_OFF]);

            mDualEffectProcessing.setTitle(R.string.title_dual_effect_processing);
            mDualEffectProcessing.setVisible(true);
            if (mode == AudioEffectManager.EFFECT_MODE_OFF) {
                mDualEffectPref.setVisible(false);
                mDap24Pref.setVisible(false);
                mVirtualxPref.setVisible(false);
            } else {
                mDualEffectPref.setVisible(true);
                mDap24Pref.setVisible(true);
                mVirtualxPref.setVisible(true);
            }
            mDualEffectsCategoryPref.setVisible(true);
        }

        mDualEffectPref.setEntries(entryList.toArray(new String[]{}));
        mDualEffectPref.setEntryValues(entryValueList.toArray(new String[]{}));
        mDualEffectPref.setValue(mode + "");
        String dualEffectSummary = entry[mode];
        mDualEffectPref.setSummary(dualEffectSummary);
        mDualEffectPref.setEnabled(true);

        if (mode == AudioEffectManager.EFFECT_MODE_OFF) {
            isDualEffectVisible = false;
            mDualEffectProcessing.setChecked(false);
        } else {
            mDualEffectProcessing.setChecked(true);
        }

        logDebug(TAG, true, "refreshDualEffectPref() -- soundMode:" + mode + "  summary:" + entry[mode]);
    }

    private int getOnBasicAudioEffectsCount() {
        int count = 0;
        for (int id = AudioEffectManager.EFFECT_HPEQ_UI_ID; id <= AudioEffectManager.EFFECT_DPE_UI_ID; id++) {
            if (mAudioEffectManager.isAudioEffectOn(id)) {
                count++;
            }
        }
        return count;
    }
}
