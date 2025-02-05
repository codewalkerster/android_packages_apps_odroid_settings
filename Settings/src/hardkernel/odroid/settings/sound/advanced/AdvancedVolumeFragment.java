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
import com.android.settingslib.core.AbstractPreferenceController;
import hardkernel.odroid.settings.PreferenceControllerFragment;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsConstant;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import com.droidlogic.app.AudioEffectManager;
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
    // DroidLogic end
    static final String KEY_SUPPORTED_SURROUND_SOUND                        = "supported_formats";
    static final String KEY_UNSUPPORTED_SURROUND_SOUND                      = "unsupported_formats";
    private static final String KEY_DOLBY_DRCMODE_PASSTHROUGH               = "key_dolby_drc_mode";          /* Dolby Sounds */
    private static final String KEY_DTSHD_DRCMODE_PASSTHROUGH               = "dtshd_drc_mode";
    private static final String KEY_DTS_X_DRCMODE                           = "dtsx_drc_mode";
    private static final String KEY_DTSDRCCUSTOMMODE_PASSTHROUGH            = "dtsdrc_custom_mode";

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
    private AudioEffectManager mAudioEffectManager;
    private SystemControlManager mSystemControlManager;
    private OutputModeManager mOutputModeManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private AudioManager mAudioManager;

    //dolby dialog enhancer pref if Dolby ms12 config with Y
    private ListPreference mDownMixModePref;
    private ListPreference mAc4DialogEnhancerPref;

    //Dolby/DTS DRC
    private ListPreference mDolbyDrcModePref;
    private SeekBarPreference mDolbyDrcLineLevelPref;
    private ListPreference mDtsHDDrcModePref;
    private ListPreference mDtsDrcCustomModePref;
    private ListPreference mDtsXDrcModePref;

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
        mAudioEffectManager = AudioEffectManager.getInstance(getActivity());
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (mAudioEffectManager != null) {
            final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
            audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI));
        }

        int dolbyDrcMode = mDroidAudioManager.getDolbyDrcMode();
        if (dolbyDrcMode == DroidAudioManager.IS_DRC_LINE) {
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
        surroundPref.setValue(getDigitalAudioFormat(getContext()));

        // DroidLogic start
        /*
        String surroundPassthroughSetting = getSurroundPassthroughSetting(getContext());
        surroundPref.setValue(surroundPassthroughSetting);
        */
        // DroidLogic end
        surroundPref.setOnPreferenceChangeListener(this);

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
        if (!mDroidAudioManager.isAudioSupportMs12System()) {
            String[] entry = getArrayString(R.array.surround_sound_entries);
            String[] entryValue = getArrayString(R.array.surround_sound_entry_values);
            List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
            List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
            entryList.remove(getActivity().getResources().getString(R.string.surround_sound_passthrough_summary));
            entryValueList.remove("passthrough");
            surroundPref.setEntries(entryList.toArray(new String[]{}));
            surroundPref.setEntryValues(entryValueList.toArray(new String[]{}));
            Log.i(TAG, "current platform not support ms12");
        } else if (mAudioEffectManager.getEffectFunctionConfig(AudioEffectManager.PASSTHROUGH_UI_ID) == 0) {
            String[] entry = getArrayString(R.array.surround_sound_entries);
            String[] entryValue = getArrayString(R.array.surround_sound_entry_values);
            List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
            List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
            entryList.remove(getActivity().getResources().getString(R.string.surround_sound_passthrough_summary));
            entryValueList.remove("passthrough");
            surroundPref.setEntries(entryList.toArray(new String[]{}));
            surroundPref.setEntryValues(entryValueList.toArray(new String[]{}));
            Log.i(TAG, "remove passthrough UI");
        }
        // DroidLogic end

        final Preference audio_only = (Preference) findPreference(AUDIO_ONLY);
        audio_only.setVisible(false); //the function is not finish, temporarily hidden

        final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
        audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(AudioEffectManager.AUDIO_LATENCY_UI_ID));

        mSpdifSwitchPref = (SwitchPreference) findPreference(KEY_SPDIF_OUTPUT_SWITCH);
        mSpdifSwitchPref.setChecked(mDroidAudioManager.getSoundSpdifEnable());
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        //SWPL-196260 removed this UI
        //if (isTv) {
        mSpdifSwitchPref.setVisible(false);
        //}

        int downMixMode = mDroidAudioManager.getSoundDmxMode();
        mDownMixModePref = findPreference(KEY_DOLBY_DOWN_MIX_SWITCH);
        mDownMixModePref.setValueIndex(downMixMode);
        mDownMixModePref.setOnPreferenceChangeListener(this);

        //dolby ac4 dialog enhancer and down mix ui for OTT when config with Y
        int uiIndex = mDroidAudioManager.getDialogEnhancerLevel();
        mAc4DialogEnhancerPref = findPreference(KEY_AC4_OUTPUT_SWITCH);
        mAc4DialogEnhancerPref.setValueIndex(uiIndex);
        mAc4DialogEnhancerPref.setOnPreferenceChangeListener(this);
        // if not support ms12 or TV project, hide the mAc4DialogEnhancerPref
        if (mAudioEffectManager.getDolbyMS12AudioConfig() != AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Y) {
            advancedSoundScreenPref.removePreference(mAc4DialogEnhancerPref);
        }

        if (mAudioEffectManager.getDolbyMS12AudioConfig() == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_N) {
            advancedSoundScreenPref.removePreference(mDownMixModePref);
        } else if (mAudioEffectManager.getDolbyMS12AudioConfig() == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_Z ||
                mAudioEffectManager.getDolbyMS12AudioConfig() == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_X) {
            if (mAudioEffectManager.getDualEffectMode() == AudioEffectManager.EFFECT_MODE_DOLBY ||
                mAudioEffectManager.getDualEffectMode() == AudioEffectManager.EFFECT_MODE_AUTO) {
                advancedSoundScreenPref.removePreference(mDownMixModePref);
                Log.i(TAG, "DAP is on, remove DownMix UI");
            }
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
        if (TextUtils.equals(preference.getKey(), KEY_SURROUND_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
                case VAL_SURROUND_SOUND_AUTO:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_AUTO);
                    // DroidLogic start
                    mDroidAudioManager.saveDigitalAudioFormatToHal(DroidAudioManager.DIGITAL_AUDIO_FORMAT_AUTO, "");
                    // DroidLogic end
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
                    // DroidLogic start
                    // hideFormatPreferences();
                    // DroidLogic end
                    break;
                case VAL_SURROUND_SOUND_NEVER:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_NONE);
                    // DroidLogic start
                    mDroidAudioManager.saveDigitalAudioFormatToHal(DroidAudioManager.DIGITAL_AUDIO_FORMAT_PCM, "");
                    // DroidLogic end
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER);
                    // DroidLogic start
                    // hideFormatPreferences();
                    // DroidLogic end
                    break;
                /* DroidLogic start
                case VAL_SURROUND_SOUND_MANUAL:
                    logEntrySelected(
                            TvSettingsEnums.DISPLAY_SOUND_ADVANCED_SOUNDS_SELECT_FORMATS_MANUAL);
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
                    showFormatPreferences();
                DroidLogic end*/
                // DroidLogic start
                case VAL_SURROUND_SOUND_PASSTHROUGH:
                    Log.d(TAG,"VAL_SURROUND_SOUND_PASSTHROUGH"); //// DroidLogic start, add passthrough feature and remove manual feature.
                    mDroidAudioManager.saveDigitalAudioFormatToHal(DroidAudioManager.DIGITAL_AUDIO_FORMAT_PASSTHROUGH, "");
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
                    break;
                case VAL_SURROUND_SOUND_ALWAYS:
                    // On Android P ALWAYS is replaced by MANUAL.
                    mDroidAudioManager.setDigitalAudioFormatOut(DroidAudioManager.DIGITAL_AUDIO_FORMAT_PASSTHROUGH);
                    setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
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
                    drcMode = DroidAudioManager.IS_DRC_OFF;
                    break;
                case DRC_LINE:
                    mDolbyDrcLineLevelPref.setVisible(true);
                    drcMode = DroidAudioManager.IS_DRC_LINE;
                    break;
                case DRC_RF:
                    mDolbyDrcLineLevelPref.setVisible(false);
                    drcMode = DroidAudioManager.IS_DRC_RF;
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
            mDroidAudioManager.setDtsXDrcMode(modeInt);
            Log.d(TAG, "setDtsXDrcMode value = " + modeInt);
        }  else if (TextUtils.equals(preference.getKey(), KEY_AC4_OUTPUT_SWITCH)) {
            final int selection = Integer.parseInt(newValue.toString());
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
            updateFormatPreferencesStates();
            return true;
       } else if (TextUtils.equals(preference.getKey(), KEY_DOLBY_DOWN_MIX_SWITCH)) {
            final int selection = Integer.parseInt(newValue.toString());
            if (mDroidAudioManager.isAudioSupportMs12System()) {
                mDroidAudioManager.setSoundDmxMode(selection);
            }
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
            mDroidAudioManager.setSoundSpdifEnable(mSpdifSwitchPref.isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
    // DroidLogic end

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

    private void initDRCModePref() {
        mDolbyDrcModePref = (ListPreference) findPreference(KEY_DOLBY_DRCMODE_PASSTHROUGH);
        mDolbyDrcLineLevelPref = (SeekBarPreference) findPreference(KEY_DOLBY_DRC_LINE_LEVEL);
        String drcModeStr = "";
        int dolbyDrcMode = mDroidAudioManager.getDolbyDrcMode();
        switch (dolbyDrcMode) {
            case DroidAudioManager.IS_DRC_OFF:
                drcModeStr = DRC_OFF;
                break;
            case DroidAudioManager.IS_DRC_LINE:
                drcModeStr = DRC_LINE;
                break;
            case DroidAudioManager.IS_DRC_RF:
                drcModeStr = DRC_RF;
                break;
        }
        mDolbyDrcModePref.setValue(drcModeStr);
        mDolbyDrcModePref.setOnPreferenceChangeListener(this);
        mDolbyDrcLineLevelPref.setOnPreferenceChangeListener(this);
        if (!mSystemControlManager.getPropertyBoolean("ro.vendor.platform.support.dolby", false) ||
            !mSoundParameterSettingManager.isDebugAudioOn(AudioEffectManager.DOLBY_DRC_UI_ID) ||
            mAudioEffectManager.getDolbyMS12AudioConfig() == AudioEffectManager.DOLBY_MS12_AUDIO_CONFIG_N) {
            mDolbyDrcModePref.setVisible(false);
            mDolbyDrcLineLevelPref.setVisible(false);
            Log.d(TAG, "hide Dolby DRC Control UI");
        }

        if (dolbyDrcMode == DroidAudioManager.IS_DRC_LINE) {
            int level = mDroidAudioManager.getDolbyDrcLineLevel();
            mDolbyDrcLineLevelPref.setVisible(true);
            mDolbyDrcLineLevelPref.setValue(level);
            mDolbyDrcLineLevelPref.setAdjustable(true);
            Log.d(TAG, "drc level=" + level);
        } else {
            mDolbyDrcLineLevelPref.setVisible(false);
        }

        mDtsHDDrcModePref = (ListPreference) findPreference(KEY_DTSHD_DRCMODE_PASSTHROUGH);
        mDtsHDDrcModePref.setValue(mSystemControlManager.getPropertyString("persist.vendor.sys.dtsdrcscale", OutputModeManager.DEFAULT_DRC_SCALE));
        mDtsHDDrcModePref.setOnPreferenceChangeListener(this);

        mDtsXDrcModePref = (ListPreference) findPreference(KEY_DTS_X_DRCMODE);
        mDtsXDrcModePref.setOnPreferenceChangeListener(this);

        mDtsDrcCustomModePref = (ListPreference) findPreference(KEY_DTSDRCCUSTOMMODE_PASSTHROUGH);
        mDtsDrcCustomModePref.setVisible(false);

        if (!mSoundParameterSettingManager.isDebugAudioOn(AudioEffectManager.DTS_DRC_UI_ID)) {
            mDtsHDDrcModePref.setVisible(false);
            mDtsXDrcModePref.setVisible(false);
        } else {
            if (mDroidAudioManager.isDtsXEnable()) {
                int dtsDRCMode = mDroidAudioManager.getDtsXDrcMode();
                String dtsDRCModeStr = "";
                if (dtsDRCMode == DroidAudioManager.DTS_X_DRC_OFF) {
                    dtsDRCModeStr = "0";
                } else {
                    dtsDRCModeStr = "1";
                }
                mDtsXDrcModePref.setValue(dtsDRCModeStr);
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
                 return VAL_SURROUND_SOUND_PASSTHROUGH;
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
    public String getDigitalAudioFormat(Context context) {
        final int valueGoogle = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT,
                Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
        if (valueGoogle == Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER) {
            return VAL_SURROUND_SOUND_NEVER;
        }
        final int value = Settings.Global.getInt(context.getContentResolver(),
                DroidAudioManager.DIGITAL_AUDIO_FORMAT, DroidAudioManager.DIGITAL_AUDIO_FORMAT_AUTO);
        Log.d(TAG, "getDigitalAudioFormat value = " + value);
        String format = "";
        switch (value) {
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_PCM:
            format = VAL_SURROUND_SOUND_PCM;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_MANUAL:
            format = VAL_SURROUND_SOUND_MANUAL;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_AUTO:
            format = VAL_SURROUND_SOUND_AUTO;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_PASSTHROUGH:
            format = VAL_SURROUND_SOUND_PASSTHROUGH;
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

}
