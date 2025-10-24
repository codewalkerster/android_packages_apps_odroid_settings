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

package hardkernel.odroid.settings.develop;

import android.os.Bundle;
import android.os.Handler;
import androidx.preference.SwitchPreference;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import android.os.SystemProperties;
import android.text.TextUtils;
import hardkernel.odroid.settings.util.DroidUtils;
import hardkernel.odroid.settings.SettingsConstant;
import hardkernel.odroid.settings.R;
import com.droidlogic.app.SystemControlManager;
import android.util.Log;
import android.provider.Settings;

public class DevelopFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DevelopFragment";

    private static final String KEY_KERNEL_LOG_CONFIG    = "kernel_loglevel_config";
    private static final String KEY_DTVKIT               = "dtvkit_features";
    private static final String KEY_DROIDSETTING_DEBUG	 = "key_droidsetting_debug";

    public static final String ENV_KERNEL_LOG_LEVEL      = "ubootenv.var.loglevel";
    private static final String DEBUG_GLOBAL_SETTING = "droidsetting_debug";

    private SystemControlManager mSystemControlManager;
    private Preference mKernelLogPref;
    private Preference mDtvkitPref;
    private SwitchPreference mDroidSettings;


    public static DevelopFragment newInstance() {
        return new DevelopFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.develop, null);
        mSystemControlManager = SystemControlManager.getInstance();

        mKernelLogPref=findPreference(KEY_KERNEL_LOG_CONFIG);
        mDtvkitPref=findPreference(KEY_DTVKIT);
        mKernelLogPref.setOnPreferenceChangeListener(this);
        if (!SystemProperties.get("ro.product.brand").contains("Amlogic")) {
            mKernelLogPref.setVisible(false);
            mDtvkitPref.setVisible(false);
        }
        String level = mSystemControlManager.getBootenv(ENV_KERNEL_LOG_LEVEL, "1");
        if (level.contains("1")) {
            ((SwitchPreference)mKernelLogPref).setChecked(false);
            mKernelLogPref.setSummary(R.string.captions_display_off);
        } else {
            ((SwitchPreference)mKernelLogPref).setChecked(true);
            mKernelLogPref.setSummary(R.string.captions_display_on);
        }
        mDroidSettings=findPreference(KEY_DROIDSETTING_DEBUG);
        mDroidSettings.setChecked(1 == Settings.Global.getInt(getContext().getContentResolver(), DEBUG_GLOBAL_SETTING, 0));
        mDroidSettings.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_KERNEL_LOG_CONFIG)) {
            String level = mSystemControlManager.getBootenv(ENV_KERNEL_LOG_LEVEL, "1");
            if (level.contains("1")) {
                mSystemControlManager.setBootenv(ENV_KERNEL_LOG_LEVEL, "7");
                mKernelLogPref.setSummary(R.string.captions_display_on);
            } else {
                mSystemControlManager.setBootenv(ENV_KERNEL_LOG_LEVEL, "1");
                mKernelLogPref.setSummary(R.string.captions_display_off);
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_DROIDSETTING_DEBUG)) {
                Settings.Global.putInt(getContext().getContentResolver(),
                    DEBUG_GLOBAL_SETTING, (boolean)newValue ? 1 : 0 );
        }
        return true;
    }
    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
