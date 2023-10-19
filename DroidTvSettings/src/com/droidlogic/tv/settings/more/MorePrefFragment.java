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

package com.droidlogic.tv.settings.more;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.RemoteException;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.content.ActivityNotFoundException;

import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.SystemControlManager;

import com.droidlogic.tv.settings.R;

public class MorePrefFragment extends SettingsPreferenceFragment {
    private static final String TAG = "MorePrefFragment";

    private static final String KEY_MAIN_MENU = "moresettings";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_WIFI_HOSTSPOT = "wifi_hotspot";
    private static final String KEY_MBOX_SOUNDS = "mbox_sound";
    private static final String KEY_POWERKEY = "powerkey_action";
    private static final String KEY_POWERONMODE = "poweronmode_action";
    private static final String KEY_UPGRADE_BLUETOOTH_REMOTE = "upgrade_bluetooth_remote";
    private static final String KEY_PLAYBACK_SETTINGS = "playback_settings";
    private static final String KEY_SOUNDS = "key_sound_effects";
    private static final String KEY_KEYSTONE = "keyStone";
    private static final String KEY_NETFLIX_ESN = "netflix_esn";
    private static final String KEY_VERSION = "hailstorm_ver";
    private static final String KEY_HDMI_CEC_CONTROL = "hdmicec";
    private static final String KEY_ADVANCE_SOUND = "advanced_sound_settings";
    private static final String KEY_DEVELOP_OPTION = "amlogic_developer_options";
    private static final String KEY_AI_PQ = "ai_pq";
    private static final String KEY_FRAME_RATE = "frame_rate";
    private static final String KEY_TV_EXTRAS = "tv_extras";


    private static final String HAILSTORM_VERSION_PROP = "ro.vendor.hailstorm.version";
    private static final String FRAME_RATE_PROP = "persist.vendor.sys.framerate.feature";
    private static final String DEBUG_GLOBAL_SETTING = "droidsetting_debug";

    public static final String WATCH_FEATURE = "android.hardware.type.watch";
    public static final String TV_FEATURE = "android.hardware.type.television";
    public static final String AUTOMOTIVE_FEATURE = "android.hardware.type.automotive";
    public static final String FEATURE_SOFTWARE_NETFLIX = "droidlogic.software.netflix";
    public static final String FEATURE_HDMI_CEC = "android.hardware.hdmi.cec";

    private Preference mSoundsPref;

    private String mEsnText;
    private SystemControlManager mSystemControlManager;

