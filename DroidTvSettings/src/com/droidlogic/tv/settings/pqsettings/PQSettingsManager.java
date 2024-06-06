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

package com.droidlogic.tv.settings.pqsettings;

import android.content.Context;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

import com.droidlogic.app.SystemControlManager;
import vendor.amlogic.hardware.systemcontrol.V1_0.SourceInputParam;

public class PQSettingsManager {
    public static final String TAG = "PQSettingsManager";
    SystemControlManager mSystemControlManager;
    public PQSettingsManager (Context context) {
        mSystemControlManager = SystemControlManager.getInstance();
    }

    public int getBrightnessStatus () {
        int value = mSystemControlManager.GetBrightness();
        logDebug(TAG, false, "getBrightnessStatus : " + value);
        return value;
    }

    public int getContrastStatus () {
        int value = mSystemControlManager.GetContrast();
        logDebug(TAG, false, "getContrastStatus : " + value);
        return value;
    }

    public int getColorStatus () {
        int value = mSystemControlManager.GetSaturation();
        logDebug(TAG, false, "getColorStatus : " + value);
        return value;
    }

    public int getSharpnessStatus () {
        int value = mSystemControlManager.GetSharpness();
        logDebug(TAG, false, "getSharpnessStatus : " + value);
        return value;
    }

    public int getToneStatus () {
        int value = mSystemControlManager.GetHue();
        logDebug(TAG, false, "getTintStatus : " + value);
        return value;
    }

    public enum Aspect_Ratio_Mode {
        ASPEC_RATIO_AUTO(0),
        ASPEC_RATIO_43(1),
        ASPEC_RATIO_PANORAMA(2),
        ASPEC_RATIO_FULL_SCREEN(3),
        ASPEC_RATIO_DOT_BY_NOT(4);

        private int val;

        Aspect_Ratio_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public int getAspectRatioStatus () {

        // ON_MPEG_SOURCE_INPUT is TvControlManager.SourceInput.XXXX.toInt()
        final int ON_MPEG_SOURCE_INPUT = 10;
        int itemPosition = mSystemControlManager.GetDisplayMode(ON_MPEG_SOURCE_INPUT);
        logDebug(TAG, false, "getAspectRatioStatus:" + itemPosition);
        if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_MODE43.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_43.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_FULL.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_PANORAMA.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_169.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_FULL_SCREEN.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_NOSCALEUP.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_DOT_BY_NOT.toInt();
        } else {
            return Aspect_Ratio_Mode.ASPEC_RATIO_AUTO.toInt();
        }
    }

    public int getAdvancedColorManagementStatus () {
        logDebug(TAG, false, "getAdvancedColorManagementStatus");
        int colorManagementStatus = mSystemControlManager.GetColorBaseMode();
        return colorManagementStatus != -1 ? colorManagementStatus : 0;
    }

    public int getAdvancedColorSpaceStatus () {
        // Leave blank first, add later
        logDebug(TAG, false, "getAdvancedColorSpaceStatus");
        return 0;
    }


    public int getAdvancedBlackStretchStatus () {
        logDebug(TAG, false, "getAdvancedBlackStretchStatus");
        return mSystemControlManager.GetBlackExtensionMode();
    }

    public int getAdvancedDNLPStatus () {
        logDebug(TAG, false, "getAdvancedDNLPStatus");
        int CurrentSourceInfo[] = mSystemControlManager.GetCurrentSourceInfo();
        int DNLPStatus = mSystemControlManager.getDNLPCurveParams(SystemControlManager.SourceInput.valueOf(CurrentSourceInfo[0]), SystemControlManager.SignalFmt.valueOf(CurrentSourceInfo[1]), SystemControlManager.TransFmt.valueOf(CurrentSourceInfo[2]));
        return DNLPStatus != -1? DNLPStatus :0;
    }

    public int getAdvancedSRStatus () {
        // Leave blank first, add later
        logDebug(TAG, false, "getAdvancedSRStatus");
        return 0;
    }

    public int getDnrStatus () {
        int itemPosition = mSystemControlManager.GetNoiseReductionMode();
        logDebug(TAG, false, "getDnrStatus : " + itemPosition);
        return itemPosition;
    }

