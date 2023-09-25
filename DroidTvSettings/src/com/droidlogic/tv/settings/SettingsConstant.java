/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.droidlogic.tv.settings;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import java.util.Objects;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.SystemControlManager;

/**
 * Settings related constants
 */
public class SettingsConstant {

    public static String PACKAGE = "com.droidlogic.tv.settings";
    public static String PACKAGENAME_LAUNCHERX = "com.google.android.apps.tv.launcherx";
    public static String PACKAGENAME_MBOX_LAUNCHER2 = "com.droidlogic.mboxlauncher";
    private static final String AMATI_FEATURE = "com.google.android.feature.AMATI_EXPERIENCE";

    public static String PACKAGE_NAME_KEYSTONE = "com.android.keystone";
    public static String ACTIVITY_NAME_KEYSTONE =  "com.android.keystone.keyStoneCorrectionActivity";

    public static String PACKAGE_NAME_SOUNDEFFECT = "com.droidlogic.tv.settings";
    public static String ACTIVITY_NAME_SOUNDEFFECT = "com.droidlogic.tv.settings.soundeffect.AdvancedVolumeActivity";

    public static String PACKAGE_NAME_TV_EXTRAS = "com.droidlogic.tv.extras";
    public static String ACTIVITY_NAME_TV_EXTRAS = "com.droidlogic.tv.extras.MainActivity";
    public static String ACTIVITY_NAME_PICTURE = "com.droidlogic.tv.extras.pqsettings.PictureModeActivity";
    public static String ACTIVITY_NAME_TV_OPTION = "com.droidlogic.tv.extras.tvoption.DroidSettingsModeActivity";

    public static boolean needDroidlogicMboxFeature(Context context){
        SystemControlManager sm = SystemControlManager.getInstance();
        return sm.getPropertyBoolean("ro.vendor.platform.has.mbxuimode", false);
    }

    public static boolean needGTVFeature(Context context) {

        final Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo homeInfo = context.getPackageManager().resolveActivity(homeIntent, 0);
        if (Objects.equals(PACKAGENAME_LAUNCHERX, homeInfo.activityInfo.packageName)) {
            return true;
        }
        return false;
    }

    public static boolean isSupportFeature(String featureName, Context context) {
        return context.getPackageManager().hasSystemFeature(featureName);
    }

    public static boolean needAospFeature(Context context) {

        final Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo homeInfo = context.getPackageManager().resolveActivity(homeIntent, 0);
        if (Objects.equals(PACKAGENAME_MBOX_LAUNCHER2, homeInfo.activityInfo.packageName)) {
            return true;
        }
        return false;
    }

    public static boolean needDroidlogicTvFeature(Context context){
        return DroidLogicUtils.isTv();
    }
    public static boolean needDroidlogicHdrFeature(Context context){
        return context.getResources().getBoolean(R.bool.display_need_hdr_function);
    }
    public static boolean needDroidlogicSdrFeature(Context context){
        return context.getResources().getBoolean(R.bool.display_need_sdr_function);
    }
    public static boolean needDroidlogicBestDolbyVision(Context context){
        return context.getResources().getBoolean(R.bool.display_need_dolby_vision_function);
    }
    public static boolean needDroidlogicDigitalSounds(Context context){
        return context.getResources().getBoolean(R.bool.display_need_digital_sounds);
    }
    public static boolean needScreenResolutionFeture(Context context){
        return context.getResources().getBoolean(R.bool.display_need_screen_resolution);
    }
    public static boolean needDroidlogicHdmicecFeature(Context context){
        SystemControlManager sm = SystemControlManager.getInstance();
        return sm.getPropertyBoolean("ro.vendor.platform.need.display.hdmicec", false);
    }
    public static boolean needDroidlogicPlaybackSetFeature(Context context){
        return context.getResources().getBoolean(R.bool.display_need_playback_set_function);
    }
    public static boolean needDroidlogicBluetoothRemoteFeature(Context context){
        return context.getResources().getBoolean(R.bool.display_need_bluetooth_remote_function);
    }

    public static boolean hasMboxFeature(Context context){
        SystemControlManager sm = SystemControlManager.getInstance();
        return sm.getPropertyBoolean("vendor.tv.soc.as.mbox", false);
    }

    public static boolean needDroidlogicCustomization(Context context){
        SystemControlManager sm = SystemControlManager.getInstance();
        return sm.getPropertyBoolean("ro.vendor.platform.customize_tvsetting", false);
    }

    public static boolean isTvFeature() {
        SystemControlManager sm = SystemControlManager.getInstance();
        return ("1".equals(sm.getPropertyString("ro.vendor.platform.is.tv", "")));
    }

}
