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

package com.droidlogic.tv.settings.tvsource;

import android.content.ActivityNotFoundException;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.droidlogic.tv.settings.BaseSettingsFragment;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.TvSettingsActivity;

/**
 * Activity that allows the enabling and disabling of sound effects.
 */
public class TvSourceActivity extends TvSettingsActivity {

    private static final String TAG = "TvSourceActivity";

    @Override
    protected Fragment createSettingsFragment() {
        if (SettingsConstant.isTvFeature()) {
            Log.d(TAG, "start panel tv source");
            startExportedActivity(SettingsConstant.PACKAGE_NAME_TV_EXTRAS,
                    SettingsConstant.ACTIVITY_NAME_TV_SOURCE);
            return null;
        }
        Log.d(TAG, "start panel ott home source");
        //return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
        //  .newSettingsFragment(TvSourceFragment.class.getName(), null);
        return new SettingsFragment(this);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public static class SettingsFragment extends BaseSettingsFragment {
        private Context mContext;

        public SettingsFragment(Context context) {
            mContext = context;
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final TvSourceFragment fragment = new TvSourceFragment(mContext);
            startPreferenceFragment(fragment);
        }
    }
}