    private BroadcastReceiver esnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mEsnText = intent.getStringExtra("ESNValue");
            findPreference(KEY_NETFLIX_ESN).setSummary(mEsnText);
        }
    };

    public static MorePrefFragment newInstance() {
        return new MorePrefFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.more, null);
        boolean is_from_new_live_tv = getActivity().getIntent().getIntExtra("from_new_live_tv", 0) == 1;
        boolean is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1 || is_from_new_live_tv;
        //tvFlag, is true when TV and T962E as TV, false when Mbox and T962E as Mbox.
        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
                && (SystemProperties.getBoolean("vendor.tv.soc.as.mbox", false) == false);
        mSystemControlManager = SystemControlManager.getInstance();

        boolean isSupportNetflix = isSupportFeature(FEATURE_SOFTWARE_NETFLIX);
        boolean isShowFrameRate = mSystemControlManager.getPropertyBoolean(FRAME_RATE_PROP, false);

        final Preference morePref = findPreference(KEY_MAIN_MENU);
        final Preference displayPref = findPreference(KEY_DISPLAY);
        final Preference wifiHotspotPref = findPreference(KEY_WIFI_HOSTSPOT);
        final Preference hdmicecPref = findPreference(KEY_HDMI_CEC_CONTROL);
        final Preference playbackPref = findPreference(KEY_PLAYBACK_SETTINGS);
        mSoundsPref = findPreference(KEY_SOUNDS);
        final Preference mboxSoundsPref = findPreference(KEY_MBOX_SOUNDS);
        final Preference powerKeyPref = findPreference(KEY_POWERKEY);
        final Preference powerKeyOnModePref = findPreference(KEY_POWERONMODE);
        final Preference keyStone = findPreference(KEY_KEYSTONE);
        //BluetoothRemote/HDMI cec/Playback Settings display only in Mbox
        final Preference mUpgradeBluetoothRemote = findPreference(KEY_UPGRADE_BLUETOOTH_REMOTE);
        final Preference netflixesnPref = findPreference(KEY_NETFLIX_ESN);
        final Preference versionPref = findPreference(KEY_VERSION);
        final Preference advanced_sound_settings_pref = findPreference(KEY_ADVANCE_SOUND);
        final Preference aipq = findPreference(KEY_AI_PQ);
        final Preference frameRatePref = findPreference(KEY_FRAME_RATE);
        final Preference tvExtrasPref = findPreference(KEY_TV_EXTRAS);

        Log.d(TAG, "isShowFrameRate: " + isShowFrameRate);
        frameRatePref.setVisible(isShowFrameRate);
        advanced_sound_settings_pref.setVisible(false);
        //hide it forcedly as new bluetooth remote upgrade application is not available now
        mUpgradeBluetoothRemote.setVisible(false/*is_from_live_tv ? false : (SettingsConstant.needDroidlogicBluetoothRemoteFeature(getContext()) && !tvFlag)*/);
        aipq.setVisible(mSystemControlManager.hasAipqFunc());
        if (SettingsConstant.needGTVFeature(getContext())) {
            hdmicecPref.setVisible(false);
        } else {
            hdmicecPref.setVisible((isSupportFeature(FEATURE_HDMI_CEC)
                    && SettingsConstant.needDroidlogicHdmicecFeature(getContext())) && !is_from_live_tv);
        }
        playbackPref.setVisible(false);
        if (netflixesnPref != null) {
            if (is_from_live_tv) {
                netflixesnPref.setVisible(false);
                versionPref.setVisible(false);
            } else if (isSupportNetflix) {
                netflixesnPref.setVisible(true);
                netflixesnPref.setSummary(mEsnText);
                versionPref.setVisible(true);
                versionPref.setSummary(mSystemControlManager.getPropertyString(HAILSTORM_VERSION_PROP, "no"));
                powerKeyPref.setVisible(false);
                keyStone.setVisible(false);

            } else {
                netflixesnPref.setVisible(false);
                versionPref.setVisible(false);
            }
        }

        final Preference developPref = findPreference(KEY_DEVELOP_OPTION);
        if ((1 == Settings.Global.getInt(getContext().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0))
                && SystemProperties.get("ro.product.brand").contains("Amlogic")) {
            developPref.setVisible(true);
        } else {
            developPref.setVisible(false);
        }

        if (is_from_live_tv) {
            morePref.setTitle(R.string.settings_menu);
            displayPref.setVisible(false);
            wifiHotspotPref.setVisible(false);
            mboxSoundsPref.setVisible(false);
            powerKeyPref.setVisible(false);
            powerKeyOnModePref.setVisible(false);
            keyStone.setVisible(false);
            if (!SettingsConstant.needDroidlogicTvFeature(getContext())) {
                mSoundsPref.setVisible(false);//mbox doesn't support sound effect
            }
        } else {
            wifiHotspotPref.setVisible(!isHandheld());  //Tablet devices do not display.
            mSoundsPref.setVisible(false);
            if (!DroidLogicUtils.isTv()) {
                powerKeyOnModePref.setVisible(false);
            }
            DroidUtils.store(getActivity(), DroidUtils.KEY_HIDE_STARTUP, DroidUtils.VALUE_HIDE_STARTUP);

            if (!SettingsConstant.isTvFeature()) {
                tvExtrasPref.setVisible(false);
            }
        }

        if (DroidUtils.hasGtvsUiMode()) {
            Log.i(TAG, "hide powerkey_action");
            powerKeyPref.setVisible(false);
        }

        if (0 == Settings.Global.getInt(getContext().getContentResolver(), DEBUG_GLOBAL_SETTING, 0)) {
            advanced_sound_settings_pref.setVisible(false);
            mboxSoundsPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        if (TextUtils.equals(preference.getKey(), KEY_KEYSTONE)) {
            startExportedActivity(SettingsConstant.PACKAGE_NAME_KEYSTONE, SettingsConstant.ACTIVITY_NAME_KEYSTONE);
        } else if (TextUtils.equals(preference.getKey(), KEY_ADVANCE_SOUND)) {
            startExportedActivity(SettingsConstant.PACKAGE_NAME_SOUNDEFFECT, SettingsConstant.ACTIVITY_NAME_SOUNDEFFECT);
        } else if (TextUtils.equals(preference.getKey(), KEY_TV_EXTRAS)) {
            startExportedActivity(SettingsConstant.PACKAGE_NAME_TV_EXTRAS, SettingsConstant.ACTIVITY_NAME_TV_EXTRAS);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void startExportedActivity(String packageName, String activityName) {
        try {
            Intent intent = new Intent();
            if (TextUtils.equals(activityName, SettingsConstant.ACTIVITY_NAME_TV_EXTRAS)) {
                intent= getActivity().getIntent();
            }
            intent.setClassName(packageName, activityName);
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "start Activity Error not found: " + activityName);
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSounds();
        IntentFilter esnIntentFilter = new IntentFilter("com.netflix.ninja.intent.action.ESN_RESPONSE");
        getActivity().getApplicationContext().registerReceiver(esnReceiver, esnIntentFilter, Context.RECEIVER_EXPORTED);
        Intent esnQueryIntent = new Intent("com.netflix.ninja.intent.action.ESN");
        esnQueryIntent.setPackage("com.netflix.ninja");
        esnQueryIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        getActivity().getApplicationContext().sendBroadcast(esnQueryIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (esnReceiver != null) {
            getActivity().getApplicationContext().unregisterReceiver(esnReceiver);
        }
    }

    private void updateSounds() {
        if (mSoundsPref == null) {
            return;
        }

        mSoundsPref.setIcon(SoundParameterSettingManager.getSoundEffectsEnabled(getContext().getContentResolver())
                ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
    }

    private boolean isHandheld() {
        return isSupportFeature(PackageManager.FEATURE_TOUCHSCREEN)
                && !isSupportFeature(PackageManager.FEATURE_PC)
                && !isSupportFeature(WATCH_FEATURE)
                && !isSupportFeature(TV_FEATURE)
                && !isSupportFeature(AUTOMOTIVE_FEATURE);
    }

    private boolean isSupportFeature(String featureName) {
        return SettingsConstant.isSupportFeature(featureName, getActivity());
    }
}