    public void setBrightness(int step) {
        logDebug(TAG, false, "setBrightness step : " + step);
        int brightness = mSystemControlManager.GetBrightness();
        mSystemControlManager.SetBrightness(brightness + step, 1);
    }

    public void setContrast(int step) {
        logDebug(TAG, false, "setContrast step : " + step);
        int contrast = mSystemControlManager.GetContrast();
        mSystemControlManager.SetContrast(contrast + step, 1);
    }

    public void setColor(int step) {
        logDebug(TAG, false, "setColor step : " + step);
        int saturation = mSystemControlManager.GetSaturation();
        mSystemControlManager.SetSaturation(saturation + step, 1);
    }

    public void setSharpness(int step) {
        logDebug(TAG, false, "setSharpness step : " + step);
        int sharpness = mSystemControlManager.GetSharpness();
        mSystemControlManager.SetSharpness(sharpness + step, 1, 1);
    }

    public void setTone(int step) {
        logDebug(TAG, false, "setTint step : " + step);
        int hue = mSystemControlManager.GetHue();
        mSystemControlManager.SetHue(hue + step, 1);
    }


    public void setAspectRatio(int mode) {
        logDebug(TAG, true, "setAspectRatio:" + mode);
        // ON_MPEG_SOURCE_INPUT is TvControlManager.SourceInput.XXXX.toInt()
        final int ON_MPEG_SOURCE_INPUT = 10;
        if (mode == 0) {
            mSystemControlManager.SetDisplayMode(ON_MPEG_SOURCE_INPUT, SystemControlManager.Display_Mode.DISPLAY_MODE_NORMAL, 1);
        } else if (mode == 1) {
            mSystemControlManager.SetDisplayMode(ON_MPEG_SOURCE_INPUT, SystemControlManager.Display_Mode.DISPLAY_MODE_MODE43, 1);
        } else if (mode == 2) {
            mSystemControlManager.SetDisplayMode(ON_MPEG_SOURCE_INPUT, SystemControlManager.Display_Mode.DISPLAY_MODE_FULL, 1);
        } else if (mode == 3) {
            mSystemControlManager.SetDisplayMode(ON_MPEG_SOURCE_INPUT, SystemControlManager.Display_Mode.DISPLAY_MODE_169, 1);
        } else if (mode == 4) {
            mSystemControlManager.SetDisplayMode(ON_MPEG_SOURCE_INPUT, SystemControlManager.Display_Mode.DISPLAY_MODE_NOSCALEUP, 1);
        }
    }

    public boolean hasAipqFunc() {
        boolean isAipqFun = mSystemControlManager.hasAipqFunc();
        logDebug(TAG, false, "hasAipqFunc: " + isAipqFun);
        return isAipqFun;
    }

    public int getAipqModeLevel() {
        logDebug(TAG, false, "getAipqEnabled:" + mSystemControlManager.GetAipqMode());
        return mSystemControlManager.GetAipqMode();
    }

    public void setAipqModeLevel(int selection, int save) {
        mSystemControlManager.SetAipqMode(selection, save);
    }

    public boolean getAipqInfo(String aipqInfoEnable) {
        return mSystemControlManager.getPropertyBoolean(aipqInfoEnable, false);
    }

    public boolean hasAisrFunc() {
        boolean isAisrFun = mSystemControlManager.hasAisrFunc();
        logDebug(TAG, false, "hasAipqFunc: " + isAisrFun);
        return isAisrFun;
    }

    public int getAisrModeLevel() {
        logDebug(TAG, true, "getAisrModeLevel:" + mSystemControlManager.GetAipqMode());
        return mSystemControlManager.GetAisrMode();
    }

    public void setAisrModeLevel(int selection, int save) {
        mSystemControlManager.SetAisrMode(selection, save);
    }


