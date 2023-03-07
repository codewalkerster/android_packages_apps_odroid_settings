/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.hardkernel.odroid.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.PlayBackManager;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.SettingsConstant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

/**
 * Fragment to control HDMI stuffs settings.
 */
public class HdmiFragment extends LeanbackAddBackPreferenceFragment {

    private static final String TAG = "HdmiFragment";

    private static final String KEY_HDMI_CEC = "hdmi_cec";
    private static final String KEY_CEC_SWITCH = "cec_switch";
    private static final String KEY_CEC_ONEKEY_PLAY = "cec_onekey_play";
    private static final String KEY_CEC_ONEKEY_POWEROFF = "cec_onekey_poweroff";
    private static final String KEY_CEC_AUTO_CHANGE_LANGUAGE = "cec_auto_change_language";
    private static final String KEY_PLAYBACK_HDMI_SELFADAPTION = "playback_hdmi_selfadaption";

    private static final String PERSIST_HDMI_CEC_SET_MENU_LANGUAGE = "persist.vendor.sys.cec.set_menu_language";

    private static final String CEC_STATE = "/sys/class/cec/pin_status";

    private PlayBackManager mPlayBackManager;

    private PreferenceCategory mHdmiCecPref;
    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecOnekeyPlayPref;
    private TwoStatePreference mCecOnekeyPoweroffPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private Preference hdmiSelfAdaptionPref;

    public static HdmiFragment newInstance() {
        return new HdmiFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.hdmi, null);
        boolean is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1;
        Context context = getContext();
        mPlayBackManager = new PlayBackManager(context);

        mHdmiCecPref = (PreferenceCategory) findPreference(KEY_HDMI_CEC);
        mCecSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_SWITCH);
        mCecOnekeyPlayPref = (TwoStatePreference) findPreference(KEY_CEC_ONEKEY_PLAY);
        mCecOnekeyPoweroffPref = (TwoStatePreference) findPreference(KEY_CEC_ONEKEY_POWEROFF);
        mCecAutoChangeLanguagePref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_CHANGE_LANGUAGE);
        hdmiSelfAdaptionPref = findPreference(KEY_PLAYBACK_HDMI_SELFADAPTION);

        mHdmiCecPref.setVisible(is_from_live_tv ? false : (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_HDMI_CEC)
                    && SettingsConstant.needDroidlogicHdmicecFeature(context)));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) {
            return super.onPreferenceTreeClick(preference);
        }
        switch (key) {
            case KEY_CEC_SWITCH:
                writeCecOption(Settings.Global.HDMI_CONTROL_ENABLED, mCecSwitchPref.isChecked());
                boolean hdmiControlEnabled = readCecOption(Settings.Global.HDMI_CONTROL_ENABLED);
                mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
                mCecOnekeyPoweroffPref.setEnabled(hdmiControlEnabled);
                mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);
                return true;
            case KEY_CEC_ONEKEY_PLAY:
                writeCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED, mCecOnekeyPlayPref.isChecked());
                return true;
            case KEY_CEC_ONEKEY_POWEROFF:
                writeCecOption(Settings.Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED, mCecOnekeyPoweroffPref.isChecked());
                return true;
            case KEY_CEC_AUTO_CHANGE_LANGUAGE:
                //writeCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED,
                //		mCecAutoChangeLanguagePref.isChecked());
                EnvProperty.set(PERSIST_HDMI_CEC_SET_MENU_LANGUAGE, mCecAutoChangeLanguagePref.isChecked() ? "true" : "false");
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private boolean isCecSupport() {
        String cecState = "";
        try {
            FileReader fileReader = new FileReader(CEC_STATE);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            cecState = bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (cecState.equals("ok")) {
            return true;
        } else {
            return false;
        }
    }

    private void refresh() {
        boolean hdmiControlEnabled;
        if (isCecSupport()) {
            hdmiControlEnabled = readCecOption(Settings.Global.HDMI_CONTROL_ENABLED);
        } else {
            hdmiControlEnabled = false;
            mCecSwitchPref.setEnabled(hdmiControlEnabled);
        }
        mCecSwitchPref.setChecked(hdmiControlEnabled);
        mCecOnekeyPlayPref.setChecked(readCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED));
        mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
        mCecOnekeyPoweroffPref.setChecked(readCecOption(Settings.Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED));
        mCecOnekeyPoweroffPref.setEnabled(hdmiControlEnabled);
        //mCecAutoChangeLanguagePref.setChecked(readCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED));
        mCecAutoChangeLanguagePref.setChecked(EnvProperty.getBoolean(PERSIST_HDMI_CEC_SET_MENU_LANGUAGE, false));
        mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);

        hdmiSelfAdaptionPref.setSummary(getHdmiSelfAdaptionStatus());
    }

    private String getHdmiSelfAdaptionStatus() {
        int mode = mPlayBackManager.getHdmiSelfAdaptionMode();
        switch (mode) {
            case PlayBackManager.MODE_OFF:
                return getString(R.string.off);
            case PlayBackManager.MODE_PART:
                return getString(R.string.playback_hdmi_selfadaption_part);
            case PlayBackManager.MODE_TOTAL:
                return getString(R.string.playback_hdmi_selfadaption_total);
            default:
                return getString(R.string.off);
        }
    }

    private boolean readCecOption(String key) {
        return Settings.Global.getInt(getContext().getContentResolver(), key, 1) == 1;
    }

    private void writeCecOption(String key, boolean value) {
        Settings.Global.putInt(getContext().getContentResolver(), key, value ? 1 : 0);
    }
}
