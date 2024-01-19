/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC HdmiCecFragment
 */

package com.droidlogic.tv.settings.tvoption;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SeekBarPreference;
import androidx.preference.TwoStatePreference;
import android.text.TextUtils;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;

import java.util.*;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.AudioConfigManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SoundFragment;

import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

/**
 * Fragment to control HDMI Cec settings.
 */
public class HdmiCecFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "HdmiCecFragment";
    private static HdmiCecFragment mHdmiCecFragment = null;

    private static final String KEY_CEC_SWITCH                  = "key_cec_switch";
    private static final String KEY_CEC_VOLUME_CONTROL          = "key_cec_volume_control";
    private static final String KEY_CEC_ONE_KEY_PLAY            = "key_cec_one_key_play";
    private static final String KEY_CEC_AUTO_POWER_OFF          = "key_cec_auto_power_off";
    private static final String KEY_CEC_AUTO_WAKE_UP            = "key_cec_auto_wake_up";
    private static final String KEY_CEC_AUTO_CHANGE_LANGUAGE    = "key_cec_auto_change_language";
    private static final String KEY_CEC_ARC_SWITCH              = "key_cec_arc_switch";
    private static final String KEY_CEC_DEVICE_LIST             = "key_cec_device_list";
    private static final String KEY_EARC_SWITCH                 = "key_earc_switch";
    private static final String KEY_ARC_AND_EARC_SWITCH         = "key_arc_and_earc_switch";
    private static final String KEY_ARC_EARC_MODE_AUTO          = "arc_earc_mode_auto";
    private static final String KEY_ARC_EARC_MODE_ARC           = "arc_earc_mode_arc";

    private static final boolean SUPPORT_EARC =
            SystemProperties.getBoolean("ro.vendor.media.support_earc", false);

    private static final int TIPS_TYPE_CEC = 0;
    private static final int TIPS_TYPE_ARC = 1;

    private static final int MSG_ENABLE_CEC_SWITCH = 0;
    private static final int MSG_ENABLE_ARC_SWITCH = 1;
    private static final int MSG_ENABLE_EARC_SWITCH = 2;
    private static final int MSG_ENABLE_ARC_EARC_SWITCH = 3;
    private static final int TIME_DELAYED = 5000;//ms

    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecVolumeControlPref;
    private TwoStatePreference mCecOneKeyPlayPref;
    private TwoStatePreference mCecDeviceAutoPowerOffPref;
    private TwoStatePreference mCecAutoWakeupPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private TwoStatePreference mArcSwitchPref;
    private TwoStatePreference mEarcSwitchPref;
    private TwoStatePreference mArcNEarcSwitchPref;
    private RadioPreference mArcEarcModeAutoPref;
    private RadioPreference mArcEarcModeARCPref;

    private SoundParameterSettingManager mSoundParameterSettingManager;
    private AudioConfigManager mAudioConfigManager = null;
    private HdmiCecManager mHdmiCecManager;
    private static long mLastObserveredCECTime = 0;
    private static long mLastObserveredArcEarcTime = 0;

    public static HdmiCecFragment newInstance() {
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = new HdmiCecFragment();
        }
        return mHdmiCecFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHdmiCecManager = new HdmiCecManager(getContext());
        if (mAudioConfigManager == null) {
            mAudioConfigManager = AudioConfigManager.getInstance(getActivity());
        }
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.hdmicec, null);
        boolean tvFlag = mHdmiCecManager.isTv();
        mCecSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_SWITCH);
        mCecVolumeControlPref = (TwoStatePreference) findPreference(KEY_CEC_VOLUME_CONTROL);
        mCecOneKeyPlayPref = (TwoStatePreference) findPreference(KEY_CEC_ONE_KEY_PLAY);
        mCecDeviceAutoPowerOffPref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_POWER_OFF);
        mCecAutoWakeupPref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_WAKE_UP);
        mCecAutoChangeLanguagePref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_CHANGE_LANGUAGE);
        mArcSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_ARC_SWITCH);
        mEarcSwitchPref = (TwoStatePreference) findPreference(KEY_EARC_SWITCH);
        mArcNEarcSwitchPref = (TwoStatePreference) findPreference(KEY_ARC_AND_EARC_SWITCH);
        mArcEarcModeAutoPref = (RadioPreference) findPreference(KEY_ARC_EARC_MODE_AUTO);
        mArcEarcModeARCPref = (RadioPreference) findPreference(KEY_ARC_EARC_MODE_ARC);

        final Preference hdmiDeviceSelectPref = findPreference(KEY_CEC_DEVICE_LIST);
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = newInstance();
        }
        hdmiDeviceSelectPref.setOnPreferenceChangeListener(mHdmiCecFragment);

        final ListPreference digitalSoundPref = (ListPreference) findPreference(SoundFragment.KEY_DIGITALSOUND_FORMAT);
        digitalSoundPref.setValue(mSoundParameterSettingManager.getDigitalAudioFormat());
        if (tvFlag) {
            /* not support passthrough when ms12 so are not included.*/
            if (!mSoundParameterSettingManager.isAudioSupportMs12System()) {
                String[] entry = getArrayString(R.array.digital_sounds_tv_entries);
                String[] entryValue = getArrayString(R.array.digital_sounds_tv_entry_values);
                List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
                List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
                entryList.remove("Passthough");
                entryValueList.remove(SoundParameterSettingManager.DIGITAL_SOUND_PASSTHROUGH);
                digitalSoundPref.setEntries(entryList.toArray(new String[]{}));
                digitalSoundPref.setEntryValues(entryValueList.toArray(new String[]{}));
            } else {
                digitalSoundPref.setEntries(R.array.digital_sounds_tv_entries);
                digitalSoundPref.setEntryValues(R.array.digital_sounds_tv_entry_values);
            }
        } else {
            digitalSoundPref.setEntries(R.array.digital_sounds_box_entries);
            digitalSoundPref.setEntryValues(R.array.digital_sounds_box_entry_values);
        }
        digitalSoundPref.setOnPreferenceChangeListener(this);

        boolean hideOptions = Settings.Global.getInt(getContext().getContentResolver(),
                HdmiCecManager.SETTINGS_DROIDLOGIC_CEC_SUPPORT, 0) == 0;

        mCecOneKeyPlayPref.setVisible(!tvFlag && !hideOptions);
        mCecAutoWakeupPref.setVisible(tvFlag);
        mCecSwitchPref.setVisible(true);
        mArcSwitchPref.setVisible(false);
        mEarcSwitchPref.setVisible(false);
        mCecDeviceAutoPowerOffPref.setVisible(true);
        mCecAutoChangeLanguagePref.setVisible(!tvFlag && !hideOptions);
        mCecVolumeControlPref.setVisible(!tvFlag && !hideOptions);
        hdmiDeviceSelectPref.setVisible(tvFlag);
        digitalSoundPref.setVisible(false);
        boolean isChecked = mHdmiCecManager.isArcEnabled();
        mArcNEarcSwitchPref.setChecked(isChecked);
        //mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
        mArcNEarcSwitchPref.setVisible(tvFlag);
        mArcEarcModeAutoPref.setVisible(tvFlag && SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
        mArcEarcModeARCPref.setVisible(tvFlag && SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_CEC_SWITCH:
                mCecSwitchPref.setEnabled(false);
                if (mArcNEarcSwitchPref.isChecked() && !mCecSwitchPref.isChecked()) {
                    showTipsDialog(TIPS_TYPE_CEC, getContext().getString(R.string.tips_turn_off_cec));
                } else {
                    sendMsgEnableCECSwitch();
                    enablePreferences(false);
                }
                break;
            case KEY_CEC_ONE_KEY_PLAY:
                mHdmiCecManager.enableOneTouchPlay(mCecOneKeyPlayPref.isChecked());
                break;
            case KEY_CEC_AUTO_POWER_OFF:
                mHdmiCecManager.enableAutoPowerOff(mCecDeviceAutoPowerOffPref.isChecked());
                break;
            case KEY_CEC_AUTO_WAKE_UP:
                mHdmiCecManager.enableAutoWakeUp(mCecAutoWakeupPref.isChecked());
                break;
            case KEY_CEC_AUTO_CHANGE_LANGUAGE:
                mHdmiCecManager.enableAutoChangeLanguage(mCecAutoChangeLanguagePref.isChecked());
                break;
            case KEY_CEC_ARC_SWITCH:
                mHdmiCecManager.enableArc(mArcSwitchPref.isChecked());
                mHandler.sendEmptyMessageDelayed(MSG_ENABLE_ARC_SWITCH, TIME_DELAYED);
                mArcSwitchPref.setEnabled(false);
                break;
            case KEY_EARC_SWITCH:
                mHdmiCecManager.enableEarc(mEarcSwitchPref.isChecked());
                mHandler.sendEmptyMessageDelayed(MSG_ENABLE_EARC_SWITCH, TIME_DELAYED);
                mEarcSwitchPref.setEnabled(false);
                break;
            case KEY_ARC_AND_EARC_SWITCH:
                logDebug(TAG, false, "arc/earc_switch: " + mArcNEarcSwitchPref.isChecked());
                //mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                if (!mCecSwitchPref.isChecked() && mArcNEarcSwitchPref.isChecked()) {
                    showTipsDialog(TIPS_TYPE_ARC, getContext().getString(R.string.tips_turn_on_arc_earc));
                } else {
                    sendMsgEnableArcEarcSwitch();
                    mArcNEarcSwitchPref.setEnabled(false);
                    mArcEarcModeAutoPref.setEnabled(false);
                    mArcEarcModeARCPref.setEnabled(false);
                }
                break;
            case KEY_ARC_EARC_MODE_AUTO:
                logDebug(TAG, false, "arc/earc_switch mode [AUTO]");
                mArcEarcModeAutoPref.setChecked(true);
                mArcEarcModeARCPref.setChecked(false);
                mHdmiCecManager.enableEarc(true);
                break;
            case KEY_ARC_EARC_MODE_ARC:
                logDebug(TAG, false, "arc/earc_switch mode [ARC]");
                mArcEarcModeARCPref.setChecked(true);
                mArcEarcModeAutoPref.setChecked(false);
                mHdmiCecManager.enableEarc(false);
                break;
            case KEY_CEC_VOLUME_CONTROL:
                updateVolumeControl(mCecVolumeControlPref.isChecked());
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void sendMsgEnableCECSwitch() {
        long curtime = System.currentTimeMillis();
        long timeDiff = curtime - mLastObserveredCECTime;
        Message cecEnabled = mHandler.obtainMessage(MSG_ENABLE_CEC_SWITCH, 0, 0);
        mHandler.removeMessages(MSG_ENABLE_CEC_SWITCH);
        mHandler.sendMessageDelayed(cecEnabled, ((timeDiff > TIME_DELAYED) ? 0 : TIME_DELAYED));
    }

    private void sendMsgEnableArcEarcSwitch() {
        long curtime = System.currentTimeMillis();
        long timeDiff = curtime - mLastObserveredArcEarcTime;
        Message arcEarcEnabled = mHandler.obtainMessage(MSG_ENABLE_ARC_EARC_SWITCH, 0, 0);
        mHandler.removeMessages(MSG_ENABLE_ARC_EARC_SWITCH);
        mHandler.sendMessageDelayed(arcEarcEnabled, ((timeDiff > TIME_DELAYED) ? 0 : TIME_DELAYED));
    }

    private void enablePreferences(boolean enabled) {
        mCecOneKeyPlayPref.setEnabled(enabled);
        mCecDeviceAutoPowerOffPref.setEnabled(enabled);
        mCecAutoWakeupPref.setEnabled(enabled);
        mCecAutoChangeLanguagePref.setEnabled(enabled);
        mArcSwitchPref.setEnabled(enabled);
        mCecVolumeControlPref.setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), SoundFragment.KEY_DIGITALSOUND_FORMAT)) {
            mSoundParameterSettingManager.setDigitalAudioFormat((String) newValue);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void updateVolumeControl(boolean enabled) {
        mHdmiCecManager.enableVolumeControl(enabled);
    }

    private void refresh() {
        boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
        mCecSwitchPref.setChecked(hdmiControlEnabled);
        mCecOneKeyPlayPref.setChecked(mHdmiCecManager.isOneTouchPlayEnabled());
        mCecOneKeyPlayPref.setEnabled(hdmiControlEnabled);
        mCecDeviceAutoPowerOffPref.setChecked(mHdmiCecManager.isAutoPowerOffEnabled());
        mCecDeviceAutoPowerOffPref.setEnabled(hdmiControlEnabled);
        mCecAutoWakeupPref.setChecked(mHdmiCecManager.isAutoWakeUpEnabled());
        mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
        mCecAutoChangeLanguagePref.setChecked(mHdmiCecManager.isAutoChangeLanguageEnabled());
        mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);
        mCecVolumeControlPref.setChecked(mHdmiCecManager.isVolumeControlEnabled());
        mCecVolumeControlPref.setEnabled(hdmiControlEnabled);
        boolean arcEnabled = mHdmiCecManager.isArcEnabled();
        boolean earcEnabled = mHdmiCecManager.isEarcEnabled();

        if (arcEnabled) {
            logDebug(TAG, false, "arcEnabled:" + arcEnabled + ",earcEnabled:" + earcEnabled);
            mArcEarcModeAutoPref.setChecked(earcEnabled);
            mArcEarcModeARCPref.setChecked(!earcEnabled);
        } else {
            mArcEarcModeAutoPref.setChecked(true);
            mArcEarcModeARCPref.setChecked(false);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_CEC_SWITCH:
                    mCecSwitchPref.setEnabled(true);
                    mHdmiCecManager.enableHdmiControl(mCecSwitchPref.isChecked());
                    boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
                    logDebug(TAG, false, "hdmiControlEnabled :" + hdmiControlEnabled);
                    enablePreferences(hdmiControlEnabled);
                    mLastObserveredCECTime = System.currentTimeMillis();
                    break;
                case MSG_ENABLE_ARC_EARC_SWITCH:
                    mArcNEarcSwitchPref.setEnabled(true);
                    mArcEarcModeAutoPref.setEnabled(true);
                    mArcEarcModeARCPref.setEnabled(true);
                    mArcEarcModeAutoPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                    mArcEarcModeARCPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                    mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                    if (mArcNEarcSwitchPref.isChecked()) {
                        mHdmiCecManager.enableEarc(mArcEarcModeAutoPref.isChecked());
                    }
                    mLastObserveredArcEarcTime = System.currentTimeMillis();
                    break;
                default:
                    break;
            }
        }
    };

    private void showTipsDialog(int tipsType, String tips) {
        final AlertDialog.Builder tipsDialog = new AlertDialog.Builder(getActivity());
        tipsDialog.setTitle("TIPS");
        tipsDialog.setMessage(tips);
        tipsDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (tipsType == TIPS_TYPE_CEC) {
                            logDebug(TAG, false, "onClick yes, Type CEC, turn off CEC and arc/eARC");
                            //TODO:turn off CEC
                            mCecSwitchPref.setChecked(false);
                            mHdmiCecManager.enableArc(false);
                            sendMsgEnableCECSwitch();
                            enablePreferences(false);
                            //TODO:turn off ARC\eARC
                            mArcNEarcSwitchPref.setChecked(false);
                            mArcEarcModeAutoPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                            mArcEarcModeARCPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                        } else if (tipsType == TIPS_TYPE_ARC) {
                            //TODO:turn on cec ui ref
                            logDebug(TAG, false, "onClick yes, Type ARC/eARC, turn on arc and cec");
                            mCecSwitchPref.setChecked(true);
                            mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                            sendMsgEnableCECSwitch();
                            enablePreferences(false);
                            //turn on ARC
                            mArcEarcModeAutoPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                            mArcEarcModeARCPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                            if (mArcEarcModeAutoPref.isChecked()) {
                                mHdmiCecManager.enableEarc(true);
                            } else {
                                mHdmiCecManager.enableEarc(false);
                            }
                        }
                    }
                });
        tipsDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //problem
                        if (tipsType == TIPS_TYPE_CEC) {
                            mCecSwitchPref.setChecked(true);
                            mCecSwitchPref.setEnabled(true);
                        } else if (tipsType == TIPS_TYPE_ARC) {
                            mArcNEarcSwitchPref.setChecked(false);
                            mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                            mArcEarcModeAutoPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                            mArcEarcModeARCPref.setVisible(SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
                            logDebug(TAG, false, "[current value] enableARC: " + mHdmiCecManager.isArcEnabled()
                                    + ", enableEARC: " + mHdmiCecManager.isEarcEnabled());
                        }
                    }
                });
        tipsDialog.setCancelable(false);
        tipsDialog.show();
    }
}
