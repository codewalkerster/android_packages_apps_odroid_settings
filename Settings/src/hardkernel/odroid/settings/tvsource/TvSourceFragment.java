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

package hardkernel.odroid.settings.tvsource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.text.TextUtils;

import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.R;

import java.util.Collections;
import java.util.List;

import java.util.ArrayList;

import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

public class TvSourceFragment extends SettingsPreferenceFragment {

    private static final String TAG = "TvSourceFragment";

    private static final String AMATI_FEATURE = "com.google.android.feature.AMATI_EXPERIENCE";
    private static final String INPUT_SOURCE_GOOGLE_HOME_KEY = "home";
    private static final String INPUT_SOURCE_AIRPLAY_KEY = "airplay";

    private static final String KEY_AIRPLAY_PACKAGE_NAME = "com.amlogic.airplay";

    private Context mContext;

    public TvSourceFragment() {
    }

    public TvSourceFragment(Context context) {
        mContext = context;
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(
                themedContext);
        screen.setTitle(R.string.tv_source);
        setPreferenceScreen(screen);

        try {
            List<Preference> preferenceList = new ArrayList<Preference>();
            if (themedContext.getPackageManager().hasSystemFeature(AMATI_FEATURE)) {
                logDebug(TAG, true, "show input");
                Preference sourcePreference = new Preference(themedContext);
                sourcePreference.setKey(INPUT_SOURCE_GOOGLE_HOME_KEY);
                sourcePreference.setPersistent(false);
                sourcePreference.setIcon(R.drawable.ic_home);
                if (isBasicMode(themedContext)) {
                    sourcePreference.setTitle(R.string.channels_and_inputs_home_title);
                } else {
                    sourcePreference.setTitle(R.string.channels_and_inputs_home_google_title);
                }
                logDebug(TAG, true, "show input add ");
                screen.addPreference(sourcePreference);
            }

            if (isApkInstalled(KEY_AIRPLAY_PACKAGE_NAME)) {
                Preference airPlayinputPre = new Preference(themedContext);
                airPlayinputPre.setKey(INPUT_SOURCE_AIRPLAY_KEY);
                airPlayinputPre.setPersistent(false);
                airPlayinputPre.setIcon(R.drawable.air_play);
                airPlayinputPre.setTitle("AirPlay");
                screen.addPreference(airPlayinputPre);
            }

        } catch (Exception e) {
            logDebug(TAG, true, "inputList is " + e.getMessage());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final Preference sourcePreference = preference;
        if (sourcePreference.getKey().equals(INPUT_SOURCE_GOOGLE_HOME_KEY)) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getPreferenceManager().getContext().startActivity(homeIntent);
            ((Activity) mContext).finish();
            return true;
        }

        if (sourcePreference.getKey().equals(INPUT_SOURCE_AIRPLAY_KEY)) {
            Intent airplayIntent = new Intent("com.amlogic.tv.AIRPLAY_LAUNCH");
            airplayIntent.putExtra("bring-to-foreground", true);
            getContext().sendBroadcast(airplayIntent, "com.amlogic.permission.AP_APP_LAUNCH_REQUEST");
            ((Activity)mContext).finish();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }


    public boolean isBasicMode(Context context) {
        final String SETTINGS_PACKAGE_NAME = "hardkernel.odroid.settings";
        String providerUriString = "";
        try {
            Resources resources = context.getPackageManager()
                    .getResourcesForApplication(SETTINGS_PACKAGE_NAME);
            int id = resources.getIdentifier("basic_mode_provider_uri", "string", SETTINGS_PACKAGE_NAME);
            if (id != 0) {
                providerUriString = resources.getString(id);
            }
        } catch (Exception e) {
            return false;
        }
        if (TextUtils.isEmpty(providerUriString)) {
            logDebug(TAG, false, "ContentProvider for basic mode is undefined.");
            return false;
        }
        // The string "offline_mode" is a static protocol and should not be changed in general.
        final String KEY_BASIC_MODE = "offline_mode";
        try {
            Uri contentUri = Uri.parse(providerUriString);
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                String basicMode = cursor.getString(cursor.getColumnIndex(KEY_BASIC_MODE));
                return "1".equals(basicMode);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            logDebug(TAG, true, "Unable to query the ContentProvider for basic mode.");
            return false;
        }
        return false;
    }

    private boolean isApkInstalled(String packageName) {
        PackageManager packageManager = getPreferenceManager().getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
