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

package hardkernel.odroid.settings.soundeffect;
import android.content.Context;
import android.provider.Settings;

import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import android.util.Log;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import com.droidlogic.app.AudioEffectManager;
import hardkernel.odroid.settings.SettingsConstant;
import com.droidlogic.app.DroidAudioManager;
import android.util.Log;

public class Dap_2_4_Fragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Dap_2_4_ModeFragment";
    private static final boolean DEBUG_ENABLE_BASS_ENHANCER_UI = false;
    private static final boolean DEBUG_ENABLE_MI_STEERING_UI = false;

    private static final String KEY_DAP_2_4_SURROUND_VIRTUALIZER_MODE  = "key_dap_2_4_surround_virtualizer_mode";
    private static final String KEY_DAP_2_4_SURROUND_VIRTUALIZER_BOOST = "key_dap_2_4_surround_virtualizer_boost";
    private static final String KEY_DAP_2_4_SURROUND_DECODER_ENABLE    = "key_dap_2_4_surround_decoder_enable";
    private static final String KEY_DAP_2_4_LEVELER_SETTING            = "key_dap_2_4_leveler_setting";
    private static final String KEY_DAP_2_4_LEVELER_AMOUNT             = "key_dap_2_4_leveler_amount";
    private static final String KEY_AC4_OUTPUT_SWITCH                  = "key_ac4_output_switch";

    //Z: preference
    private ListPreference mSuvPref;
    private SeekBarPreference mSuvBoostPref;

    //Y: preference
    private ListPreference mAc4DialogEnhancerPref;

    private TwoStatePreference mSdePref;
    //X: preference
    private ListPreference mSoundModePref;
    private ListPreference mLePref;
    private SeekBarPreference mLeAmountPref;

    private DroidAudioManager mDroidAudioManager;
    private AudioEffectManager mAudioEffectManager;
    private int mDolbyMS12AudioConfig;
    private int mSoundMode;
    private boolean mIsTv;
    private boolean mNeedFreshUI = false;

    public static Dap_2_4_Fragment newInstance() {
        return new Dap_2_4_Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mAudioEffectManager == null) {
            mAudioEffectManager = AudioEffectManager.getInstance(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        // DroidLogic start
        mDroidAudioManager = DroidAudioManager.getInstance(context);
        // DroidLogic end
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedFreshUI) {
            update_preference_all(false);
        }
        mNeedFreshUI = true;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dolby_audioeffect_2_4, null);

        mIsTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        mDolbyMS12AudioConfig = mAudioEffectManager.getDolbyMS12AudioConfig();
        mSoundMode = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_PROFILE);

        init();
        mNeedFreshUI = false;
    }

    private String getShowString(int resid) {
        return getActivity().getResources().getString(resid);
    }

    private void init() {
        init_preference_z();
        init_preference_x();
        init_preference_y();
        Log.d(TAG, "init() mDolbyMS12AudioConfig:" + mDolbyMS12AudioConfig);
    }

    private void init_preference_z() {
        int progress = 0;
        int suvMode = 0;
        int progressLe = 0;

        mSuvPref = (ListPreference) findPreference(KEY_DAP_2_4_SURROUND_VIRTUALIZER_MODE);
        mSuvBoostPref = (SeekBarPreference) findPreference(KEY_DAP_2_4_SURROUND_VIRTUALIZER_BOOST);
        mSdePref = (TwoStatePreference) findPreference(KEY_DAP_2_4_SURROUND_DECODER_ENABLE);
        mSdePref.setOnPreferenceChangeListener(this);

        if (mDolbyMS12AudioConfig == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Z) {
            suvMode = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_SURROUND_VIRTUALIZER);
            if (suvMode == 0) {
                mSuvPref.setSummary("OFF");
            } else if (suvMode == 1) {
                mSuvPref.setSummary("ON");
            } else if (suvMode == 2) {
                mSuvPref.setSummary("AUTO");
            }
            mSuvPref.setOnPreferenceChangeListener(this);
            mSuvPref.setValueIndex(suvMode);
            mSuvPref.setVisible(true);

            int val = mAudioEffectManager.getDapParam(AudioEffectManager.SUBCMD_DAP_2_4_SURROUND_VIRTUALIZER_BOOST);
            mSuvBoostPref.setValue(val);
            mSuvBoostPref.setAdjustable(true);
            mSuvBoostPref.setMin(0);
            mSuvBoostPref.setMax(96);
            mSuvBoostPref.setOnPreferenceChangeListener(this);
            mSuvBoostPref.setSeekBarIncrement(1);
            if (suvMode == 0) {
                mSuvBoostPref.setVisible(false);
            } else {
                mSuvBoostPref.setVisible(true);
            }
            mSdePref.setVisible(false);
        } else {
            mSuvPref.setVisible(false);
            mSuvBoostPref.setVisible(false);
            mSdePref.setVisible(false);
        }

    }

    private void init_preference_x() {
        int progressLe = 0;
        mLePref = (ListPreference) findPreference(KEY_DAP_2_4_LEVELER_SETTING);
        mLeAmountPref = (SeekBarPreference) findPreference(KEY_DAP_2_4_LEVELER_AMOUNT);

        if (mDolbyMS12AudioConfig == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y) {
            mLePref.setVisible(false);
            mLeAmountPref.setVisible(false);
        } else {
            progressLe = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_LEVELER);
            mLePref.setValueIndex(progressLe);
            mLePref.setOnPreferenceChangeListener(this);
            mLeAmountPref.setMin(0);
            mLeAmountPref.setMax(10);
            mLeAmountPref.setOnPreferenceChangeListener(this);
            mLePref.setVisible(true);
            if (progressLe != AudioEffectManager.SOUND_EFFECT_DAP_2_4_LEVELER_OFF) {
                mLeAmountPref.setVisible(true);
            } else {
                mLeAmountPref.setVisible(false);
            }
        }
    }

    private void init_preference_y() {
        int progress = 0;
        int uiIndex = 0;
        int val = 0;
        boolean enable;

        mAc4DialogEnhancerPref = findPreference(KEY_AC4_OUTPUT_SWITCH);

        if (mDolbyMS12AudioConfig == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y) {  //Y: No DAP instance
            uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
        } else { //X, Z with DAP
            val = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_DIALOGUE_ENHANCER);
            if (val == 0) {
                uiIndex = 0;
            } else {
                uiIndex = val;
            }
        }
        mAc4DialogEnhancerPref.setValueIndex(uiIndex);
        mAc4DialogEnhancerPref.setOnPreferenceChangeListener(this);
    }

    private void update_preference_all(boolean doSetting) {
        int soundMode = mSoundMode;
        switch (mDolbyMS12AudioConfig) {
            case AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Z:
                update_preference_z(soundMode, doSetting);
                update_preference_x(soundMode, doSetting);
                update_preference_y(soundMode, doSetting);
                break;
            case AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_X:
                update_preference_x(soundMode, doSetting);
                update_preference_y(soundMode, doSetting);
                break;
            case AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y:
                update_preference_y(soundMode, doSetting);
                break;
        }
        Log.d(TAG, "update_preference_all() mDolbyMS12AudioConfig:" + mDolbyMS12AudioConfig);
    }

    // "refresh = true" means when switch sound mode from others to "custom"
    // then force apply stored values to low layer
    private void update_preference_z(int soundMode, boolean doSetting) {
        int suvMode = 0;
        int val = 0;
        suvMode = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_SURROUND_VIRTUALIZER);
        val = mAudioEffectManager.getDapParam(AudioEffectManager.SUBCMD_DAP_2_4_SURROUND_VIRTUALIZER_BOOST);
        mSuvBoostPref.setValue(val);
        mSuvBoostPref.setAdjustable(true);
        mSuvPref.setVisible(true);
        if (suvMode != AudioEffectManager.SOUND_EFFECT_DAP_2_4_SURROUND_VIRTUALIZER_OFF) {
            mSuvBoostPref.setVisible(true);
            if (doSetting) {
                mAudioEffectManager.setDapParam(AudioEffectManager.SUBCMD_DAP_2_4_SURROUND_VIRTUALIZER_BOOST, (int)val);
            }
        } else {
            mSuvBoostPref.setVisible(false);
        }

        boolean enable = (mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_SURROUND_DECODER_ENABLE) != AudioEffectManager.DAP_OFF);
        //mSdePref.setChecked(enable);
        mSdePref.setVisible(false);
    }

    private void update_preference_x(int soundMode, boolean doSetting) {
        int progress_le = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_LEVELER);
        int val = mAudioEffectManager.getDapParam(AudioEffectManager.SUBCMD_DAP_2_4_LEVELER_AMOUNT);
        mLeAmountPref.setValue(val);
        mLeAmountPref.setAdjustable(true);
        mLeAmountPref.setTitle(getShowString(R.string.title_dap_2_4_leveler_amount));
        mLePref.setVisible(true);
        if (progress_le != AudioEffectManager.SOUND_EFFECT_DAP_2_4_LEVELER_OFF) {
            mLeAmountPref.setVisible(true);
            if (doSetting) {
                mAudioEffectManager.setDapParam(AudioEffectManager.SUBCMD_DAP_2_4_LEVELER_AMOUNT, val);
            }
        } else {
            mLeAmountPref.setVisible(false);
        }
    }

    private void update_preference_y(int soundMode, boolean doSetting) {
        int uiIndex = 0;
        int val = 0;
        boolean enable;
        if (mDolbyMS12AudioConfig == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y) {
            uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
            enable = (uiIndex == 0 ? false : true);
        } else {
            val = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_DIALOGUE_ENHANCER);
            if (val == 0) {
                uiIndex = 0;
            } else {
                uiIndex = val;
            }
        }

        mAc4DialogEnhancerPref.setValueIndex(uiIndex);
        mAc4DialogEnhancerPref.setVisible(true);
        if (doSetting) {
            setDialogEnhancer(uiIndex);
        }
        //Log.d(TAG, "update_preference_y() enable:" + enable + ", val:" + val +", uiIndex: " + uiIndex);
    }

    /*
    * Call DroidAudioManager API if Config with Y
    * Call AudioEffectManager API if Config with X/Z
    */
    private void setDialogEnhancer(int selection) {
        int value = 0;
        if (mDolbyMS12AudioConfig == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y) {
            switch (selection) {
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_OFF:
                    if (mDroidAudioManager.isAudioSupportMs12System()) {
                        mDroidAudioManager.setDialogEnhancerLevel(0);
                    }
                    break;
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_LOW:
                    if (mDroidAudioManager.isAudioSupportMs12System()) {
                        mDroidAudioManager.setDialogEnhancerLevel(1);
                    }
                    break;
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_MEDIUM:
                    if (mDroidAudioManager.isAudioSupportMs12System()) {
                        mDroidAudioManager.setDialogEnhancerLevel(2);
                    }
                    break;
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_HIGH:
                    if (mDroidAudioManager.isAudioSupportMs12System()) {
                        mDroidAudioManager.setDialogEnhancerLevel(3);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ac4 pref value: "
                            + selection);
            }
        } else {
            value = selection;
            mAudioEffectManager.setDapParam(AudioEffectManager.CMD_DAP_2_4_DIALOGUE_ENHANCER, value);
            Log.i(TAG, "setDialogEnhancer() mDolbyMS12AudioConfig:" + mDolbyMS12AudioConfig + ", value:" + value);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean isChecked;
        boolean isNeedRefresh = false;
        switch (preference.getKey()) {
            case KEY_DAP_2_4_SURROUND_DECODER_ENABLE:
                updateSoundModeOnUser();
                isChecked = mSdePref.isChecked();
                mAudioEffectManager.setDapParam(AudioEffectManager.CMD_DAP_2_4_SURROUND_DECODER_ENABLE, isChecked?1:0);
                break;
        }

        if (isNeedRefresh) {
            refresh_params_on_mode_change();
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isNeedRefresh = false;
        switch (preference.getKey()) {
            case KEY_DAP_2_4_SURROUND_VIRTUALIZER_MODE:
                final int mode = Integer.parseInt((String)newValue);
                updateSoundModeOnUser();
                mAudioEffectManager.setDapParam(AudioEffectManager.CMD_DAP_2_4_SURROUND_VIRTUALIZER, mode);
                if (mode == 0) {
                    mSuvPref.setSummary("OFF");
                } else if (mode == 1) {
                    mSuvPref.setSummary("ON");
                } else if (mode == 2) {
                    mSuvPref.setSummary("AUTO");
                }
                isNeedRefresh = true;
                break;
            case KEY_DAP_2_4_SURROUND_VIRTUALIZER_BOOST:
                updateSoundModeOnUser();
                mAudioEffectManager.setDapParam(AudioEffectManager.SUBCMD_DAP_2_4_SURROUND_VIRTUALIZER_BOOST, (int)newValue);
                isNeedRefresh = true;
                break;
            case KEY_AC4_OUTPUT_SWITCH:
                final int selection_1 = Integer.parseInt((String)newValue);
                updateSoundModeOnUser();
                setDialogEnhancer(selection_1);
                isNeedRefresh = true;
                break;
            case KEY_DAP_2_4_LEVELER_SETTING:
                final int setting = Integer.parseInt((String)newValue);
                updateSoundModeOnUser();
                mAudioEffectManager.setDapParam(AudioEffectManager.CMD_DAP_2_4_LEVELER, setting);
                if (setting == 0) {
                    mAudioEffectManager.setDapParam(AudioEffectManager.SUBCMD_DAP_2_4_LEVELER_AMOUNT, 0);
                }
                isNeedRefresh = true;
                break;
            case KEY_DAP_2_4_LEVELER_AMOUNT:
                updateSoundModeOnUser();
                mAudioEffectManager.setDapParam(AudioEffectManager.SUBCMD_DAP_2_4_LEVELER_AMOUNT, (int)newValue);
                isNeedRefresh = true;
                break;
        }

        if (isNeedRefresh) {
            refresh_params_on_mode_change();
        }
        return true;
    }

    private void updateSoundModeOnUser() {
        mSoundMode = mAudioEffectManager.getDapParam(AudioEffectManager.CMD_DAP_2_4_PROFILE);
        if (mSoundMode != AudioEffectManager.SOUND_EFFECT_DAP_2_4_PROFILE_USER_SELECTABLE) {
            mAudioEffectManager.setSoundMode(AudioEffectManager.COMMON_SOUND_MODE_CUSTOM);
            Log.d(TAG, "Change sound mode to CUSTOM! because user adjust Customized UI");
        }
    }

    private void refresh_params_on_mode_change()
    {
        update_preference_all(true);
    }
}
