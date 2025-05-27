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

package hardkernel.odroid.settings.pqsettings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.text.TextUtils;

import com.droidlogic.app.SystemControlManager;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.R;
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

public class PictureModeFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PictureModeFragment";
    private static final String PQ_AI_PQ = "ai_pq";
    private static final String PQ_CUSTOM = "pq_custom";
    private static final String PQ_ASPECT_RATIO = "pq_aspect_ratio";
    private static final String PQ_BACKLIGHT = "pq_backlight";
    private static final String PQ_ALLRESET = "pq_allreset";

    private PQSettingsManager mPQSettingsManager;

    public static PictureModeFragment newInstance() {
        return new PictureModeFragment();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_picture_mode, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        //final ListPreference aspectratioPref = (ListPreference) findPreference(PQ_ASPECT_RATIO);
        //if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_ASPECT_RATIO)) {
        //    aspectratioPref.setValueIndex(mPQSettingsManager.getAspectRatioStatus());
        //    aspectratioPref.setOnPreferenceChangeListener(this);
        //} else {
        //    aspectratioPref.setEnabled(false);
        //}

        final Preference aipqPref = (Preference) findPreference(PQ_AI_PQ);

        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_PQ)
                || mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_AI_SR)) {
            aipqPref.setEnabled(true);
        } else {
            aipqPref.setEnabled(false);
        }

        final Preference backlightPref = (Preference) findPreference(PQ_BACKLIGHT);
        backlightPref.setSummary(mPQSettingsManager.getBacklightStatus() + "%");
        //The set-top box does not contain this menu, but it does not mean
        // that it can be deleted entirely, and it is a better way to hide it.
        backlightPref.setVisible(false);

        final Preference pictureAllResetPref = (Preference) findPreference(PQ_ALLRESET);
        if (!mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_RESET)) {
            pictureAllResetPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, true, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        switch (preference.getKey()) {
            case PQ_ALLRESET:
                Intent PQAllResetIntent = new Intent();
                PQAllResetIntent.setClassName(
                        "hardkernel.odroid.settings",
                        "hardkernel.odroid.settings.pqsettings.PQResetAllActivity");
                startActivity(PQAllResetIntent);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, true, "[onPreferenceChange] preference.getKey() = "
                + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), PQ_ASPECT_RATIO)) {
            final int selection = Integer.parseInt((String) newValue);
            mPQSettingsManager.setAspectRatio(selection);
        }
        return true;
    }

    private boolean curPictureModeShow() {
        return mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_CONTRAST)
                || mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_BRIGHTNESS)
                || mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_SATURATION)
                || mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_HUE)
                || mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_SHARPNESS);
    }

}
