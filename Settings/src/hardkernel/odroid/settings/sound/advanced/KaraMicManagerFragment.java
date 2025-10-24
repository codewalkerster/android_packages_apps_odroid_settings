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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.TwoStatePreference;
import androidx.preference.SeekBarPreference;
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
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.DroidAudioEffect;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.app.SystemControlManager;

import hardkernel.odroid.settings.TvSettingsActivity;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;


public class KaraMicManagerFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "KaraMicManagerFragment";

    private static final String KEY_GLOBAL_MIC_SWITCH                   = "key_global_mic_switch";
    private static final String KEY_TV_KARAMIC_SOURCE                   = "key_tv_karamic_source";
    private static final String KEY_MIC_MUTE                            = "key_mic_mute";
    private static final String KEY_MIC_GAIN                            = "key_mic_gain";
    private static final String KEY_REVERB_SWITCH                       = "key_reverb_switch";
    private static final String KEY_REVERB_LEVEL                        = "key_reverb_level";

    private DroidAudioEffect mDroidAudioEffect = null;
    private DroidAudioManager mDroidAudioManager = null;
    private AudioManager mAudioManager;
    private SystemControlManager mSystemControl;
    private Context mContext = null;

    private TwoStatePreference  mMicSwitchPref;
    private ListPreference      mMicSourcePref;
    private TwoStatePreference  mMicMutePref;
    private SeekBarPreference   mMicGainPref;
    private TwoStatePreference  mMicReverbPref;
    private SeekBarPreference   mMicReverbLevelPref;

    private int         mSelectedSourcePre = 0;
    private int         mHalMicDeviceType = 0;
    private boolean     mUsbInputAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getActivity();
        mSystemControl = SystemControlManager.getInstance();
        mDroidAudioEffect = DroidAudioEffect.getInstance(getActivity());
        mDroidAudioManager = DroidAudioManager.getInstance(getActivity());
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerAudioDeviceCallback(mAudioDeviceCallback, null);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.karamic, null);

        mHalMicDeviceType = mDroidAudioEffect.getEffectFunctionConfig(DroidAudioEffect.EFFECT_CONFIG_GLOBAL_MIC_DEVICE_TYPE_CONFIG);
        mSelectedSourcePre = mDroidAudioManager.getMicSource();

        mMicSwitchPref = (TwoStatePreference) findPreference(KEY_GLOBAL_MIC_SWITCH);
        //check mic source change in runtime
        if (Integer.bitCount(mHalMicDeviceType) == 1) {
            if ((mHalMicDeviceType & DroidAudioEffect.MIC_SOURCE_CONFIG_LINE_IN) != 0) {
                mDroidAudioManager.setMicSource(DroidAudioManager.MIC_SOURCE_TYPE_LINE_IN);
            } else if ((mHalMicDeviceType & DroidAudioEffect.MIC_SOURCE_CONFIG_USB_IN) != 0) {
                mDroidAudioManager.setMicSource(DroidAudioManager.MIC_SOURCE_TYPE_USB_IN);
            }
            //reset if mic source been changed
            reset();
        }

        int currentSource = mDroidAudioManager.getMicSource();
        boolean isMicEnabled = mDroidAudioManager.getGlobalMicStatus(currentSource);
        mMicSwitchPref.setChecked(isMicEnabled);
        mUsbInputAvailable = isUsbAudioInputAvailable();

        mMicSourcePref = (ListPreference) findPreference(KEY_TV_KARAMIC_SOURCE);
        mMicSourcePref.setOnPreferenceChangeListener(this);
        mMicSourcePref.setValueIndex(currentSource == DroidAudioManager.MIC_SOURCE_TYPE_LINE_IN? 0 : 1);
        if (Integer.bitCount(mHalMicDeviceType) < 2 ) {
            mMicSourcePref.setVisible(false);
        } else if (!mUsbInputAvailable) {
            mMicSourcePref.setVisible(false);
            Log.i(TAG, "onCreatePreferences: USB input is not available");
        } else {
            mMicSourcePref.setVisible(true);
        }

        mMicMutePref = (TwoStatePreference) findPreference(KEY_MIC_MUTE);
        mMicMutePref.setChecked(mDroidAudioManager.isMicMute(currentSource));
        mMicMutePref.setVisible(isMicEnabled);

        mMicGainPref = (SeekBarPreference) findPreference(KEY_MIC_GAIN);
        mMicGainPref.setOnPreferenceChangeListener(this);
        mMicGainPref.setValue(mDroidAudioManager.getMicGain(currentSource));
        mMicGainPref.setAdjustable(isMicEnabled);
        mMicGainPref.setMin(0);
        mMicGainPref.setMax(100);
        mMicGainPref.setSeekBarIncrement(1);
        mMicGainPref.setVisible(isMicEnabled);
        mMicGainPref.setEnabled(true);

        mMicReverbPref = (TwoStatePreference) findPreference(KEY_REVERB_SWITCH);
        mMicReverbPref.setChecked(mDroidAudioManager.isEnableMicReverb(currentSource));
        mMicReverbPref.setVisible(isMicEnabled);

        mMicReverbLevelPref = (SeekBarPreference) findPreference(KEY_REVERB_LEVEL);
        mMicReverbLevelPref.setValue(mDroidAudioManager.getMicReverbLevel(currentSource));
        mMicReverbLevelPref.setAdjustable(true);
        mMicReverbLevelPref.setMin(0);
        mMicReverbLevelPref.setMax(5);
        mMicReverbLevelPref.setOnPreferenceChangeListener(this);
        mMicReverbLevelPref.setSeekBarIncrement(1);
        mMicReverbLevelPref.setVisible(isMicEnabled);
        Log.i(TAG, "onCreatePreferences: mHalMicDeviceType: " + mHalMicDeviceType + ", Source: " + mDroidAudioManager.getMicSource());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        String key = preference.getKey();
        boolean isChecked;
        int micSource;
        if (TextUtils.equals(key, KEY_GLOBAL_MIC_SWITCH)) {
            mDroidAudioManager.setGlobalMicEnable(mDroidAudioManager.getMicSource(), mMicSwitchPref.isChecked());
            refreshAllPrefStatus();
        } else if (TextUtils.equals(preference.getKey(), KEY_MIC_MUTE)) {
            mDroidAudioManager.setMicMute(mDroidAudioManager.getMicSource(), mMicMutePref.isChecked());
            refreshAllPrefStatus();
        } else if (TextUtils.equals(preference.getKey(), KEY_REVERB_SWITCH)) {
            mDroidAudioManager.setMicReverb(mDroidAudioManager.getMicSource(), mMicReverbPref.isChecked());
            refreshAllPrefStatus();
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_TV_KARAMIC_SOURCE)) {
            String sourceStr = (String)newValue;
            if (sourceStr.contains("1")) {
                mDroidAudioManager.setMicSource(DroidAudioManager.MIC_SOURCE_TYPE_LINE_IN);
            } else if (sourceStr.contains("2")) {
                mDroidAudioManager.setMicSource(DroidAudioManager.MIC_SOURCE_TYPE_USB_IN);
            } else {
                Log.e(TAG,"onPreferenceChange: Invalid Source: " + sourceStr);
            }

            reset();
            refreshAllPrefStatus();
        } else if (TextUtils.equals(preference.getKey(), KEY_MIC_GAIN)) {
            int gain = (int)newValue;
            mDroidAudioManager.setMicGain(mDroidAudioManager.getMicSource(), gain);
            refreshAllPrefStatus();
        } else if (TextUtils.equals(preference.getKey(), KEY_REVERB_LEVEL)) {
            int level = (int)newValue;
            mDroidAudioManager.setMicReverbLevel(mDroidAudioManager.getMicSource(), level);
            refreshAllPrefStatus();
        }

        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    public boolean isUsbAudioInputAvailable() {
        if (mAudioManager == null) {
            Log.w(TAG, "isUsbAudioInputAvailable: warning, mAudioManager is null!");
            return false;
        }

        AudioDeviceInfo[] devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE ||
                device.getType() == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
                device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET ) {
                return true;
            }
        }
        return false;
    }

    private AudioDeviceCallback mAudioDeviceCallback = new AudioDeviceCallback() {
        @Override
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            refreshAllPrefStatus();
        }

        @Override
        public void onAudioDevicesRemoved(AudioDeviceInfo[] devices) {
            refreshAllPrefStatus();
        }
    };

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    private void refreshAllPrefStatus() {
        int source = mDroidAudioManager.getMicSource();
        if (Integer.bitCount(mHalMicDeviceType) < 2) {
            mMicSourcePref.setVisible(false);
        } else {
            if (!isUsbAudioInputAvailable()) {
                if (source == DroidAudioManager.MIC_SOURCE_TYPE_USB_IN) {
                    mDroidAudioManager.setMicSource(DroidAudioManager.MIC_SOURCE_TYPE_LINE_IN);
                }

                mDroidAudioManager.setGlobalMicEnable(DroidAudioManager.MIC_SOURCE_TYPE_USB_IN, false);
                if (source == DroidAudioManager.MIC_SOURCE_TYPE_USB_IN) {
                    mMicMutePref.setVisible(false);
                    mMicGainPref.setVisible(false);
                    mMicReverbPref.setVisible(false);
                    mMicReverbLevelPref.setVisible(false);
                }
                mMicSourcePref.setVisible(false);
                Log.i(TAG, "refreshAllPrefStatus: USB disconnected switch to line in!");
            } else {
                mMicSourcePref.setVisible(true);
                Log.i(TAG, "refreshAllPrefStatus: USB connected");
            }
        }

        source = mDroidAudioManager.getMicSource();
        boolean isEnable = mDroidAudioManager.getGlobalMicStatus(source);

        mMicSwitchPref.setChecked(isEnable);
        if (!isEnable) {
            mMicMutePref.setVisible(false);
            mMicGainPref.setVisible(false);
            mMicReverbPref.setVisible(false);
            mMicReverbLevelPref.setVisible(false);
            Log.i(TAG, "refreshAllPrefStatus: source:"  + source );
        } else {
            boolean isMicMute = mDroidAudioManager.isMicMute(source);
            int gain = mDroidAudioManager.getMicGain(source);
            boolean isReverb = mDroidAudioManager.isEnableMicReverb(source);
            int level = mDroidAudioManager.getMicReverbLevel(source);

            mMicMutePref.setChecked(isMicMute);
            mMicMutePref.setVisible(true);
            mMicGainPref.setValue(gain);
            mMicGainPref.setVisible(!isMicMute);

            mMicReverbPref.setChecked(isReverb);
            mMicReverbPref.setVisible(true);
            mMicReverbLevelPref.setValue(level);
            mMicReverbLevelPref.setVisible(isReverb);

            mMicSourcePref.setValueIndex((source == DroidAudioManager.MIC_SOURCE_TYPE_LINE_IN? 0 : 1));
            Log.i(TAG, "refreshAllPrefStatus: source:"  + source + ", mute:" + isMicMute + ", gain:" + gain + ", reverb:" + isReverb + ", level:" + level);
        }
    }

    private void reset() {
        int newSource = mDroidAudioManager.getMicSource();
        if (mSelectedSourcePre != newSource) {
            boolean isEnable = mDroidAudioManager.getGlobalMicStatus(newSource);
            mMicSwitchPref.setChecked(isEnable);
            mDroidAudioManager.setGlobalMicEnable(newSource, isEnable);
            mDroidAudioManager.setMicMute(newSource, mDroidAudioManager.isMicMute(newSource));
            mDroidAudioManager.setMicGain(newSource, mDroidAudioManager.getMicGain(newSource));
            mDroidAudioManager.setMicReverb(newSource, mDroidAudioManager.isEnableMicReverb(newSource));
            mDroidAudioManager.setMicReverbLevel(newSource, mDroidAudioManager.getMicReverbLevel(newSource));
            mSelectedSourcePre = newSource;
        }
        Log.i(TAG, "reset() reset MIC setting for new source: " + newSource);
    }

}

