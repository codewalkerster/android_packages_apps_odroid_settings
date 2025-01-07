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

package hardkernel.odroid.settings.more;

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
import androidx.preference.TwoStatePreference;

import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.content.ActivityNotFoundException;

import hardkernel.odroid.settings.util.DroidUtils;
import hardkernel.odroid.settings.SettingsConstant;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import hardkernel.odroid.settings.pqsettings.PQSettingsManager;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.SystemControlManager;

import hardkernel.odroid.settings.R;

public class MorePrefFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{
    private static final String TAG = "MorePrefFragment";

    private static final String KEY_MAIN_MENU = "moresettings";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_WIFI_HOSTSPOT = "wifi_hotspot";
    private static final String KEY_POWERKEY = "powerkey_action";
    private static final String KEY_POWERONMODE = "poweronmode_action";
    private static final String KEY_UPGRADE_BLUETOOTH_REMOTE = "upgrade_bluetooth_remote";
    private static final String KEY_PLAYBACK_SETTINGS = "playback_settings";
    private static final String KEY_SOUNDS = "key_sound_effects";
    private static final String KEY_KEYSTONE = "keyStone";
    private static final String KEY_HDMI_CEC_CONTROL = "hdmicec";
    private static final String KEY_ADVANCE_SOUND = "advanced_sound_settings";
    private static final String KEY_DEVELOP_OPTION = "amlogic_developer_options";
    private static final String KEY_FRAME_RATE = "frame_rate";
    private static final String KEY_TV_EXTRAS = "tv_extras";


    private static final String FRAME_RATE_PROP = "persist.vendor.sys.framerate.feature";
    private static final String DEBUG_GLOBAL_SETTING = "droidsetting_debug";

    public static final String WATCH_FEATURE = "android.hardware.type.watch";
    public static final String TV_FEATURE = "android.hardware.type.television";
    public static final String AUTOMOTIVE_FEATURE = "android.hardware.type.automotive";
    private static final String KEY_PICTURE = "picture_mode";
    public static final String KEY_ENABLE_OSD_SHARPNESS = "pq_osd_sharpness_enabled";
    public static final String FEATURE_HDMI_CEC = "android.hardware.hdmi.cec";

    private Preference mSoundsPref;

    private String mEsnText;
    private SystemControlManager mSystemControlManager;
    private PQSettingsManager mPQSettingsManager;

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
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        boolean is_from_new_live_tv = getActivity().getIntent().getIntExtra("from_new_live_tv", 0) == 1;
        boolean is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1 || is_from_new_live_tv;
        //tvFlag, is true when TV and T962E as TV, false when Mbox and T962E as Mbox.
        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
                && (SystemProperties.getBoolean("vendor.tv.soc.as.mbox", false) == false);
        mSystemControlManager = SystemControlManager.getInstance();

        boolean isShowFrameRate = mSystemControlManager.getPropertyBoolean(FRAME_RATE_PROP, false);

        final Preference morePref = findPreference(KEY_MAIN_MENU);
        final Preference displayPref = findPreference(KEY_DISPLAY);
        final Preference wifiHotspotPref = findPreference(KEY_WIFI_HOSTSPOT);
        final Preference hdmicecPref = findPreference(KEY_HDMI_CEC_CONTROL);
        final Preference playbackPref = findPreference(KEY_PLAYBACK_SETTINGS);
        mSoundsPref = findPreference(KEY_SOUNDS);
        mSoundsPref.setIcon(SoundParameterSettingManager.getSoundEffectsEnabled(getContext().getContentResolver())
                ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);

        final Preference powerKeyPref = findPreference(KEY_POWERKEY);
        final Preference powerKeyOnModePref = findPreference(KEY_POWERONMODE);
        final Preference keyStone = findPreference(KEY_KEYSTONE);
        //BluetoothRemote/HDMI cec/Playback Settings display only in Mbox
        final Preference mUpgradeBluetoothRemote = findPreference(KEY_UPGRADE_BLUETOOTH_REMOTE);
        final Preference picturePref = findPreference(KEY_PICTURE);
        final Preference advanced_sound_settings_pref = findPreference(KEY_ADVANCE_SOUND);
        final Preference frameRatePref = findPreference(KEY_FRAME_RATE);
        final Preference tvExtrasPref = findPreference(KEY_TV_EXTRAS);

        Log.d(TAG, "isShowFrameRate: " + isShowFrameRate);
        frameRatePref.setVisible(isShowFrameRate);
        advanced_sound_settings_pref.setVisible(false);
        //hide it forcedly as new bluetooth remote upgrade application is not available now
        mUpgradeBluetoothRemote.setVisible(false/*is_from_live_tv ? false : (SettingsConstant.needDroidlogicBluetoothRemoteFeature(getContext()) && !tvFlag)*/);
        if (SettingsConstant.needGTVFeature(getContext())) {
            hdmicecPref.setVisible(false);
        } else {
            hdmicecPref.setVisible((isSupportFeature(FEATURE_HDMI_CEC)
                    && SettingsConstant.needDroidlogicHdmicecFeature(getContext())) && !is_from_live_tv);
        }
        playbackPref.setVisible(false);

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
            powerKeyPref.setVisible(false);
            powerKeyOnModePref.setVisible(false);
            keyStone.setVisible(false);
        } else {
            wifiHotspotPref.setVisible(!isHandheld());  //Tablet devices do not display.
            if (!DroidLogicUtils.isTv()) {
                powerKeyOnModePref.setVisible(false);
            }
            DroidUtils.store(getActivity(), DroidUtils.KEY_HIDE_STARTUP, DroidUtils.VALUE_HIDE_STARTUP);
        }
        if (!SettingsConstant.isTvFeature()) {
            tvExtrasPref.setVisible(false);
        } else {
            picturePref.setVisible(false);
        }

        TwoStatePreference enableOsdSharpnessPref = (TwoStatePreference) findPreference(KEY_ENABLE_OSD_SHARPNESS);
        enableOsdSharpnessPref.setOnPreferenceChangeListener(this);
        enableOsdSharpnessPref.setChecked(mPQSettingsManager.getOsdSharpnessEnabled());
        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_OSD_SHARPNESS)) {
            enableOsdSharpnessPref.setEnabled(false);
        }

        if (DroidUtils.hasGtvsUiMode()) {
            Log.i(TAG, "hide powerkey_action");
            powerKeyPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_ENABLE_OSD_SHARPNESS)) {
            mPQSettingsManager.setOsdSharpnessEnabled((boolean) newValue);
        }
        return true;
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
