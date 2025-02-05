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

package hardkernel.odroid.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.TextUtils;
import com.droidlogic.app.SystemControlManager;

public class FrameRateFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "FrameRateFragment";

    private static final String KEY_ENABLE_FRAME_RATE = "frame_rate_enable";

    private static final String PROP_FRAME_RATE_ENABLE = "persist.vendor.sys.framerate.enable";
    private static final String SAVE_FRAME_RATE = "FRAME_RATE";
    private static final int FRAME_RATE_ENABLE = 1;
    private static final int FRAME_RATE_DISABLE = 0;

    public static FrameRateFragment newInstance() {
        return new FrameRateFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.framerate, null);
        final TwoStatePreference mEnableFrameRatePref = (TwoStatePreference) findPreference(KEY_ENABLE_FRAME_RATE);
        mEnableFrameRatePref.setOnPreferenceChangeListener(this);
        mEnableFrameRatePref.setChecked(getFrameRateEnabled());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_ENABLE_FRAME_RATE)) {
            if ((boolean) newValue) {
                Settings.Global.putInt(getActivity().getContentResolver(), SAVE_FRAME_RATE, FRAME_RATE_ENABLE);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(), SAVE_FRAME_RATE, FRAME_RATE_DISABLE);
            }
        }
        return true;
    }

    private Boolean getFrameRateEnabled() {
        return SystemControlManager.getInstance().getPropertyBoolean(PROP_FRAME_RATE_ENABLE, false);
    }
}
