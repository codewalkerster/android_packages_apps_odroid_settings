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

package hardkernel.odroid.settings.soundeffect;

import static hardkernel.odroid.settings.util.InstrumentationUtils.logEntrySelected;
import static hardkernel.odroid.settings.util.InstrumentationUtils.logToggleInteracted;

import android.app.tvsettings.TvSettingsEnums;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.os.SystemProperties;

import androidx.annotation.Keep;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.TwoStatePreference;
import androidx.preference.SeekBarPreference;
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

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.core.AbstractPreferenceController;
import hardkernel.odroid.settings.PreferenceControllerFragment;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsConstant;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import com.droidlogic.app.DroidAudioEffect;
import com.droidlogic.app.DroidLogicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

// DroidLogic start modify, add passthrough feature and remove manual feature.
import java.util.Arrays;
import androidx.preference.PreferenceScreen;
import com.droidlogic.app.DroidAudioManager;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import android.util.Log;
// DroidLogic end
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

/**
 * The "Advanced sound settings" screen in TV Settings.
 */
@Keep
public class AdvancedVolumeFragment extends PreferenceControllerFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG                                          = "AdvancedVolumeFragment";

    private static final String AUDIO_ONLY                                  = "tv_sound_audio_only";
    private static final String KEY_AUDIO_LATENCY                           = "key_audio_latency";
    // DroidLogic start
    static final String KEY_SPDIF_OUTPUT_SWITCH                             = "key_spdif_output_switch";
    static final String KEY_AC4_OUTPUT_SWITCH                               = "key_ac4_output_switch";
    static final String KEY_DOLBY_DOWN_MIX_SWITCH                           = "key_dolby_down_mix";
    static final String KEY_DOLBY_DRC_LINE_LEVEL                            = "key_drc_line_mode";
    static final String KEY_DAP_2_4_LEVELER_SETTING                         = "key_dap_2_4_leveler_setting";
    static final String KEY_DAP_2_4_LEVELER_AMOUNT                          = "key_dap_2_4_leveler_amount";

    static final String KEY_AI_DE                                           = "key_ai_de";
    static final String KEY_AI_DE_GAIN                                      = "key_ai_de_gain";
    // DroidLogic end
    static final String KEY_SUPPORTED_SURROUND_SOUND                        = "supported_formats";
    static final String KEY_UNSUPPORTED_SURROUND_SOUND                      = "unsupported_formats";
    private static final String KEY_DOLBY_DRCMODE_PASSTHROUGH               = "key_dolby_drc_mode";          /* Dolby Sounds */
    private static final String KEY_DTSHD_DRCMODE_PASSTHROUGH               = "dtshd_drc_mode";
    private static final String KEY_DTS_X_DRCMODE                           = "dtsx_drc_mode";
    private static final String KEY_DTSDRCCUSTOMMODE_PASSTHROUGH            = "dtsdrc_custom_mode";
    private static final String KEY_GLOBAL_MIC                              = "key_audio_microphone";

    public static final String DRC_OFF                                      = "off";
    public static final String DRC_LINE                                     = "line";
    public static final String DRC_RF                                       = "rf";

    static final String VAL_SURROUND_SOUND_AUTO                             = "auto";
    static final String VAL_SURROUND_SOUND_NEVER                            = "never";
    static final String VAL_SURROUND_SOUND_MANUAL                           = "manual";
    // DroidLogic start
    static final String VAL_SURROUND_SOUND_ALWAYS                           = "always";
    static final String VAL_SURROUND_SOUND_PASSTHROUGH                      = "passthrough";
    static final String VAL_SURROUND_SOUND_PCM                              = "pcm";
    // DroidLogic end

    static final String KEY_SURROUND_PASSTHROUGH                            = "surround_passthrough";
    static final String KEY_SURROUND_SOUND_FORMAT_PREFIX                    = "surround_sound_format_";
    static final String KEY_SURROUND_SOUND_AUTO                             = "surround_sound_auto";
    static final String KEY_SURROUND_SOUND_NONE                             = "surround_sound_none";
    static final String KEY_SURROUND_SOUND_MANUAL                           = "surround_sound_manual";

    static final int[] SURROUND_SOUND_DISPLAY_ORDER = {
            AudioFormat.ENCODING_AC3,
            AudioFormat.ENCODING_E_AC3,
            AudioFormat.ENCODING_DOLBY_TRUEHD,
            AudioFormat.ENCODING_E_AC3_JOC,
            AudioFormat.ENCODING_DOLBY_MAT,
            AudioFormat.ENCODING_DTS,
            AudioFormat.ENCODING_DTS_HD,
            AudioFormat.ENCODING_DTS_UHD,
            AudioFormat.ENCODING_DRA
    };

    private static final int AUDIO_ONLY_INT                                 = 0;

    private Map<Integer, Boolean> mFormats;
    // private Map<Integer, Boolean> mReportedFormats; R
    private List<Integer> mReportedFormats; //S
    private List<AbstractPreferenceController> mPreferenceControllers;

    // DroidLogic start
    private boolean isFromMainSettings = true;
    private String MainSettings = "MainSettings";
    private DroidAudioManager mDroidAudioManager;
    private SwitchPreference mSpdifSwitchPref;
    // DroidLogic end
    private DroidAudioEffect mDroidAudioEffect;
    private SystemControlManager mSystemControlManager;
    private OutputModeManager mOutputModeManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private AudioManager mAudioManager;

    //dolby dialog enhancer pref if Dolby ms12 config with Y
    private ListPreference mDownMixModePref;
    private ListPreference mAc4DialogEnhancerPref;
    //Leveler for OTT ms12 config with X
    private ListPreference mLePref;
    private SeekBarPreference mLeAmountPref;

    //Dolby/DTS DRC
    private ListPreference mDolbyDrcModePref;
    private SeekBarPreference mDolbyDrcLineLevelPref;
    private ListPreference mDtsHDDrcModePref;
    private ListPreference mDtsDrcCustomModePref;
    private ListPreference mDtsXDrcModePref;

    //Global mic
    private Preference mGlobalMicPref;

    private PreferenceCategory mSupportedFormatsPreferenceCategory;
    private PreferenceCategory mUnsupportedFormatsPreferenceCategory;

    @Override
    public void onAttach(Context context) {
        AudioManager audioManager = getAudioManager();
        mFormats = audioManager.getSurroundFormats();
        mReportedFormats = audioManager.getReportedSurroundFormats();
        // DroidLogic start
        mDroidAudioManager = DroidAudioManager.getInstance(context);
        // DroidLogic end
        mSystemControlManager = SystemControlManager.getInstance();
        mOutputModeManager = OutputModeManager.getInstance(getActivity());
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDroidAudioEffect = DroidAudioEffect.getInstance(getActivity());
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (mDroidAudioEffect != null) {
            final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
            audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI));
        }

        int dolbyDrcMode = mDroidAudioManager.getDolbyDrcMode();
        if (mDolbyDrcModePref.isVisible() &&dolbyDrcMode == DroidAudioManager.DOLBY_DRC_MODE_LINE) {
            int level = mDroidAudioManager.getDolbyDrcLineLevel();
            mDolbyDrcLineLevelPref.setVisible(true);
            mDolbyDrcLineLevelPref.setValue(level);
            mDolbyDrcLineLevelPref.setAdjustable(true);
            Log.d(TAG, "drc level=" + level);
        }
        Log.d(TAG, "onResume drcMode" + dolbyDrcMode);
        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.advanced_sound, null /* key */);

        //Dolby & DTS DRC preference
        initDRCModePref();

        // DroidLogic start
        isFromMainSettings = getActivity().toString().contains(MainSettings) ;
        PreferenceScreen advancedSoundScreenPref = getPreferenceScreen();
        // DroidLogic end
        final ListPreference surroundPref = findPreference(KEY_SURROUND_PASSTHROUGH);
        surroundPref.setValue(getDigitalAudioMode(getContext()));
        surroundPref.setOnPreferenceChangeListener(this);
        if (!isSupportMs12AndPassthrough()) {
            removePassthroughFromFormatEntries(surroundPref);
        }
        // DroidLogic start
        /*
        String surroundPassthroughSetting = getSurroundPassthroughSetting(getContext());
        surroundPref.setValue(surroundPassthroughSetting);
        */
        // DroidLogic end

        // DroidLogic start
        /* createFormatPreferences();
        if (surroundPassthroughSetting == VAL_SURROUND_SOUND_MANUAL) {
            showFormatPreferences();
        } else {
            hideFormatPreferences();
        }*/
        // DroidLogic end

        // DroidLogic start
        //Hide "Select formate" UI if SoundBar Mode is enabled
        if (SettingsConstant.isSoundbarFeature() && mDroidAudioManager.isSoundBarModeEnabled()) {
            surroundPref.setVisible(false);
        }

        /* not support passthrough when ms12 so are not included.*/
        // DroidLogic end

        final Preference audio_only = (Preference) findPreference(AUDIO_ONLY);
        audio_only.setVisible(false); //the function is not finish, temporarily hidden

        final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
        audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(DroidAudioEffect.EFFECT_CONFIG_AUDIO_LATENCY));

        mSpdifSwitchPref = (SwitchPreference) findPreference(KEY_SPDIF_OUTPUT_SWITCH);
        mSpdifSwitchPref.setChecked(mDroidAudioManager.isSoundSpdifEnabled());
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        //if (isTv) {
            mSpdifSwitchPref.setVisible(false);
        //}

        //dolby ac4 dialog enhancer and down mix ui for OTT when config with Y
        int dolbyMs12AudioConfig = mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_DAP);
        int ottMS12Config = mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_OTT_MS12);
        int dualEffectMode = mDroidAudioEffect.getDualEffectMode();
        //DE UI
        mAc4DialogEnhancerPref = findPreference(KEY_AC4_OUTPUT_SWITCH);
        mAc4DialogEnhancerPref.setOnPreferenceChangeListener(this);
        if (!isSupportMs12()) {
            mAc4DialogEnhancerPref.setVisible(false);
            Log.i(TAG, "Not support MS12, Hide AC4 DE");
        } else if ((ottMS12Config == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X || ottMS12Config == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Y)) {
            int uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
            mAc4DialogEnhancerPref.setValueIndex(uiIndex);
            mAc4DialogEnhancerPref.setVisible(true);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show AC4 DE");
        } else if (dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Y) {
            int uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
            mAc4DialogEnhancerPref.setValueIndex(uiIndex);
            mAc4DialogEnhancerPref.setVisible(true);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show AC4 DE");
        } else if ((dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Z || dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X)) {
            if (dualEffectMode == DroidAudioEffect.DUAL_EFFECT_MODE_DOLBY || dualEffectMode == DroidAudioEffect.DUAL_EFFECT_MODE_AUTO) {
                mAc4DialogEnhancerPref.setVisible(false);
                Log.i(TAG, "MS12_Config: X/Z, DAP: enable, Hide AC4 DE UI");
            } else {
                int uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
                mAc4DialogEnhancerPref.setValueIndex(uiIndex);
                mAc4DialogEnhancerPref.setVisible(true);
                Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show AC4 DE");
            }
        } else {
            mAc4DialogEnhancerPref.setVisible(false);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Hide AC4 DE");
        }

        //leveler UI: only OTT_MS12 config with X, Show under advanced UI
        mLePref = (ListPreference) findPreference(KEY_DAP_2_4_LEVELER_SETTING);
        mLeAmountPref = (SeekBarPreference) findPreference(KEY_DAP_2_4_LEVELER_AMOUNT);
        mLePref.setOnPreferenceChangeListener(this);
        mLeAmountPref.setOnPreferenceChangeListener(this);
        if ((ottMS12Config == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X)) {
            int levelMode = mDroidAudioManager.getSoundLevelerMode();
            mLePref.setValueIndex(levelMode);
            mLePref.setVisible(true);
            mLeAmountPref.setMin(0);
            mLeAmountPref.setMax(10);
            mLeAmountPref.setTitle(getShowString(R.string.title_dap_2_4_leveler_amount));
            if (levelMode != DroidAudioManager.DOLBY_SOUND_LEVELER_MODE_OFF) {
                int levelAmount = mDroidAudioManager.getSoundLevelerAmount();
                mLeAmountPref.setValue(levelAmount);
                mLeAmountPref.setVisible(true);
            } else {
                mLeAmountPref.setVisible(false);
            }
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show leveler");
        } else {
            mLePref.setVisible(false);
            mLeAmountPref.setVisible(false);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Hide leveler");
        }

        //DownMix UI
        int downMixMode = mDroidAudioManager.getSoundDmxMode();
        mDownMixModePref = findPreference(KEY_DOLBY_DOWN_MIX_SWITCH);
        mDownMixModePref.setValueIndex(downMixMode);
        mDownMixModePref.setOnPreferenceChangeListener(this);
        if (!isSupportMs12()) {
            mDownMixModePref.setVisible(false);
            Log.i(TAG, "Not support MS12, Hide DownMix");
        } else if ((ottMS12Config == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X || ottMS12Config == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Y)) {
            mDownMixModePref.setVisible(true);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show DownMix");
        } else if (dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Y) {
            mDownMixModePref.setVisible(true);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Show DownMix");
        } else if (dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Z || dolbyMs12AudioConfig == DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X) {
            if ((dualEffectMode == DroidAudioEffect.DUAL_EFFECT_MODE_DOLBY || dualEffectMode == DroidAudioEffect.DUAL_EFFECT_MODE_AUTO)) {
                mDownMixModePref.setVisible(false);
                Log.i(TAG, "MS12_Config: X/Z, DAP: enable, Hide DownMix");
            } else {
                mDownMixModePref.setVisible(true);
                Log.i(TAG, "MS12_Config: X/Z, DAP: enable, Show DownMix");
            }
        } else {
            mDownMixModePref.setVisible(false);
            Log.i(TAG, "MS12_Config: " + ms12Config2String(dolbyMs12AudioConfig) + ", OTT_Config:" + ms12Config2String(ottMS12Config) + ", Hide DownMix");
        }

        //AI DE
        final ListPreference aiDeGainPref = (ListPreference)findPreference(KEY_AI_DE_GAIN);
        aiDeGainPref.setOnPreferenceChangeListener(this);
        aiDeGainPref.setSummary("");
        if (mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_AI_DE) == 1) {
            int mode = mDroidAudioManager.getAiDeGain();
            aiDeGainPref.setValueIndex(mode);
            aiDeGainPref.setVisible(true);
            Log.i(TAG, "onCreatePreference: AIDE is displayed!");
        } else {
            aiDeGainPref.setVisible(false);
            Log.i(TAG, "onCreatePreference: hide AIDE UI!");
        }

        //Global mic
        mGlobalMicPref = findPreference(KEY_GLOBAL_MIC);
        if (mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_GLOBAL_MIC_DEVICE_TYPE_CONFIG) == 0) {
            mGlobalMicPref.setVisible(false);
            Log.i(TAG, "hide global mic setting UI");
        }
    }

    @Override
    protected List<AbstractPreferenceController> onCreatePreferenceControllers(Context context) {
        // DroidLogic start, add passthrough feature and remove manual feature.
        Log.d(TAG,"mDroidAudioManager onCreatePreferenceControllers");
        mDroidAudioManager = DroidAudioManager.getInstance(context);
        // DroidLogic end, add passthrough feature and remove manual feature.
        mPreferenceControllers = new ArrayList<>(mFormats.size());
        for (Map.Entry<Integer, Boolean> format : mFormats.entrySet()) {
            mPreferenceControllers.add(new SoundFormatPreferenceController(context,
                    format.getKey() /*formatId*/, mAudioManager, mFormats, mReportedFormats));
        }
        return mPreferenceControllers;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i(TAG, "[onPreferenceChange] - preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_SURROUND_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
                case VAL_SURROUND_SOUND_AUTO:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_AUTO);
                    // DroidLogic start
                    mDroidAudioManager.setDigitalAudioMode(DroidAudioManager.DIGITAL_AUDIO_MODE_AUTO, "");
                    // DroidLogic end
                    // setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
                    // DroidLogic start
                    // hideFormatPreferences();
                    // DroidLogic end
                    break;
                case VAL_SURROUND_SOUND_NEVER:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_NONE);
                    // DroidLogic start
                    mDroidAudioManager.setDigitalAudioMode(DroidAudioManager.DIGITAL_AUDIO_MODE_PCM, "");
                    // DroidLogic end
                    // setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER);
                    // DroidLogic start
                    // hideFormatPreferences();
                    // DroidLogic end
                    break;
                // DroidLogic start
                case VAL_SURROUND_SOUND_MANUAL:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_MANUAL);
                    mDroidAudioManager.setDigitalAudioMode(DroidAudioManager.DIGITAL_AUDIO_MODE_MANUAL, mDroidAudioManager.getAudioManualFormats());
                    // setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
                    showFormatPreferences();
                    break;
                // DroidLogic end
                // DroidLogic start
                case VAL_SURROUND_SOUND_PASSTHROUGH:
                    Log.d(TAG,"VAL_SURROUND_SOUND_PASSTHROUGH"); //// DroidLogic start, add passthrough feature and remove manual feature.
                    mDroidAudioManager.setDigitalAudioMode(DroidAudioManager.DIGITAL_AUDIO_MODE_PASSTHROUGH, "");
                    //setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
                    break;
                case VAL_SURROUND_SOUND_ALWAYS:
                    // On Android P ALWAYS is replaced by MANUAL.
                    mDroidAudioManager.setDigitalAudioMode(DroidAudioManager.DIGITAL_AUDIO_MODE_ALWAYS);
                    //setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
                    break;
                // DroidLogic end
                /* DroidLogic start
                case VAL_SURROUND_SOUND_ALWAYS:
                    // On Android P ALWAYS is replaced by MANUAL.
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_MANUAL);
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
                    break;
                case VAL_SURROUND_SOUND_MANUAL:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_MANUAL);
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
                    break;
                DroidLogic end */
                // DroidLogic end, add passthrough feature and remove manual feature.
                default:
                    throw new IllegalArgumentException("Unknown surround sound pref value: "
                            + selection);
            }
            updateFormatPreferencesStates();
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_DOLBY_DRCMODE_PASSTHROUGH)) {
            final String selection = (String) newValue;
            int drcMode = 0;
            switch (selection) {
                case DRC_OFF:
                    mDolbyDrcLineLevelPref.setVisible(false);
                    drcMode = DroidAudioManager.DOLBY_DRC_MODE_OFF;
                    break;
                case DRC_LINE:
                    mDolbyDrcLineLevelPref.setVisible(true);
                    drcMode = DroidAudioManager.DOLBY_DRC_MODE_LINE;
                    break;
                case DRC_RF:
                    mDolbyDrcLineLevelPref.setVisible(false);
                    drcMode = DroidAudioManager.DOLBY_DRC_MODE_RF;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown drc mode pref value");
            }
            mDroidAudioManager.setDolbyDrcMode(drcMode);
        } else if (TextUtils.equals(preference.getKey(), KEY_DOLBY_DRC_LINE_LEVEL)) {
            int value = (int) newValue;
            mDroidAudioManager.setDolbyDrcLineLevel(value);
            Log.d(TAG, "setDolbyDrcMode Line = " + value);
        } else if (TextUtils.equals(preference.getKey(), KEY_DTSHD_DRCMODE_PASSTHROUGH)) {
            final String selection = (String) newValue;
            mOutputModeManager.setDtsDrcScale(selection);
            Log.d(TAG, "setDtsDrcScale value = " + selection);
        } else if (TextUtils.equals(preference.getKey(), KEY_DTS_X_DRCMODE)) {
            final String selection = (String) newValue;
            int modeInt = Integer.parseInt(selection);
            mDroidAudioManager.setDtsXDrcEnabled((modeInt != 0)? true : false);
        }  else if (TextUtils.equals(preference.getKey(), KEY_AC4_OUTPUT_SWITCH)) {
            final int selection = Integer.parseInt(newValue.toString());
            boolean supportMs12 = mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_DAP)
                                != DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_N;
            switch (selection) {
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_LEVEL_OFF:
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_LEVEL_LOW:
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_LEVEL_MEDIUM:
                case DroidAudioManager.DIALOGUE_ENHANCEMENT_LEVEL_HIGH:
                    if (supportMs12) {
                        mDroidAudioManager.setDialogEnhancerLevel(selection);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ac4 pref value: "
                            + selection);
            }
            updateFormatPreferencesStates();
            return true;
       } else if (TextUtils.equals(preference.getKey(), KEY_DOLBY_DOWN_MIX_SWITCH)) {
            final int selection = Integer.parseInt(newValue.toString());
            boolean supportMs12 = mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_DAP)
                                != DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_N;
            if (supportMs12) {
                mDroidAudioManager.setSoundDmxMode(selection);
            }
       } else if (TextUtils.equals(preference.getKey(), KEY_DAP_2_4_LEVELER_SETTING)) {
            final int setting = Integer.parseInt((String)newValue);
            mDroidAudioManager.setSoundLevelerMode(setting);
            refreshLevelerOtt();
       }  else if (TextUtils.equals(preference.getKey(), KEY_DAP_2_4_LEVELER_AMOUNT)) {
            mDroidAudioManager.setSoundLevelerAmount((int)newValue);
       } else if (TextUtils.equals(preference.getKey(), KEY_AI_DE_GAIN)) {
            final int selection = Integer.parseInt(newValue.toString());
            int mode = selection;
            if (mode == DroidAudioManager.AI_DE_MODE_OFF) {
                mDroidAudioManager.setAiDeGain(mode);
                if (mDroidAudioManager.isAiDeEnabled()) {
                    mDroidAudioManager.setAiDeEnabled(false);
                }
            } else {
                if (!mDroidAudioManager.isAiDeEnabled()) {
                    mDroidAudioManager.setAiDeEnabled(true);
                }
                mDroidAudioManager.setAiDeGain(mode);
            }
            Log.w(TAG, "Change AI DE Gain selection:" + selection + ", mode:" + mode);
       }

        return true;
    }

    // DroidLogic start
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(key, AUDIO_ONLY)) {
            createUiDialog(AUDIO_ONLY_INT);
        } else if (key.equals(KEY_SPDIF_OUTPUT_SWITCH)) {
            mDroidAudioManager.setSoundSpdifEnabled(mSpdifSwitchPref.isChecked());
        }
        return super.onPreferenceTreeClick(preference);
    }
    // DroidLogic end

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

    @Override
    protected int getPageId() {
        return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS;
    }

    protected int getPreferenceScreenResId() {
        return R.xml.advanced_sound;
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @VisibleForTesting
    AudioManager getAudioManager() {
        return getContext().getSystemService(AudioManager.class);
    }

    private void refreshLevelerOtt() {
        int levelMode = mDroidAudioManager.getSoundLevelerMode();
        if (levelMode != DroidAudioManager.DOLBY_SOUND_LEVELER_MODE_OFF) {
            int levelAmount = mDroidAudioManager.getSoundLevelerAmount();
            mDroidAudioManager.setSoundLevelerAmount(levelAmount);
            mLeAmountPref.setValue(levelAmount);
            mLeAmountPref.setVisible(true);
        } else {
            mLeAmountPref.setVisible(false);
        }
    }

    private void initDRCModePref() {
        mDolbyDrcModePref = (ListPreference) findPreference(KEY_DOLBY_DRCMODE_PASSTHROUGH);
        mDolbyDrcLineLevelPref = (SeekBarPreference) findPreference(KEY_DOLBY_DRC_LINE_LEVEL);
        String drcModeStr = "";
        int dolbyDrcMode = mDroidAudioManager.getDolbyDrcMode();
        switch (dolbyDrcMode) {
            case DroidAudioManager.DOLBY_DRC_MODE_OFF:
                drcModeStr = DRC_OFF;
                break;
            case DroidAudioManager.DOLBY_DRC_MODE_LINE:
                drcModeStr = DRC_LINE;
                break;
            case DroidAudioManager.DOLBY_DRC_MODE_RF:
                drcModeStr = DRC_RF;
                break;
        }
        mDolbyDrcModePref.setValue(drcModeStr);
        mDolbyDrcModePref.setOnPreferenceChangeListener(this);
        mDolbyDrcLineLevelPref.setOnPreferenceChangeListener(this);
        if (!mSystemControlManager.getPropertyBoolean("ro.vendor.platform.support.dolby", false) ||
            mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_DOLBY_DRC) == 0) {
            mDolbyDrcModePref.setVisible(false);
            mDolbyDrcLineLevelPref.setVisible(false);
            Log.d(TAG, "hide Dolby DRC, platform doesn't support dolby");
        } else {
            mDolbyDrcModePref.setVisible(true);
            if (dolbyDrcMode == DroidAudioManager.DOLBY_DRC_MODE_LINE) {
                int level = mDroidAudioManager.getDolbyDrcLineLevel();
                mDolbyDrcLineLevelPref.setVisible(true);
                mDolbyDrcLineLevelPref.setValue(level);
                mDolbyDrcLineLevelPref.setAdjustable(true);
                Log.d(TAG, "drc level=" + level);
            } else {
                mDolbyDrcLineLevelPref.setVisible(false);
            }
        }

        mDtsHDDrcModePref = (ListPreference) findPreference(KEY_DTSHD_DRCMODE_PASSTHROUGH);
        mDtsHDDrcModePref.setValue(mSystemControlManager.getPropertyString("persist.vendor.sys.dtsdrcscale", OutputModeManager.DEFAULT_DRC_SCALE));
        mDtsHDDrcModePref.setOnPreferenceChangeListener(this);

        mDtsXDrcModePref = (ListPreference) findPreference(KEY_DTS_X_DRCMODE);
        mDtsXDrcModePref.setOnPreferenceChangeListener(this);

        mDtsDrcCustomModePref = (ListPreference) findPreference(KEY_DTSDRCCUSTOMMODE_PASSTHROUGH);
        mDtsDrcCustomModePref.setVisible(false);

        if (mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_DTS_DRC) == 0) {
            mDtsHDDrcModePref.setVisible(false);
            mDtsXDrcModePref.setVisible(false);
            Log.d(TAG, "hide DTS DRC");
        } else {
            if (mDroidAudioManager.isDtsXEnabled()) {
                mDtsXDrcModePref.setValue(mDroidAudioManager.isDtsXDrcEnabled() ? "1" : "0");
                mDtsHDDrcModePref.setVisible(false);
                mDtsXDrcModePref.setVisible(true);
                Log.d(TAG, "initDRCModePref: DTS-X enabled");
            } else {
                mDtsHDDrcModePref.setVisible(true);
                mDtsXDrcModePref.setVisible(false);
                Log.d(TAG, "initDRCModePref: DTS-HD enabled");
            }
        }

        /* TBD: Invalid property test, would be removed
        if (!mSystemControlManager.getPropertyBoolean("ro.vendor.platform.support.dts", false)) {
            mDtsHDDrcModePref.setVisible(false);
            mDtsDrcCustomModePref.setVisible(false);
            Log.d(TAG, "platform doesn't support dts");
        } else if (mSystemControlManager.getPropertyBoolean("persist.vendor.sys.dtsdrccustom", false)) {
            mDtsHDDrcModePref.setVisible(false);
        } else {
            mDtsDrcCustomModePref.setVisible(false);
        }
        */
        Log.d(TAG, "initDRCModePref() done");
    }

    private boolean isSupportMs12() {
        int dolbyMs12Config = mDroidAudioManager.getDroidAudioConfig(DroidAudioManager.DROID_AUDIO_CONFIG_ID_IS_SUPPORT_MS12);
        return (dolbyMs12Config == 1 ? true : false);
    }

    private boolean isSupportMs12AndPassthrough() {
        boolean isSupportPassthrough = (mDroidAudioEffect.getEffectFunctionConfig(mDroidAudioEffect.EFFECT_CONFIG_PASSTHROUGH) == 1);
        return isSupportMs12() && isSupportPassthrough;
    }

    private void removePassthroughFromFormatEntries(ListPreference pref) {
        String[] entry = getArrayString(R.array.surround_sound_entries);
        String[] entryValue = getArrayString(R.array.surround_sound_entry_values);
        List<String> entryList = new ArrayList<>(Arrays.asList(entry));
        List<String> entryValueList = new ArrayList<>(Arrays.asList(entryValue));
        entryList.remove(getActivity().getResources().getString(
                R.string.surround_sound_passthrough_summary));
        entryValueList.remove("passthrough");
        pref.setEntries(entryList.toArray(new String[]{}));
        pref.setEntryValues(entryValueList.toArray(new String[]{}));
        Log.i(TAG, "Removed passthrough from UI entries");
    }

    /** Creates titles and switches for each surround sound format. */
    private void createFormatPreferences() {
        mSupportedFormatsPreferenceCategory = createPreferenceCategory(
                R.string.surround_sound_supported_title,
                KEY_SUPPORTED_SURROUND_SOUND);
        getPreferenceScreen().addPreference(mSupportedFormatsPreferenceCategory);
        mUnsupportedFormatsPreferenceCategory = createPreferenceCategory(
                R.string.surround_sound_unsupported_title,
                KEY_UNSUPPORTED_SURROUND_SOUND);
        getPreferenceScreen().addPreference(mUnsupportedFormatsPreferenceCategory);

          for (int formatId : SURROUND_SOUND_DISPLAY_ORDER) {
              if (mFormats.containsKey(formatId)) {
                  boolean enabled = mFormats.get(formatId);
                  // If the format is not a known surround sound format, do not create a preference
                  // for it.
                  int titleId = getFormatDisplayResourceId(formatId);
                  if (titleId == -1) {
                      continue;
                  }
                  final SwitchPreference pref = new SwitchPreference(getContext()) {
                      @Override
                      public void onBindViewHolder(PreferenceViewHolder holder) {
                          super.onBindViewHolder(holder);
                          // Enabling the view will ensure that the preference is focusable even if it
                          // the preference is disabled. This allows the user to scroll down over the
                          // disabled surround sound formats and see them all.
                          holder.itemView.setEnabled(true);
                      }
                  };
                  pref.setTitle(titleId);
                  pref.setKey(KEY_SURROUND_SOUND_FORMAT_PREFIX + formatId);
                  pref.setChecked(enabled);
                  if (getEntryId(formatId) != -1) {
                      pref.setOnPreferenceClickListener(
                              preference -> {
                                  logToggleInteracted(getEntryId(formatId), pref.isChecked());
                                  return false;
                              }
                      );
                  }
                  if (mReportedFormats.contains(formatId)) {
                      mSupportedFormatsPreferenceCategory.addPreference(pref);
                  } else {
                      mUnsupportedFormatsPreferenceCategory.addPreference(pref);
                  }
              }
          }
    }

    private void showFormatPreferences() {
        getPreferenceScreen().addPreference(mSupportedFormatsPreferenceCategory);
        getPreferenceScreen().addPreference(mUnsupportedFormatsPreferenceCategory);
        updateFormatPreferencesStates();
    }

    private void hideFormatPreferences() {
        getPreferenceScreen().removePreference(mSupportedFormatsPreferenceCategory);
        getPreferenceScreen().removePreference(mUnsupportedFormatsPreferenceCategory);
        updateFormatPreferencesStates();
    }

    private PreferenceCategory createPreferenceCategory(int titleResourceId, String key) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(getContext());
        preferenceCategory.setTitle(titleResourceId);
        preferenceCategory.setKey(key);
        return preferenceCategory;
    }

    /**
     * @return the display id for each surround sound format.
     */
    private int getFormatDisplayResourceId(int formatId) {
        switch (formatId) {
            case AudioFormat.ENCODING_AC3:
                return R.string.surround_sound_format_ac3;
            case AudioFormat.ENCODING_E_AC3:
                return R.string.surround_sound_format_e_ac3;
            case AudioFormat.ENCODING_DTS:
                return R.string.surround_sound_format_dts;
            case AudioFormat.ENCODING_DTS_HD:
                return R.string.surround_sound_format_dts_hd;
            case AudioFormat.ENCODING_DOLBY_TRUEHD:
                return R.string.surround_sound_format_dolby_truehd;
            case AudioFormat.ENCODING_E_AC3_JOC:
                return R.string.surround_sound_format_e_ac3_joc;
            case AudioFormat.ENCODING_DOLBY_MAT:
                return R.string.surround_sound_format_dolby_mat;
            default:
                return -1;
        }
    }

    private void updateFormatPreferencesStates() {
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            Preference preference = findPreference(
                    controller.getPreferenceKey());
            if (preference != null) {
                controller.updateState(preference);
            }
        }
    }

    private void setSurroundPassthroughSetting(int newVal) {
        Settings.Global.putInt(getContext().getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT, newVal);
    }

    static String getSurroundPassthroughSetting(Context context) {
        final int value = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT,
                Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);

        switch (value) {
            case Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL:
                return VAL_SURROUND_SOUND_MANUAL;
            case Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER:
                return VAL_SURROUND_SOUND_NEVER;
            // DroidLogic start, add passthrough feature and remove manual feature.
            case Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS:
                 return VAL_SURROUND_SOUND_ALWAYS;
            // DroidLogic end, add passthrough feature and remove manual feature.
            case Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO:
            default:
                return VAL_SURROUND_SOUND_AUTO;
            // DroidLogic start,
            // On Android P ALWAYS is replaced by MANUAL.
            //case Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS:
            //case Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL:
            //    return VAL_SURROUND_SOUND_MANUAL;
            // DroidLogic end
        }
    }

    // DroidLogic start
    public String getDigitalAudioMode(Context context) {
        final int valueGoogle = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT,
                Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
        if (valueGoogle == Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER) {
            return VAL_SURROUND_SOUND_NEVER;
        }
        final int value = mDroidAudioManager.getDigitalAudioMode();
        Log.d(TAG, "getDigitalAudioMode:" + DroidAudioManager.digitalModeToString(value));
        String format = "";
        switch (value) {
        case DroidAudioManager.DIGITAL_AUDIO_MODE_PCM:
            format = VAL_SURROUND_SOUND_PCM;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_MODE_MANUAL:
            format = VAL_SURROUND_SOUND_MANUAL;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_MODE_AUTO:
            format = VAL_SURROUND_SOUND_AUTO;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_MODE_PASSTHROUGH:
            format = VAL_SURROUND_SOUND_PASSTHROUGH;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_MODE_ALWAYS:
            format = VAL_SURROUND_SOUND_ALWAYS;
            break;
        default:
            format = VAL_SURROUND_SOUND_AUTO;
        }
        return format;
    }
    // DroidLogic end

    private int getEntryId(int formatId) {
        switch (formatId) {
            case AudioFormat.ENCODING_AC4:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DAC4;
            case AudioFormat.ENCODING_E_AC3_JOC:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DADDP;
            case AudioFormat.ENCODING_AC3:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DD;
            case AudioFormat.ENCODING_E_AC3:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DDP;
            case AudioFormat.ENCODING_DTS:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DTS;
            case AudioFormat.ENCODING_DTS_HD:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DTSHD;
            case AudioFormat.ENCODING_DTS_UHD:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DTSUHD;
            case AudioFormat.ENCODING_AAC_LC:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_AAC;
            case AudioFormat.ENCODING_DOLBY_TRUEHD:
                return TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_DTHD;
            default:
                return -1;
        }
    }

    private void createUiDialog (int type) {
        Context context = (Context) (getActivity());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.xml.layout_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
        TextView button_cancel = (TextView)view.findViewById(R.id.dialog_cancel);
        TextView dialogtitle = (TextView)view.findViewById(R.id.dialog_title);
        TextView dialogdetails = (TextView)view.findViewById(R.id.dialog_details);
        if (AUDIO_ONLY_INT == type) {
            dialogtitle.setText(getActivity().getResources().getString(R.string.title_tv_sound_audio_only));
            dialogdetails.setText(getActivity().getResources().getString(R.string.msg_tv_sound_audio_only));
        }
        button_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });
        button_cancel.requestFocus();
        TextView button_ok = (TextView)view.findViewById(R.id.dialog_ok);
        button_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AUDIO_ONLY_INT == type) {
                    SystemProperties.set("persist.audio.only.state", "true");
                }
                mAlertDialog.dismiss();
            }
        });
    }

    private String getShowString(int resid) {
        return getActivity().getResources().getString(resid);
    }

    private String ms12Config2String(int value) {
        switch (value) {
            case DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_N:
                return "N";
            case DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Z:
                return "Z";
            case DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_X:
                return "X";
            case DroidAudioEffect.EFFECT_CONFIG_DAP_MS12_Y:
                return "Y";
            default: {
                return "Unknown";
            }
        }
    }
}
