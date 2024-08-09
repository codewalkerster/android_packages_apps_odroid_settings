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
import android.util.Log;

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
import com.droidlogic.tv.settings.SettingsConstant;

public class AudioDeviceRoutingFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SoundModeFragment";
    private static final String KEY_TV_SOUND_AUDIO_DEVICE                   = "key_tv_sound_output_device";
    private static final String KEY_SPEAKER_MUTE                            = "key_speaker_mute";
    private static final String KEY_COEXIST_SPDIF_OTHER                     = "key_coexist_spdif_other";

    private DroidAudioManager mDroidAudioManager = null;
    private AudioManager mAudioManager;
    private SystemControlManager mSystemControl;
    private Context mContext = null;

    private ListPreference mAudioOutputDevPref;;
    private TwoStatePreference mSpeakerMutePref;
    private TwoStatePreference mCoexistSpdifSwitchPref;

    private int mAudioDeviceOutputStrategy = DroidAudioManager.OUTPUT_STRATEGY_AUTO;
    private HashSet<AudioDeviceInfo> mAudioOutputDevices = new HashSet<AudioDeviceInfo>();

    /* index value, refer to array_audio_settings_output_dev_entries in xml*/
    public static final int UI_INDEX_DEVICE_OUT_AUTO                        = 0;
    public static final int UI_INDEX_DEVICE_OUT_SPEAKER                     = 1;
    public static final int UI_INDEX_DEVICE_OUT_SPDIF                       = 2;
    public static final int UI_INDEX_DEVICE_OUT_HDMI_ARC                    = 3;
    public static final int UI_INDEX_DEVICE_OUT_HDMI_OUT                    = 4;
    public static final int UI_INDEX_DEVICE_OUT_HEADPHONE                   = 5;
    public static final int UI_INDEX_DEVICE_OUT_USB                         = 6;
    public static final int UI_INDEX_DEVICE_OUT_BLUETOOTH                   = 7;
    public static final int UI_INDEX_DEVICE_OUT_MAX                         = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getActivity();
        mSystemControl = SystemControlManager.getInstance();
        mDroidAudioManager = DroidAudioManager.getInstance(getActivity());
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerAudioDeviceCallback(mAudioDeviceCallback, null);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mAudioManager.unregisterAudioDeviceCallback(mAudioDeviceCallback);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        refreshDevicesPref();
        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.audio_devices_routing, null);

        mAudioOutputDevPref = (ListPreference) findPreference(KEY_TV_SOUND_AUDIO_DEVICE);
        mAudioOutputDevPref.setOnPreferenceChangeListener(this);
        mAudioDeviceOutputStrategy = mSystemControl.getPropertyInt(DroidAudioManager.PROP_AUDIO_OUTPUT_STRATEGY, DroidAudioManager.OUTPUT_STRATEGY_AUTO);
        if (mAudioDeviceOutputStrategy < DroidAudioManager.OUTPUT_STRATEGY_AUTO || mAudioDeviceOutputStrategy > DroidAudioManager.OUTPUT_STRATEGY_MANUAL) {
            logDebug(TAG, true, "refreshDevicesPref strategy invalid:" + mAudioDeviceOutputStrategy);
            mAudioDeviceOutputStrategy = DroidAudioManager.OUTPUT_STRATEGY_AUTO;
        }

        mSpeakerMutePref = (TwoStatePreference) findPreference(KEY_SPEAKER_MUTE);
        mSpeakerMutePref.setChecked(!mDroidAudioManager.isSpeakerEnabled());

        mCoexistSpdifSwitchPref = (TwoStatePreference) findPreference(KEY_COEXIST_SPDIF_OTHER);
        mCoexistSpdifSwitchPref.setChecked(mSystemControl.getPropertyBoolean(DroidAudioManager.PROP_AUDIO_OUTPUT_SPDIF_COEXIST, true));
        //hide UI if the project is not TV
        if (!DroidLogicUtils.isTv()) {
            mSpeakerMutePref.setVisible(false);
        }
        //hide UI if the project enabled SoundBar Mode
        if (SettingsConstant.isSoundbarFeature() && mDroidAudioManager.isSoundBarModeEnabled()) {
            mCoexistSpdifSwitchPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
           String key = preference.getKey();
        if (TextUtils.equals(key, KEY_TV_SOUND_AUDIO_DEVICE)) {
            refreshDevicesPref();
        }  else if (TextUtils.equals(key, KEY_SPEAKER_MUTE)) {
            mDroidAudioManager.setSpeakerEnabled(!mSpeakerMutePref.isChecked());
        } else if (TextUtils.equals(key, KEY_COEXIST_SPDIF_OTHER)) {
            mDroidAudioManager.setCoexistSpdifOther(mCoexistSpdifSwitchPref.isChecked());
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), KEY_TV_SOUND_AUDIO_DEVICE)) {
            int[] devices = new int[] {indexToAudioDev(selection)};
            mDroidAudioManager.setOutputDevices(devices);
            refreshDevicesPref();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private AudioDeviceCallback mAudioDeviceCallback = new AudioDeviceCallback() {
        @Override
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            refreshDevicesPref();
        }

        @Override
        public void onAudioDevicesRemoved(AudioDeviceInfo[] devices) {
            refreshDevicesPref();
        }

    };

    private void refreshDevicesPref() {
        String[] entry = getArrayString(R.array.tv_sound_output_device_entries);
        String[] entryValue = getArrayString(R.array.tv_sound_output_device_entry_values);
        List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
        List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));

        for (int i = 0; i < UI_INDEX_DEVICE_OUT_MAX; i++ ) {
            if (!isConnectedDev(i)) {
                entryList.remove(getActivity().getResources().getString(indexToStringIndex(i)));
                entryValueList.remove(i + "");
            }
        }

        mAudioOutputDevPref.setEntries(entryList.toArray(new String[]{}));
        mAudioOutputDevPref.setEntryValues(entryValueList.toArray(new String[]{}));

        mAudioDeviceOutputStrategy = mSystemControl.getPropertyInt(DroidAudioManager.PROP_AUDIO_OUTPUT_STRATEGY, DroidAudioManager.OUTPUT_STRATEGY_AUTO);
        int uiIndex = convertDevicesToUiDisplay(mDroidAudioManager.getOutputDevices());
        mAudioOutputDevPref.setValue(uiIndex + "");
        mAudioOutputDevPref.setSummary(getActivity().getResources().getString(indexToStringIndex(uiIndex)));
        boolean isAllowSetOutputDevice = true;
        if (mAudioDeviceOutputStrategy == DroidAudioManager.OUTPUT_STRATEGY_SEMI_AUTO && !isSemiAutoAllowSetDevice()) {
            isAllowSetOutputDevice = false;
        }
        mAudioOutputDevPref.setEnabled(isAllowSetOutputDevice);
    }

    private int convertDevicesToUiDisplay(int[] devices) {
        if (devices == null || devices.length == 0) {
            logDebug(TAG, false, "convertDevicesToUiDisplay devices is null");
            return UI_INDEX_DEVICE_OUT_SPEAKER;
        }
        if (devices.length == 1) {
            return audioDevToIndex(devices[0]);
        } else if (devices.length == 2) {
            logDebug(TAG, false, "convertDevicesToUiDisplay not supported dev0:" + devices[0] + ", dev1:" + devices[1]);
            if (devices[0] == DroidAudioManager.DROID_AUDIO_FORCE_USE_SPDIF) {
                return audioDevToIndex(devices[1]);
            } else {
                return audioDevToIndex(devices[0]);
            }
        }
        logDebug(TAG, false, "convertDevicesToUiDisplay not supported device length:" + devices.length);
        return UI_INDEX_DEVICE_OUT_SPEAKER;
    }

    private int indexToStringIndex(int index) {
        switch (index) {
            case UI_INDEX_DEVICE_OUT_AUTO:
                return R.string.title_tv_sound_output_device_auto;
            case UI_INDEX_DEVICE_OUT_SPEAKER:
                return R.string.title_tv_sound_output_device_speaker;
            case UI_INDEX_DEVICE_OUT_SPDIF:
                return R.string.title_tv_sound_output_device_spdif;
            case UI_INDEX_DEVICE_OUT_HDMI_OUT:
                return R.string.title_tv_sound_output_device_hdmi_out;
            case UI_INDEX_DEVICE_OUT_HEADPHONE:
                return R.string.title_tv_sound_output_device_headphone;
            case UI_INDEX_DEVICE_OUT_HDMI_ARC:
                return R.string.title_tv_sound_output_device_hdmi_arc;
            case UI_INDEX_DEVICE_OUT_USB:
                return R.string.title_tv_sound_output_device_usb;
            case UI_INDEX_DEVICE_OUT_BLUETOOTH:
                return R.string.title_tv_sound_output_device_bluetooth;
            default:
                logDebug(TAG, false, "indexToStringIndex not supported device:" + index);
                return 0;
        }
    }

    private int audioDevToIndex(int device) {
        switch (device) {
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_SPEAKER:
                return UI_INDEX_DEVICE_OUT_SPEAKER;
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_HEADPHONES:
                return UI_INDEX_DEVICE_OUT_HEADPHONE;
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_SPDIF:
                return UI_INDEX_DEVICE_OUT_SPDIF;
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_HDMI:
                if (DroidLogicUtils.isTv()) {
                    return UI_INDEX_DEVICE_OUT_HDMI_ARC;
                } else {
                    return UI_INDEX_DEVICE_OUT_HDMI_OUT;
                }
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_USB:
                return UI_INDEX_DEVICE_OUT_USB;
            case DroidAudioManager.DROID_AUDIO_FORCE_USE_BT_A2DP:
                return UI_INDEX_DEVICE_OUT_BLUETOOTH;
            default:
                logDebug(TAG, false, "audioDevToIndex not supported AudioDeviceInfo device:" + device);
                return UI_INDEX_DEVICE_OUT_SPEAKER;
        }
    }

    private int indexToAudioDev(int index) {
        switch (index) {
            case UI_INDEX_DEVICE_OUT_AUTO:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_NONE;
            case UI_INDEX_DEVICE_OUT_SPEAKER:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_SPEAKER;
            case UI_INDEX_DEVICE_OUT_SPDIF:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_SPDIF;
            case UI_INDEX_DEVICE_OUT_HEADPHONE:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_HEADPHONES;
            case UI_INDEX_DEVICE_OUT_HDMI_ARC:
            case UI_INDEX_DEVICE_OUT_HDMI_OUT:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_HDMI;
            case UI_INDEX_DEVICE_OUT_USB:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_USB;
            case UI_INDEX_DEVICE_OUT_BLUETOOTH:
                return DroidAudioManager.DROID_AUDIO_FORCE_USE_BT_A2DP;
            default:
                logDebug(TAG, false, "indexToAudioDev not supported ui index:" + index);
                return 0;
        }
    }

    private HashSet<Integer> indexToAudioTypes(int index) {
        HashSet<Integer> sinkDevices = new HashSet<>();
        switch (index) {
            case UI_INDEX_DEVICE_OUT_SPEAKER:
                sinkDevices.add(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
                break;
            case UI_INDEX_DEVICE_OUT_SPDIF:
                sinkDevices.add(AudioDeviceInfo.TYPE_LINE_DIGITAL);
                break;
            case UI_INDEX_DEVICE_OUT_HDMI_OUT:
                sinkDevices.add(AudioDeviceInfo.TYPE_HDMI);
                break;
            case UI_INDEX_DEVICE_OUT_HEADPHONE:
                sinkDevices.add(AudioDeviceInfo.TYPE_WIRED_HEADPHONES);
                sinkDevices.add(AudioDeviceInfo.TYPE_WIRED_HEADSET);
                break;
            case UI_INDEX_DEVICE_OUT_HDMI_ARC:
                sinkDevices.add(AudioDeviceInfo.TYPE_HDMI_ARC);
                sinkDevices.add(AudioDeviceInfo.TYPE_HDMI_EARC);
                break;
            case UI_INDEX_DEVICE_OUT_USB:
                sinkDevices.add(AudioDeviceInfo.TYPE_USB_ACCESSORY);
                sinkDevices.add(AudioDeviceInfo.TYPE_USB_DEVICE);
                sinkDevices.add(AudioDeviceInfo.TYPE_USB_HEADSET);
                break;
            case UI_INDEX_DEVICE_OUT_BLUETOOTH:
                sinkDevices.add(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);
                break;
            default:
                // logDebug(TAG, false, "indexToAudioTypes not supported ui index:" + index);
                break;
        }
        return sinkDevices;
    }

    private boolean isSemiAutoAllowSetDevice() {
        AudioDeviceInfo[] outputDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo info : outputDevices) {
            if (info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    info.getType() == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
                    info.getType() == AudioDeviceInfo.TYPE_USB_DEVICE ||
                    info.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
                return false;
            }
        }
        return true;
    }

    private boolean isConnectedDev(int index) {
        if (mAudioDeviceOutputStrategy == DroidAudioManager.OUTPUT_STRATEGY_AUTO) {
            HashSet<Integer> audioTypelist = indexToAudioTypes(index);
            if (audioTypelist.size() == 0) {
                return false;
            }
            AudioDeviceInfo[] outputDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo info : outputDevices) {
                if (info.isSink()) {
                    if (audioTypelist.contains(info.getType())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

}