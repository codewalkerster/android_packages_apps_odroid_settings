/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC PQAdvancedFragment
 */



package hardkernel.odroid.settings.pqsettings.advanced;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsConstant;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.pqsettings.PQSettingsManager;
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;
import com.droidlogic.app.SystemControlManager;

public class PQAdvancedFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "PQAdvancedFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT = "pq_picture_advanced_color_management";
    private static final String PQ_PICTURE_ADVANCED_COLOR_SPACE = "pq_picture_advanced_color_space";
    private static final String PQ_PICTURE_ADVANCED_BLACK_STRETCH = "pq_picture_advanced_black_stretch";
    private static final String PQ_PICTURE_ADVANCED_DNLP = "pq_picture_advanced_dnlp";
    private static final String PQ_PICTURE_ADVANCED_SR = "pq_picture_advanced_sr";
    private static final String PQ_DNR = "pq_dnr";

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedFragment newInstance() {
        return new PQAdvancedFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_picture_advanced, null);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final ListPreference pictureAdvancedColorManagementPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT);
        final ListPreference pictureAdvancedColorSpacePref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_SPACE);
        final ListPreference pictureAdvancedBlackStretchPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_BLACK_STRETCH);
        final ListPreference pictureAdvancedDNLPPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DNLP);
        final ListPreference pictureAdvancedSRPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_SR);
        final ListPreference pictureAdvancedDNRPref = (ListPreference) findPreference(PQ_DNR);

        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_COLOR_MANAGEMENT)) {
            pictureAdvancedColorManagementPref.setValueIndex(mPQSettingsManager.getAdvancedColorManagementStatus());
            pictureAdvancedColorManagementPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedColorManagementPref.setEnabled(false);
        }
        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_COLOR_SPACE)) {
            pictureAdvancedColorSpacePref.setValueIndex(mPQSettingsManager.getAdvancedColorSpaceStatus());
            pictureAdvancedColorSpacePref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedColorSpacePref.setEnabled(false);
        }
        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_BLACK_STRETCH)) {
            pictureAdvancedBlackStretchPref.setValueIndex(mPQSettingsManager.getAdvancedBlackStretchStatus());
            pictureAdvancedBlackStretchPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedBlackStretchPref.setEnabled(false);
        }
        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_DNLP)) {
            pictureAdvancedDNLPPref.setValueIndex(mPQSettingsManager.getAdvancedDNLPStatus());
            pictureAdvancedDNLPPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedDNLPPref.setEnabled(false);
        }
        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_SR)) {
            pictureAdvancedSRPref.setValueIndex(mPQSettingsManager.getAdvancedSRStatus());
            pictureAdvancedSRPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedSRPref.setEnabled(false);
        }
        if (mPQSettingsManager.hasPqCaseFunc(SystemControlManager.PqFuncCase.PQ_CASE_FUNC_DNR)) {
            pictureAdvancedDNRPref.setValueIndex(mPQSettingsManager.getDnrStatus());
            pictureAdvancedDNRPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedDNRPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, true, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());

        final int selection = Integer.parseInt((String)newValue);
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT:
                mPQSettingsManager.setAdvancedColorManagementStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_SPACE:
                mPQSettingsManager.setAdvancedColorSpaceStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_BLACK_STRETCH:
                mPQSettingsManager.setAdvancedBlackStretchStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_DNLP:
                mPQSettingsManager.setAdvancedDNLPStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_SR:
                mPQSettingsManager.setAdvancedSRStatus(selection);
                break;
            case PQ_DNR:
                mPQSettingsManager.setDnr(selection);
                break;
            default:
                break;
        }
        return true;
    }
}