    public void setAdvancedColorManagementStatus (int value) {
        logDebug(TAG, true, "setAdvancedColorManagementStatus value:"+value);
        switch (value) {
                case 0:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_OFF, 1);// off
                    break;
                case 1:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode. COLOR_BASE_MODE_OPTIMIZE, 1);// low
                    break;
                case 2:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode. COLOR_BASE_MODE_ENHANCE, 1);// middle
                    break;
                case 3:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_DEMO, 1); // high
                    break;
                default:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_OFF, 1);// off
                    break;
        }
    }

    public void setAdvancedColorSpaceStatus (int value) {
        logDebug(TAG, true, "setAdvancedColorSpaceStatus value:"+value);
        switch (value) {
                case 0:
                    // auto
                    break;
                case 1:
                     // srgb/rec.709
                    break;
                case 2:
                     // dci-p3
                    break;
                case 3:
                     // adobe rgb
                    break;
                case 4:
                     // bt.2020
                    break;
                default:
                    // auto
                    break;
        }
    }

    public void setAdvancedBlackStretchStatus (int value) {
        logDebug(TAG, true, "setAdvancedBlackStretchStatus value:"+value);
        mSystemControlManager.SetBlackExtensionMode(SystemControlManager.Black_Extension_Mode.valueOf(value), 1);
    }

    public void setAdvancedDNLPStatus (int value) {
        logDebug(TAG, true, "setAdvancedDNLPStatus value:"+value);
        int CurrentSourceInfo[] = mSystemControlManager.GetCurrentSourceInfo();
        mSystemControlManager.setDNLPCurveParams(SystemControlManager.SourceInput.valueOf(CurrentSourceInfo[0]), SystemControlManager.SignalFmt.valueOf(CurrentSourceInfo[1]), SystemControlManager.TransFmt.valueOf(CurrentSourceInfo[2]), value);
    }

    public void setAdvancedSRStatus (int value) {
        // Leave blank first, add later
        logDebug(TAG, true, "setAdvancedSRStatus value:"+value);
        switch (value) {
                case 0:
                    // off
                    break;
                case 1:
                     // standard
                    break;
                case 2:
                     // enhance
                    break;
                default:
                    // off
                    break;
        }
    }

    //0 1 2 3 4 ~ off low medium high auto
    public void setDnr (int mode) {
        logDebug(TAG, true, "setDnr : "+ mode);
        mSystemControlManager.SetNoiseReductionMode(mode, 1);
    }

    public void setBacklightValue (int value) {
        logDebug(TAG, true, "setBacklightValue : "+ value);
        mSystemControlManager.SetBacklight(getBacklightStatus() + value, 1);
    }

    public int getBacklightStatus () {
        int value = mSystemControlManager.GetBacklight();
        logDebug(TAG, true, "getBacklightStatus : " + value);
        return value;
    }

    public int SSMRecovery() {
        int value = mSystemControlManager.SSMRecovery();
        logDebug(TAG, true, "SSMRecovery : " + value);
        if (value == 1) {
            //srcInputParam not used
            SourceInputParam srcInputParam= new SourceInputParam();
            value = mSystemControlManager.LoadPQSettings(srcInputParam);
        }
        return value;
    }

    public int getAiColor() {
        logDebug(TAG, true, "getAiColor");
        return mSystemControlManager.GetAiColor();
    }

    public int setAiColor(int value, int isSave) {
        logDebug(TAG, true, "setAiColor value: " + value + ", isSave: " + isSave);
        return mSystemControlManager.SetAiColor(value, isSave);
    }

    public int getAisreDemoEnabled() {
        final int AISR_DEMO = 1;
        logDebug(TAG, false, "getAisreDemoEnabled: " +  mSystemControlManager.GetPQModuleDemoState(AISR_DEMO));
        return mSystemControlManager.GetPQModuleDemoState(AISR_DEMO);
    }

    public int setAisreDemoEnabled(boolean enable) {
        logDebug(TAG, false, "setAisreDemoEnabled enable: " + enable);
        final int AISR_DEMO = 1;
        int stateValue = 0;
        if (enable) {
            stateValue = 1;
        }
        logDebug(TAG, false, "setAisreDemoEnabled stateValue: " + stateValue);
        return mSystemControlManager.SetPQModuleDemoState(AISR_DEMO, stateValue);
    }

}
