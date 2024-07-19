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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ComponentName;
import android.hardware.hdmi.HdmiDeviceInfo;
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
import android.util.Log;
import android.view.WindowManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;

import java.util.*;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SoundFragment;

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
    private static final String KEY_SOUNDBAR_MODE               = "hdmi_soundbar_mode_key";
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
    private static final int MSG_ENABLE_SONUDBAR_MODE_SWITCH = 4;

    private static final int TIME_DELAYED = 2000;//ms

    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecVolumeControlPref;
    private TwoStatePreference mCecOneKeyPlayPref;
    private TwoStatePreference mCecDeviceAutoPowerOffPref;
    private TwoStatePreference mCecAutoWakeupPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private TwoStatePreference mArcSwitchPref;
    private TwoStatePreference mSoundbarModePref;
    private TwoStatePreference mEarcSwitchPref;
    private TwoStatePreference mArcNEarcSwitchPref;
    private RadioPreference mArcEarcModeAutoPref;
    private RadioPreference mArcEarcModeARCPref;
    private Preference mHdmiDeviceSelectPref;
    private ListPreference mDigitalSoundPref;

    private DroidAudioManager mDroidAudioManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private ProgressDialog mProgress;
    private HdmiCecManager mHdmiCecManager;
    private static long mLastObserveredCECTime = 0;
    private static long mLastObserveredArcEarcTime = 0;
    private static long mLastObserveredSoundbarModeTime = 0;

    public static HdmiCecFragment newInstance() {
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = new HdmiCecFragment();
        }
        return mHdmiCecFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHdmiCecManager = new HdmiCecManager(getContext());
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }
        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage(getActivity().getResources().getString(R.string.cec_wait));
        mProgress.setIndeterminate(false);
        mProgress.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        mProgress.setCancelable(false);
        if (mDroidAudioManager == null) {
            mDroidAudioManager = DroidAudioManager.getInstance(getActivity());
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        if (mHandler.hasMessages(MSG_ENABLE_CEC_SWITCH)) {
            mHandler.removeMessages(MSG_ENABLE_CEC_SWITCH);
            mHandler.handleMessage(mHandler.obtainMessage(MSG_ENABLE_CEC_SWITCH, 0, 0));
        } else if (mHandler.hasMessages(MSG_ENABLE_ARC_EARC_SWITCH)) {
            mHandler.removeMessages(MSG_ENABLE_ARC_EARC_SWITCH);
            mHandler.handleMessage(mHandler.obtainMessage(MSG_ENABLE_ARC_EARC_SWITCH, 0, 0));
        }
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.hdmicec, null);
        boolean tvFlag = mHdmiCecManager.isTv();
        boolean soundbarFlag = mHdmiCecManager.getClient(HdmiDeviceInfo.DEVICE_AUDIO_SYSTEM) != null
                                && mHdmiCecManager.isSoundbarModeEnabled();
        mCecSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_SWITCH);
        mSoundbarModePref = (TwoStatePreference) findPreference(KEY_SOUNDBAR_MODE);
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

        mHdmiDeviceSelectPref = findPreference(KEY_CEC_DEVICE_LIST);
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = newInstance();
        }
        mHdmiDeviceSelectPref.setOnPreferenceChangeListener(mHdmiCecFragment);

        mDigitalSoundPref = (ListPreference) findPreference(SoundFragment.KEY_DIGITALSOUND_FORMAT);
        mDigitalSoundPref.setValue(mSoundParameterSettingManager.getDigitalAudioFormat());
        if (tvFlag) {
            /* not support passthrough when ms12 so are not included.*/
            if (!mDroidAudioManager.isAudioSupportMs12System()) {
                String[] entry = getArrayString(R.array.digital_sounds_tv_entries);
                String[] entryValue = getArrayString(R.array.digital_sounds_tv_entry_values);
                List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
                List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
                entryList.remove("Passthough");
                entryValueList.remove(SoundParameterSettingManager.DIGITAL_SOUND_PASSTHROUGH);
                mDigitalSoundPref.setEntries(entryList.toArray(new String[]{}));
                mDigitalSoundPref.setEntryValues(entryValueList.toArray(new String[]{}));
            } else {
                mDigitalSoundPref.setEntries(R.array.digital_sounds_tv_entries);
                mDigitalSoundPref.setEntryValues(R.array.digital_sounds_tv_entry_values);
            }
        } else {
            mDigitalSoundPref.setEntries(R.array.digital_sounds_box_entries);
            mDigitalSoundPref.setEntryValues(R.array.digital_sounds_box_entry_values);
        }
        mDigitalSoundPref.setOnPreferenceChangeListener(this);

        if (tvFlag && !SUPPORT_EARC) {
            getPreferenceScreen().setTitle(R.string.cec_control);
            mArcNEarcSwitchPref.setTitle(R.string.title_arc_and_earc_mode_arc);
            mArcNEarcSwitchPref.setSummary(R.string.cec_arc_switch_description);
        }
        if (!tvFlag) {
            getPreferenceScreen().setTitle(R.string.cec_control);
        }

        setVisibleForPref(tvFlag, soundbarFlag);
    }

    private void setVisibleForPref(boolean tvFlag, boolean soundbarFlag) {
        mCecOneKeyPlayPref.setVisible(!tvFlag && !soundbarFlag);
        mCecAutoWakeupPref.setVisible(tvFlag);
        mCecSwitchPref.setVisible(!soundbarFlag);
        mArcSwitchPref.setVisible(false);
        mEarcSwitchPref.setVisible(false);
        mCecDeviceAutoPowerOffPref.setVisible(!soundbarFlag);
        mCecAutoChangeLanguagePref.setVisible(!tvFlag && !soundbarFlag);
        mCecVolumeControlPref.setVisible(!tvFlag && !soundbarFlag);
        mSoundbarModePref.setVisible(SettingsConstant.isSoundbarFeature());
        mHdmiDeviceSelectPref.setVisible(tvFlag || soundbarFlag);
        mDigitalSoundPref.setVisible(false);
        boolean isChecked = mHdmiCecManager.isArcEnabled();
        mArcNEarcSwitchPref.setChecked(isChecked);
        //mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
        mArcNEarcSwitchPref.setVisible(tvFlag);
        mArcEarcModeAutoPref.setVisible(tvFlag && SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
        mArcEarcModeARCPref.setVisible(tvFlag && SUPPORT_EARC && mArcNEarcSwitchPref.isChecked());
        refresh();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_CEC_SWITCH:
                mCecSwitchPref.setEnabled(false);
                if (mHdmiCecManager.isTv() && mArcNEarcSwitchPref.isChecked() && !mCecSwitchPref.isChecked()) {
                    showTipsDialog(TIPS_TYPE_CEC, getContext().getString(
                        SUPPORT_EARC ? R.string.tips_turn_off_cec : R.string.tips_turn_off_cec_arc_device));
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
                Log.d(TAG, "arc/earc_switch: " + mArcNEarcSwitchPref.isChecked());
                //mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                if (!mCecSwitchPref.isChecked() && mArcNEarcSwitchPref.isChecked()) {
                    showTipsDialog(TIPS_TYPE_ARC, getContext().getString(
                        SUPPORT_EARC ? R.string.tips_turn_on_arc_earc : R.string.tips_turn_on_arc));
                } else {
                    sendMsgEnableArcEarcSwitch();
                    mArcNEarcSwitchPref.setEnabled(false);
                    mArcEarcModeAutoPref.setEnabled(false);
                    mArcEarcModeARCPref.setEnabled(false);
                }
                break;
            case KEY_ARC_EARC_MODE_AUTO:
                Log.d(TAG, "arc/earc_switch mode [AUTO]");
                mArcEarcModeAutoPref.setChecked(true);
                mArcEarcModeARCPref.setChecked(false);
                mHdmiCecManager.enableEarc(true);
                break;
            case KEY_ARC_EARC_MODE_ARC:
                Log.d(TAG, "arc/earc_switch mode [ARC]");
                mArcEarcModeARCPref.setChecked(true);
                mArcEarcModeAutoPref.setChecked(false);
                mHdmiCecManager.enableEarc(false);
                break;
            case KEY_CEC_VOLUME_CONTROL:
                updateVolumeControl(mCecVolumeControlPref.isChecked());
                break;
            case KEY_SOUNDBAR_MODE:
                Log.d(TAG, "soundbar mode switch clicked: " + mSoundbarModePref.isChecked());
                if (mSoundbarModePref.isChecked() && !mHdmiCecManager.isHdmiControlEnabled()) {
                    // open soundbar mode with cec disabled, need to turn on cec at the same time
                    showTipsDialogForSoundbarMode(getContext().getString(
                            R.string.tips_turn_on_soundbar_mode));
                } else {
                    updateSoundbarMode(mSoundbarModePref.isChecked());
                }
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void sendMsgEnableCECSwitch() {
        sendMsgEnableCECSwitch(false);
    }

    private void sendMsgEnableCECSwitch(boolean enableEarc) {
        long curtime = System.currentTimeMillis();
        long timeDiff = curtime - mLastObserveredCECTime;
        Message cecEnabled = mHandler.obtainMessage(MSG_ENABLE_CEC_SWITCH, enableEarc ? 1 : 0, 0);
        mHandler.removeMessages(MSG_ENABLE_CEC_SWITCH);
        if (mProgress != null && !mProgress.isShowing() && (timeDiff <= TIME_DELAYED)) {
            Log.d(TAG, "check enable/disable cec switch");
            mProgress.show();
        }
        mHandler.sendMessageDelayed(cecEnabled, ((timeDiff > TIME_DELAYED) ? 0 : TIME_DELAYED));
    }


    private void sendMsgEnableArcEarcSwitch() {
        long curtime = System.currentTimeMillis();
        long timeDiff = curtime - mLastObserveredArcEarcTime;
        Message arcEarcEnabled = mHandler.obtainMessage(MSG_ENABLE_ARC_EARC_SWITCH, 0, 0);
        mHandler.removeMessages(MSG_ENABLE_ARC_EARC_SWITCH);
        if (mProgress != null && !mProgress.isShowing() && (timeDiff <= TIME_DELAYED)) {
            Log.d(TAG, "check enable show arc/earc switch");
            mProgress.show();
        }
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
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
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

    private void updateSoundbarMode(boolean soundbarModeEnabled) {
        long curtime = System.currentTimeMillis();
        long timeDiff = curtime - mLastObserveredSoundbarModeTime;
        Log.d(TAG, "updateSoundbarMode soundbarModeEnabled: " + soundbarModeEnabled);
        if (mProgress != null && !mProgress.isShowing() && (timeDiff <= TIME_DELAYED)) {
             Log.d(TAG, "check enable/disable soundbar mode");
             mProgress.show();
        }
        mHandler.removeMessages(MSG_ENABLE_SONUDBAR_MODE_SWITCH);
        mHandler.sendEmptyMessageDelayed(MSG_ENABLE_SONUDBAR_MODE_SWITCH,
                ((timeDiff > TIME_DELAYED) ? 0 : TIME_DELAYED));
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
            Log.d(TAG, "arcEnabled:" + arcEnabled + ",earcEnabled:" + earcEnabled);
            mArcEarcModeAutoPref.setChecked(earcEnabled);
            mArcEarcModeARCPref.setChecked(!earcEnabled);
        } else {
            mArcEarcModeAutoPref.setChecked(true);
            mArcEarcModeARCPref.setChecked(false);
        }
        mSoundbarModePref.setChecked(mDroidAudioManager.isSoundBarModeEnabled());
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_CEC_SWITCH:
                    mCecSwitchPref.setEnabled(true);
                    mHdmiCecManager.enableHdmiControl(mCecSwitchPref.isChecked());
                    if (msg.arg1 == 1) {
                        Log.d(TAG, "Enable earc after cec");
                        mHdmiCecManager.enableArc(mArcNEarcSwitchPref.isChecked());
                    }
                    boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
                    Log.d(TAG, "hdmiControlEnabled :" + hdmiControlEnabled);
                    enablePreferences(hdmiControlEnabled);
                    mLastObserveredCECTime = System.currentTimeMillis();
                    if (mCecSwitchPref.isChecked()) {
                        mLastObserveredSoundbarModeTime = System.currentTimeMillis();
                    }
                    setVisibleForPref(mHdmiCecManager.isTv(), mSoundbarModePref.isChecked());
                    if (mProgress != null && mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
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
                    if (mProgress != null && mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                    break;
                case MSG_ENABLE_SONUDBAR_MODE_SWITCH:
                    mDroidAudioManager.setSoundBarModeEnabled(mSoundbarModePref.isChecked());
                    mLastObserveredSoundbarModeTime = System.currentTimeMillis();
                    if (mProgress != null && mProgress.isShowing() && mHdmiCecManager.isHdmiControlEnabled()) {
                        mProgress.dismiss();
                    }
                    if (mDroidAudioManager.isSoundBarModeEnabled() && !mHdmiCecManager.isHdmiControlEnabled()) {
                        mCecSwitchPref.setChecked(true);
                        sendMsgEnableCECSwitch();
                    } else {
                        mLastObserveredCECTime = System.currentTimeMillis();
                        setVisibleForPref(mHdmiCecManager.isTv(), mSoundbarModePref.isChecked());
                    }
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
                            Log.d(TAG, "onClick yes, Type CEC, turn off CEC and arc/eARC");
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
                            Log.d(TAG, "onClick yes, Type ARC/eARC, turn on arc and cec");
                            mCecSwitchPref.setChecked(true);
                            sendMsgEnableCECSwitch(true);
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
                            Log.d(TAG, "[current value] enableARC: " + mHdmiCecManager.isArcEnabled()
                                    + ", enableEARC: " + mHdmiCecManager.isEarcEnabled());
                        }
                    }
                });
        tipsDialog.setCancelable(false);
        tipsDialog.show();
    }

    private void showTipsDialogForSoundbarMode(String tips) {
        final AlertDialog.Builder tipsDialog = new AlertDialog.Builder(getActivity());
        tipsDialog.setTitle("TIPS");
        tipsDialog.setMessage(tips);
        tipsDialog.setPositiveButton("YES",
             new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     Log.d(TAG, "onClick yes, turn on soundbar mode, and then turn on CEC");
                     updateSoundbarMode(mSoundbarModePref.isChecked());
                 }
             });
        tipsDialog.setNegativeButton("CANCEL",
             new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    mSoundbarModePref.setChecked(false);
                }
            });
        tipsDialog.setCancelable(false);
        tipsDialog.show();
    }
}
