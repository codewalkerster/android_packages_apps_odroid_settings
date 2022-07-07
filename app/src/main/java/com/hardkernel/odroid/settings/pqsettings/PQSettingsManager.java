/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.hardkernel.odroid.settings.pqsettings;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.os.SystemProperties;
import android.provider.Settings;

import com.hardkernel.odroid.settings.R;
import com.droidlogic.app.SystemControlManager;

import com.hardkernel.odroid.settings.TvSettingsActivity;

public class PQSettingsManager {
    public static final String TAG = "PQSettingsManager";

    public static final String KEY_BRIGHTNESS                       = "brightness";
    public static final String KEY_CONTRAST                         = "contrast";
    public static final String KEY_COLOR                            = "color";
    public static final String KEY_SHARPNESS                        = "sharpness";
    public static final String KEY_TONE                             = "tone";

    public static final String STATUS_STANDARD                      = "standard";
    public static final String STATUS_VIVID                         = "vivid";
    public static final String STATUS_SOFT                          = "soft";
    public static final String STATUS_SPORT                         = "sport";
    public static final String STATUS_MONITOR                    = "monitor";
    public static final String STATUS_GAME                          = "game";
    public static final String STATUS_USER                          = "user";
    public static final String STATUS_MOVIE                          = "movie";
    private Context mContext;
    private SystemControlManager mSystemControlManager;

    public PQSettingsManager (Context context) {
        mContext = context;
        mSystemControlManager = SystemControlManager.getInstance();
    }

    static public boolean CanDebug() {
        return SystemProperties.getBoolean("sys.pqsetting.debug", false);
    }

    public static final int PIC_STANDARD = 0;
    public static final int PIC_VIVID = 1;
    public static final int PIC_SOFT = 2;
    public static final int PIC_USER = 3;
    public static final int PIC_MOVIE = 4;
    public static final int PIC_MONITOR = 6;
    public static final int PIC_GAME = 7;
    public static final int PIC_SPORT = 8;

    public int getBrightnessStatus () {
        int value = mSystemControlManager.GetBrightness();
        if (CanDebug()) Log.d(TAG, "getBrightnessStatus : " + value);
        return value;
    }

    public int getContrastStatus () {
        int value = mSystemControlManager.GetContrast();
        if (CanDebug()) Log.d(TAG, "getContrastStatus : " + value);
        return value;
    }

    public int getColorStatus () {
        int value = mSystemControlManager.GetSaturation();
        if (CanDebug()) Log.d(TAG, "getColorStatus : " + value);
        return value;
    }

    public int getSharpnessStatus () {
        int value = mSystemControlManager.GetSharpness();
        if (CanDebug()) Log.d(TAG, "getSharpnessStatus : " + value);
        return value;
    }

    public int getToneStatus () {
        int value = mSystemControlManager.GetHue();
        if (CanDebug()) Log.d(TAG, "getTintStatus : " + value);
        return value;
    }

    public void setPictureMode (String mode) {
        if (CanDebug()) Log.d(TAG, "setPictureMode : " + mode);
        if (mode.equals(STATUS_STANDARD)) {
            mSystemControlManager.SetPQMode(PIC_STANDARD, 1, 0);
        } else if (mode.equals(STATUS_VIVID)) {
            mSystemControlManager.SetPQMode(PIC_VIVID, 1, 0);
        } else if (mode.equals(STATUS_SOFT)) {
            mSystemControlManager.SetPQMode(PIC_SOFT, 1, 0);
        } else if (mode.equals(STATUS_USER)) {
            mSystemControlManager.SetPQMode(PIC_USER, 1, 0);
        } else if (mode.equals(STATUS_MONITOR)) {
            mSystemControlManager.SetPQMode(PIC_MONITOR, 1, 0);
        } else if (mode.equals(STATUS_SPORT)) {
            mSystemControlManager.SetPQMode(PIC_SPORT, 1, 0);
        } else if (mode.equals(STATUS_MOVIE)) {
            mSystemControlManager.SetPQMode(PIC_MOVIE, 1, 0);
        } else if (mode.equals(STATUS_GAME)) {
            mSystemControlManager.SetPQMode(PIC_GAME, 1, 0);
        }
    }

