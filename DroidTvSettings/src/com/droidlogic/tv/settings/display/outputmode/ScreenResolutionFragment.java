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

package com.droidlogic.tv.settings.display.outputmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.droidlogic.app.DolbyVisionSettingManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.ueventobserver.SetModeUEventObserver;
import com.droidlogic.tv.settings.SettingsConstant;

public class ScreenResolutionFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ScreenResolutionFragment";
    private static final String KEY_COLOR_FORMAT = "color_format_key";
    private static final String KEY_DISPLAYMODE = "displaymode_setting";
    private static final String KEY_BEST_RESOLUTION = "best_resolution";
    private static final String KEY_BEST_DOLBYVISION = "best_dolbyvision";
    private static final String KEY_DOLBYVISION = "dolby_vision";
    private static final String KEY_HDR_PRIORITY = "hdr_priority";
    private static final String KEY_HDR_POLICY = "hdr_policy";
    private static final String KEY_DOLBYVISION_PRIORITY = "dolby_vision_graphics_priority";
    private static final String DEVICE_DISPLAY_RESET = "device_display_reset";

    private Preference mBestResolutionPref;
    private Preference mBestDolbyVisionPref;
    private Preference mDisplayModePref;
    private Preference mColorFormat;
    private Preference mDolbyVisionPref;
    private Preference mHdrPriorityPref;
    private Preference mHdrPolicyPref;
    private Preference mGraphicsPriorityPref;

    private static final String HDMI_OUTPUT_MODE = "dummy_l";

    private static final String SETTINGS_PACKAGE = "com.droidlogic.tv.settings";
    private static final String SETTINGS_ACTIVITY_DisplayResetActivity =
            "com.droidlogic.tv.settings.sliceprovider.dialog.DisplayResetActivity";

    private static final int DV_LL_RGB          = 3;
    private static final int DV_LL_YUV          = 2;
    private static final int DV_ENABLE          = 1;


    private static final int DV_PRIORITY        = 0;
    private static final int HDR_PRIORITY       = 1;
    private static final int SDR_PRIORITY       = 2;

    private String preMode;
    private SetModeUEventObserver mSetModeUEventObserver;
    private static final int MSG_FRESH_UI = 0;

    private DolbyVisionSettingManager mDolbyVisionSettingManager;
    private OutputModeManager mOutputModeManager;

    private DisplayCapabilityManager mDisplayCapabilityManager;
    private IntentFilter mIntentFilter;
    public boolean hpdFlag = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRESH_UI:
                    Log.d(TAG, "CurrentOutputMode:" + mDisplayCapabilityManager.getCurrentMode() + " HdmiMode:" + isHdmiMode());
                    if (!(HDMI_OUTPUT_MODE.equals(mDisplayCapabilityManager.getCurrentMode())
                            && isHdmiMode())) {
                        updateScreenResolutionDisplay();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hpdFlag = intent.getBooleanExtra("state", false);
            mHandler.sendEmptyMessageDelayed(MSG_FRESH_UI, hpdFlag ^ isHdmiMode() ? 2000 : 1000);
        }
    };

    public static ScreenResolutionFragment newInstance() {
        return new ScreenResolutionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDisplayCapabilityManager = DisplayCapabilityManager.getDisplayCapabilityManager(getActivity());
        mOutputModeManager = OutputModeManager.getInstance(getActivity());
        mDolbyVisionSettingManager = new DolbyVisionSettingManager((Context) getActivity());

        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.screen_resolution, null);
        mBestResolutionPref = findPreference(KEY_BEST_RESOLUTION);
        mBestDolbyVisionPref = findPreference(KEY_BEST_DOLBYVISION);
        mBestResolutionPref.setOnPreferenceChangeListener(this);
        mBestDolbyVisionPref.setOnPreferenceChangeListener(this);
        mDisplayModePref = findPreference(KEY_DISPLAYMODE);
        mColorFormat = findPreference(KEY_COLOR_FORMAT);
        mDolbyVisionPref = findPreference(KEY_DOLBYVISION);
        mHdrPriorityPref = findPreference(KEY_HDR_PRIORITY);
        mHdrPolicyPref = findPreference(KEY_HDR_POLICY);
        mGraphicsPriorityPref = findPreference(KEY_DOLBYVISION_PRIORITY);
        updateScreenResolutionDisplay();
    }

    @Override
    public void onResume() {
        mSetModeUEventObserver = SetModeUEventObserver.getInstance();
        mSetModeUEventObserver.setOnUEventRunnable(() -> mHandler.sendEmptyMessage(MSG_FRESH_UI));
        mSetModeUEventObserver.startObserving();
        mHandler.sendEmptyMessage(MSG_FRESH_UI);
        super.onResume();
    }

    @Override
    public void onPause() {
        mSetModeUEventObserver.stopObserving();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    private void updateScreenResolutionDisplay() {
        Log.i(TAG, "showUI and at this time current isBestOutputmode? "
                + mDisplayCapabilityManager.isBestResolution());

        if (getContext() == null) {
            Log.d(TAG, "context is NULL");
            return;
        }

        // output mode.
        mDisplayCapabilityManager.refresh();
        String currentMode = mDisplayCapabilityManager.getTitleByMode(mDisplayCapabilityManager.getCurrentMode());
        mDisplayModePref.setSummary(currentMode);

        boolean dvFlag = mDisplayCapabilityManager.isTvSupportDolbyVision()
                && (mOutputModeManager.getHdrPriority() == DV_PRIORITY);
        boolean cfFlag = !(mDisplayCapabilityManager.isDolbyVisionEnable() && mDisplayCapabilityManager.isTvSupportDolbyVision()
                && (mOutputModeManager.getHdrPriority() == DV_PRIORITY));
        //socSupportDv: if the chip is G12A/G12B/SM1 and T962E/E2 etc, it will be true
        //platformSupportDv: if the chip support dv and the code contains dovi.ko, it will be true
        boolean isSocSupportDv = mDolbyVisionSettingManager.isSocSupportDolbyVision();
        boolean socSupportDv = isSocSupportDv &&
                (!SettingsConstant.needDroidlogicTvFeature(getPreferenceManager().getContext()) || SystemProperties.getBoolean("vendor.tv.soc.as.mbox", false));
        boolean platformSupportDv = mDolbyVisionSettingManager.isMboxSupportDolbyVision();
        boolean displayConfig = SettingsConstant.needDroidlogicBestDolbyVision(getPreferenceManager().getContext());
        boolean customConfig = mOutputModeManager.isSupportNetflix();
        boolean debugConfig = mOutputModeManager.isSupportDisplayDebug();

        Log.i(TAG, "isSocSupportDv " + isSocSupportDv);
        Log.i(TAG, "platformSupportDv " + platformSupportDv);
        Log.i(TAG, "socSupportDv " + socSupportDv);
        Log.i(TAG, "displayConfig " + displayConfig);
        Log.i(TAG, "customConfig " + customConfig);
        Log.i(TAG, "debugConfig " + debugConfig);

        if (isHdmiMode()) {

            mBestResolutionPref.setVisible(true);
            mBestResolutionPref.setEnabled(true);
            ((SwitchPreference) mBestResolutionPref).setChecked(isBestResolution());
            if (isBestResolution()) {
                mBestResolutionPref.setSummary(R.string.captions_display_on);
            } else {
                mBestResolutionPref.setSummary(R.string.captions_display_off);
            }

            // color space/color format
            mColorFormat.setVisible(cfFlag);
            mColorFormat.setEnabled(cfFlag);
            String currentColorFormat = mDisplayCapabilityManager.getCurrentColorAttribute();
            mColorFormat.setSummary(mDisplayCapabilityManager.getTitleByColorAttr(currentColorFormat));

            // dolby vision
            mDolbyVisionPref.setVisible(platformSupportDv && displayConfig && dvFlag);
            mDolbyVisionPref.setEnabled(displayConfig && dvFlag);
            if (mDisplayCapabilityManager.isDolbyVisionEnable()) {
                if (mDolbyVisionSettingManager.getDolbyVisionType() == 2) {
                    mDolbyVisionPref.setSummary(R.string.dolby_vision_low_latency_yuv);
                } else if (mDolbyVisionSettingManager.getDolbyVisionType() == 3) {
                    mDolbyVisionPref.setSummary(R.string.dolby_vision_low_latency_rgb);
                } else {
                    if (mDisplayCapabilityManager.isTvSupportDolbyVision()) {
                        mDolbyVisionPref.setSummary(R.string.dolby_vision_sink_led);
                    } else {
                        mDolbyVisionPref.setSummary(R.string.dolby_vision_default_enable);
                    }
                }
            } else {
                mDolbyVisionPref.setSummary(R.string.dolby_vision_off);
            }

            // HDR policy
            mHdrPolicyPref.setVisible(isSocSupportDv);
            if (mDisplayCapabilityManager.getHdrStrategy().equals("0")) {
                mHdrPolicyPref.setSummary(R.string.hdr_policy_sink);
            } else if (mDisplayCapabilityManager.getHdrStrategy().equals("1")) {
                mHdrPolicyPref.setSummary(R.string.hdr_policy_source);
            }

            //dolby vision graphic
            mGraphicsPriorityPref.setVisible(isSocSupportDv
                    && mDisplayCapabilityManager.isDolbyVisionEnable()
                    && displayConfig);
            if (mDolbyVisionSettingManager.getGraphicsPriority().equals("1")) {
                mGraphicsPriorityPref.setSummary(R.string.graphics_priority);
            } else if (mDolbyVisionSettingManager.getGraphicsPriority().equals("0")) {
                mGraphicsPriorityPref.setSummary(R.string.video_priority);
            }

            // hdr priority
            mHdrPriorityPref.setVisible(platformSupportDv);
            if (mOutputModeManager.getHdrPriority() == 1) {
                mHdrPriorityPref.setSummary(R.string.hdr10);
            } else if (mOutputModeManager.getHdrPriority() == 2) {
                mHdrPriorityPref.setSummary(R.string.sdr);
            } else {
                mHdrPriorityPref.setSummary(R.string.dolby_vision);
            }

            // best dolby vision.
            mBestDolbyVisionPref.setVisible(false);
            ((SwitchPreference) mBestDolbyVisionPref).setChecked(isBestDolbyVision());
            if (isBestDolbyVision()) {
                mBestDolbyVisionPref.setSummary(R.string.captions_display_on);
            } else {
                mBestDolbyVisionPref.setSummary(R.string.captions_display_off);
            }

            //for custom design
            if (!debugConfig && customConfig) {
                mDolbyVisionPref.setVisible(false);
                if (mDisplayCapabilityManager.isDolbyVisionEnable()) {
                    mColorFormat.setVisible(false);
                }
                mGraphicsPriorityPref.setEnabled(false);
                mHdrPolicyPref.setVisible(false);
            }

        } else {
            mBestResolutionPref.setVisible(false);
            mColorFormat.setVisible(false);
            mBestDolbyVisionPref.setVisible(false);
            mDolbyVisionPref.setVisible(false);
            mGraphicsPriorityPref.setVisible(false);
            mHdrPolicyPref.setVisible(false);
            mHdrPriorityPref.setVisible(false);
        }
        if (mDisplayCapabilityManager.getSystemPreferredDisplayMode()) {
            removeResolutionPreference();
        }

    }

    /**
     * If it is the system preferred display mode,
     * This slice is implemented by TvSetting using Preference on AndroidT.
     */
    private void removeResolutionPreference() {
        if (mBestResolutionPref != null) {
            getPreferenceScreen().removePreference(mBestResolutionPref);
        }
        if (mDisplayModePref != null) {
            getPreferenceScreen().removePreference(mDisplayModePref);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_BEST_RESOLUTION)) {
            preMode = getCurrentDisplayMode();
            if ((boolean) newValue) {
                setBestResolution();
                mHandler.sendEmptyMessage(MSG_FRESH_UI);
                /*if (isBestResolution()) {
                    showDialog();
                }*/
            } else {
                mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(preMode);
                mHandler.sendEmptyMessage(MSG_FRESH_UI);
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_BEST_DOLBYVISION)) {
            int type = mDolbyVisionSettingManager.getDolbyVisionType();
            String mode = mDolbyVisionSettingManager.isTvSupportDolbyVision();
            if (!isBestDolbyVision()) {
                if (!mode.equals("")) {
                    if (mode.contains("LL_YCbCr_422_12BIT")) {
                        mDolbyVisionSettingManager.setDolbyVisionEnable(DV_LL_YUV);
                    } else if (mode.contains("DV_RGB_444_8BIT")) {
                        mDolbyVisionSettingManager.setDolbyVisionEnable(DV_ENABLE);
                    } else if ((mode.contains("LL_RGB_444_12BIT") || mode.contains("LL_RGB_444_10BIT"))) {
                        mDolbyVisionSettingManager.setDolbyVisionEnable(DV_LL_RGB);
                    }
                }
                setBestDolbyVision(true);
            } else {
                setBestDolbyVision(false);
            }

            mHandler.sendEmptyMessage(MSG_FRESH_UI);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.i(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        switch (preference.getKey()) {
            case DEVICE_DISPLAY_RESET:
                Intent displayResetIntent = new Intent();
                displayResetIntent.setClassName(SETTINGS_PACKAGE, SETTINGS_ACTIVITY_DisplayResetActivity);
                startActivity(displayResetIntent);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private boolean isBestResolution() {
        return mDisplayCapabilityManager.isBestResolution();
    }

    private boolean isBestDolbyVision() {
        return mOutputModeManager.isBestDolbyVsion();
    }

    /**
     * Taggle best resolution state.
     * if current best resolution state is enable, it will disable best resolution after method.
     * if current best resolution state is disable, it will enable best resolution after method.
     */
    private void setBestResolution() {
        mDisplayCapabilityManager.change2BestMode();
    }

    private void setBestDolbyVision(boolean enable) {
        mOutputModeManager.setBestDolbyVision(enable);
    }

    private String getCurrentDisplayMode() {
        return mDisplayCapabilityManager.getCurrentMode();
    }

    private boolean isHdmiMode() {
        return !mDisplayCapabilityManager.isCvbsMode();
    }
}
