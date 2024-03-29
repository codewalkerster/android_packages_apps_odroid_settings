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

package com.droidlogic.tv.settings.tvoption;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.os.SystemProperties;
import android.media.AudioManager;
import android.app.ActivityManager;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.content.ContentResolver;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.app.DataProviderManager;
import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.app.SystemControlManager;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

public class SoundParameterSettingManager {

    public static final String TAG = "SoundParameterSettingManager";
    public static final String DIGITAL_SOUND_PCM                = "pcm";
    public static final String DIGITAL_SOUND_AUTO               = "auto";
    public static final String DIGITAL_SOUND_MANUAL             = "manual";
    public static final String DIGITAL_SOUND_PASSTHROUGH        = "passthrough";
    public static final String TV_KEY_AD_SWITCH                 = "ad_switch";

    public static final int DEBUG_DOLBY_DRC_UI                  = 0;
    public static final int DEBUG_DTS_DRC_UI                    = 1;
    public static final int DEBUG_FORCE_DDP_UI                  = 2;
    public static final int DEBUG_AUDIO_LATENCY_UI              = 3;

    public static final int DEBUG_AUDIO_UI_ON                   = 1;
    public static final int DEBUG_AUDIO_UI_OFF                  = 0;

    public static final String DB_ID_DOLBY_DRC_DEBUG            = "db_id_dobly_drc_debug";
    public static final String DB_ID_DTS_DRC_DEBUG              = "db_id_dts_drc_debug";
    public static final String DB_ID_FORCE_DDP_DEBUG            = "db_id_force_ddp_debug";
    public static final String DB_ID_AUDIO_LATENCY_DEBUG        = "db_id_audio_latency_debug";

    private Resources mResources;
    private Context mContext;
    private DroidAudioManager mDroidAudioManager;

    public SoundParameterSettingManager (Context context) {
        mContext = context;
        mResources = mContext.getResources();
        mDroidAudioManager = DroidAudioManager.getInstance(context);
    }

    public void setDebugAudioOn (int id, boolean dbSwitch) {
        logDebug(TAG, false, "setDebugAudioOn id:" + id + ", dbSwitch:" + dbSwitch);

        switch (id) {
            case DEBUG_DOLBY_DRC_UI:
                Settings.Global.putInt(mContext.getContentResolver(), DB_ID_DOLBY_DRC_DEBUG, dbSwitch ? 1 : 0);
                break;
            case DEBUG_DTS_DRC_UI:
                Settings.Global.putInt(mContext.getContentResolver(), DB_ID_DTS_DRC_DEBUG, dbSwitch ? 1 : 0);
                break;
            case DEBUG_FORCE_DDP_UI:
                Settings.Global.putInt(mContext.getContentResolver(), DB_ID_FORCE_DDP_DEBUG, dbSwitch ? 1 : 0);
                break;
            case DEBUG_AUDIO_LATENCY_UI:
                Settings.Global.putInt(mContext.getContentResolver(), DB_ID_AUDIO_LATENCY_DEBUG, dbSwitch ? 1 : 0);
                break;
            default:
                break;
        }
    }

    public boolean isDebugAudioOn(int id) {
        int value = -1;
        switch (id) {
            case DEBUG_DOLBY_DRC_UI:
                value = Settings.Global.getInt(mContext.getContentResolver(), DB_ID_DOLBY_DRC_DEBUG, DEBUG_AUDIO_UI_OFF);
                break;
            case DEBUG_DTS_DRC_UI:
                value = Settings.Global.getInt(mContext.getContentResolver(), DB_ID_DTS_DRC_DEBUG, DEBUG_AUDIO_UI_OFF);
                break;
            case DEBUG_FORCE_DDP_UI:
                value = Settings.Global.getInt(mContext.getContentResolver(), DB_ID_FORCE_DDP_DEBUG, DEBUG_AUDIO_UI_OFF);
                break;
            case DEBUG_AUDIO_LATENCY_UI:
                value = Settings.Global.getInt(mContext.getContentResolver(), DB_ID_AUDIO_LATENCY_DEBUG, DEBUG_AUDIO_UI_OFF);
                break;
            default:
                break;
        }
        logDebug(TAG, false, "isDebugAudioOn id:" + id + ", value:" + value);

        return value == DEBUG_AUDIO_UI_ON;
    }

    public void setDigitalAudioFormat (String mode) {
        logDebug(TAG, false, "setDigitalAudioFormat = " + mode);
        switch (mode) {
            case DIGITAL_SOUND_PCM:
                mDroidAudioManager.setDigitalAudioFormatOut(DroidAudioManager.DIGITAL_AUDIO_FORMAT_PCM);
                break;
            case DIGITAL_SOUND_MANUAL:
                mDroidAudioManager.setDigitalAudioFormatOut(DroidAudioManager.DIGITAL_AUDIO_FORMAT_MANUAL,
                        mDroidAudioManager.getAudioManualFormats());
                break;
            case DIGITAL_SOUND_PASSTHROUGH:
                mDroidAudioManager.setDigitalAudioFormatOut(DroidAudioManager.DIGITAL_AUDIO_FORMAT_PASSTHROUGH);
                break;
            case DIGITAL_SOUND_AUTO:
            default:
                mDroidAudioManager.setDigitalAudioFormatOut(DroidAudioManager.DIGITAL_AUDIO_FORMAT_AUTO);
                break;
        }
    }

    public String getDigitalAudioFormat() {
        int surround = mDroidAudioManager.getDigitalAudioFormatOut();
        logDebug(TAG, false, "getDigitalAudioFormat surround: " + DroidAudioManager.audioFormatOutputToString(surround));
        String format = "";
        switch (surround) {
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_PCM:
            format = DIGITAL_SOUND_PCM;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_MANUAL:
            format = DIGITAL_SOUND_MANUAL;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_AUTO:
            format = DIGITAL_SOUND_AUTO;
            break;
        case DroidAudioManager.DIGITAL_AUDIO_FORMAT_PASSTHROUGH:
            format = DIGITAL_SOUND_PASSTHROUGH;
            break;
        default:
            format = DIGITAL_SOUND_AUTO;
        }
        return format;
    }

    public static boolean getSoundEffectsEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
    }
}

