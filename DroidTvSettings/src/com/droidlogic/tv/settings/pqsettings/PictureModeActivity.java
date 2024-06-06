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

package com.droidlogic.tv.settings.pqsettings;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.util.Log;
import androidx.fragment.app.Fragment;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.overlay.FlavorUtils;

/**
 * Activity to display pq mode.
 */
public class PictureModeActivity extends TvSettingsActivity {
    private static final String TAG = "PictureModeActivity";
    @Override
    protected Fragment createSettingsFragment() {
        if (SettingsConstant.isTvFeature()) {
            Log.d(TAG, "start panel tv PQ");
            startExportedActivity(SettingsConstant.PACKAGE_NAME_TV_EXTRAS,
                    SettingsConstant.ACTIVITY_NAME_PICTURE);
            return null;
        }
        Log.d(TAG, "start panel ott PQ");
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(PictureModeFragment.class.getName(), null);
    }

    private void startExportedActivity(String packageName, String activityName) {
        try {
            Intent intent =  new Intent();
            intent.setClassName(packageName, activityName);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "start Activity Error not found: " + activityName);
            return;
        }
    }

}
