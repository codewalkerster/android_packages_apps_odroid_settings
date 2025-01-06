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

package hardkernel.odroid.settings.display.rotation;

import android.os.Bundle;
import android.os.Handler;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.ListPreference;
import androidx.preference.TwoStatePreference;

import android.content.Context;
import android.util.Log;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.util.DroidUtils;
import hardkernel.odroid.settings.SettingsConstant;
import hardkernel.odroid.settings.R;
import android.view.Display;
import hardkernel.odroid.settings.RadioPreference;

public class RotationFragment extends SettingsPreferenceFragment {

    private static final String TAG = "RotationFragment";

    private static int degree;

    public static RotationFragment newInstance() {
        return new RotationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        degree = display.getRotation();
        updatePreferenceFragment();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final RadioPreference radioPreference = (RadioPreference)preference;
        radioPreference.clearOtherRadioPreferences(getPreferenceScreen());

        String getDegree = radioPreference.getKey();

        Log.e(TAG, "set rotation : " + getDegree);

        switch (Integer.valueOf(getDegree)) {
            case 0:
                degree = 0;
                break;
            case 90:
                degree = 1;
                break;
            case 180:
                degree = 2;
                break;
            case 270:
                degree = 3;
                break;
        }
        final Context context = getPreferenceManager().getContext();

        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, degree);
        updatePreferenceFragment();

        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.rotation_title);
        setPreferenceScreen(screen);

        RadioPreference rotate_0;
        RadioPreference rotate_90;
        RadioPreference rotate_180;
        RadioPreference rotate_270;

        rotate_0 = new RadioPreference(themedContext);
        rotate_0.setKey("0");
        rotate_0.setPersistent(false);
        rotate_0.setTitle(R.string.rotate_0);
        rotate_0.setLayoutResource(R.layout.preference_reversed_widget);

        rotate_90 = new RadioPreference(themedContext);
        rotate_90.setKey("90");
        rotate_90.setPersistent(false);
        rotate_90.setTitle(R.string.rotate_90);
        rotate_90.setLayoutResource(R.layout.preference_reversed_widget);

        rotate_180 = new RadioPreference(themedContext);
        rotate_180.setKey("180");
        rotate_180.setPersistent(false);
        rotate_180.setTitle(R.string.rotate_180);
        rotate_180.setLayoutResource(R.layout.preference_reversed_widget);

        rotate_270 = new RadioPreference(themedContext);
        rotate_270.setKey("270");
        rotate_270.setPersistent(false);
        rotate_270.setTitle(R.string.rotate_270);
        rotate_270.setLayoutResource(R.layout.preference_reversed_widget);

        switch (degree) {
            case 0:
                rotate_0.setChecked(true);
                break;
            case 1:
                rotate_90.setChecked(true);
                break;
            case 2:
                rotate_180.setChecked(true);
                break;
            case 3:
                rotate_270.setChecked(true);
                break;
        }

        screen.addPreference(rotate_0);
        screen.addPreference(rotate_90);
        screen.addPreference(rotate_180);
        screen.addPreference(rotate_270);
    }
}