    public void setBrightness (int step) {
        if (CanDebug())  Log.d(TAG, "setBrightness step : " + step );
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            int tmp = mSystemControlManager.GetBrightness();
            mSystemControlManager.SetBrightness(tmp + step, 1);
        } else {
            mSystemControlManager.SetBrightness(setPictureUserMode(KEY_BRIGHTNESS) + step, 1);
        }
    }

    public void setContrast (int step) {
        if (CanDebug())  Log.d(TAG, "setContrast step : " + step);
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            int tmp = mSystemControlManager.GetContrast();
            mSystemControlManager.SetContrast(tmp + step, 1);
        } else {
            mSystemControlManager.SetContrast(setPictureUserMode(KEY_CONTRAST) + step, 1);
        }
    }

    public void setColor (int step) {
        if (CanDebug())  Log.d(TAG, "setColor step : " + step);
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            int tmp = mSystemControlManager.GetSaturation();
            mSystemControlManager.SetSaturation(tmp + step, 1);
        } else {
            mSystemControlManager.SetSaturation(setPictureUserMode(KEY_COLOR) + step, 1);
        }
    }

    public void setSharpness (int step) {
        if (CanDebug())  Log.d(TAG, "setSharpness step : " + step);
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            int tmp = mSystemControlManager.GetSharpness();
            mSystemControlManager.SetSharpness(tmp + step , 1 , 1);
        } else
            mSystemControlManager.SetSharpness(setPictureUserMode(KEY_SHARPNESS) + step, 1 , 1);
    }

    public void setTone(int step) {
        if (CanDebug())  Log.d(TAG, "setTint step : " + step);
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            int tmp = mSystemControlManager.GetHue();
            mSystemControlManager.SetHue(tmp + step, 1);
        } else {
            mSystemControlManager.SetHue(setPictureUserMode(KEY_TONE) + step, 1);
        }
    }

private int setPictureUserMode(String key) {
        if (CanDebug()) Log.d(TAG, "setPictureUserMode : "+ key);
        int brightness = mSystemControlManager.GetBrightness();
        int contrast = mSystemControlManager.GetContrast();
        int color = mSystemControlManager.GetSaturation();
        int tint = -1;
        tint = mSystemControlManager.GetHue();
        int ret = -1;

        switch (mSystemControlManager.GetPQMode()) {
            case PIC_STANDARD:
            case PIC_VIVID:
            case PIC_SOFT:
            case PIC_MONITOR:
            case PIC_SPORT:
            case PIC_MOVIE:
            case PIC_GAME:
                setPictureMode(STATUS_USER);
                break;
        }

        if (CanDebug()) Log.d(TAG, " brightness=" + brightness + " contrast=" + contrast + " color=" + color);
        if (!key.equals(KEY_BRIGHTNESS))
            mSystemControlManager.SetBrightness(brightness, 1);
        else
            ret = brightness;

        if (!key.equals(KEY_CONTRAST))
            mSystemControlManager.SetContrast(contrast, 1);
        else
            ret = contrast;

        if (!key.equals(KEY_COLOR))
            mSystemControlManager.SetSaturation(color, 1);
        else
            ret = color;

        if (!key.equals(KEY_TONE))
            mSystemControlManager.SetHue(tint, 1);
        else
            ret = tint;
        return ret;
    }

    public void resetPQ() {
        int PQMode = mSystemControlManager.GetPQMode();
        if (PQMode == 3) {
            mSystemControlManager.SetBrightness(50, 1);
            mSystemControlManager.SetContrast(50, 1);
            mSystemControlManager.SetSaturation(50, 1);
            mSystemControlManager.SetHue(50, 1);
        } else {
            setPictureUserMode(KEY_BRIGHTNESS);
            setPictureUserMode(KEY_CONTRAST);
            setPictureUserMode(KEY_COLOR);
            setPictureUserMode(KEY_TONE);
        }
    }
}
