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
import android.util.Log;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;

import com.droidlogic.tv.settings.dialog.ProgressingDialogUtil;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.R;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.ueventobserver.SetModeUEventObserver;

@Keep
public class ColorAttributeFragment extends SettingsPreferenceFragment {
    private static final String LOG_TAG = "ColorAttributeFragment";
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private static String saveValue = null;
    private static String curValue = null;
    private static String curMode = null;
    private static final int MSG_FRESH_UI = 0;
    private IntentFilter mIntentFilter;
    public boolean hpdFlag = false;
    private static final String DEFAULT_VALUE = "444,8bit";
    private static final String DEFAULT_TITLE = "YCbCr444 8bit";
    private Context themedContext;
    private ProgressingDialogUtil mProgressingDialogUtil;
    private String mOldColorSpace;
    private String mNewColorSpace;
    private SetModeUEventObserver mSetModeUEventObserver;

    private ArrayList<String> colorTitleList = new ArrayList();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hpdFlag = intent.getBooleanExtra ("state", false);
            mHandler.sendEmptyMessage(MSG_FRESH_UI);
        }
    };

    public static ColorAttributeFragment newInstance() {
        return new ColorAttributeFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mDisplayCapabilityManager = DisplayCapabilityManager.getDisplayCapabilityManager(getActivity());
        themedContext = getPreferenceManager().getContext();
        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mProgressingDialogUtil = new ProgressingDialogUtil();
        updatePreferenceFragment();
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
    }

    private void updatePreferenceFragment() {
        mDisplayCapabilityManager.refresh();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(
                themedContext);
        screen.setTitle(R.string.device_outputmode_color_space);
        setPreferenceScreen(screen);
        if (!isHdmiMode()) {
            colorTitleList.clear();
            return;
        }
        final List<Action> InfoList = getMainActions();
        for (final Action Info : InfoList) {
            final String InfoTag = Info.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(InfoTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(Info.getTitle());
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (Info.isChecked()) {
                mNewColorSpace = InfoTag;
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }

    private boolean isModeSupportColor(final String curMode, final String curValue){
        return mDisplayCapabilityManager.doesModeSupportColor(curMode, curValue);
    }

    private List<String> getTitleListByColorAttr(List<String> colorAttrList) {
        List<String> colorListTitle = new ArrayList<>();
        for (String colorAttr : colorAttrList) {
            colorListTitle.add(mDisplayCapabilityManager.getTitleByColorAttr(colorAttr));
        }
        return colorListTitle;
    }

    private List<Action> getMainActions() {
        List<Action> actions = new ArrayList<Action>();
        colorTitleList.clear();
        List<String> colorValueList = mDisplayCapabilityManager.getColorAttributes();
        List<String> colorTitleList = getTitleListByColorAttr(colorValueList);
        String value = null;
        String filterValue = null;
        String  curColorSpaceValue = mDisplayCapabilityManager.getCurrentColorAttribute();
        Log.i(LOG_TAG, "curColorSpaceValue: " + curColorSpaceValue);
        if ("default".equals(curColorSpaceValue)) {
            curColorSpaceValue = DEFAULT_VALUE;
        }

        for (int i = 0; i < colorTitleList.size(); i++) {
            value = colorValueList.get(i).trim();
            curMode = mDisplayCapabilityManager.getCurrentMode();
            if (!isModeSupportColor(curMode, value)) {
                continue;
            }
            filterValue += value;
        }

        for (int i = 0; i < OutputModeManager.HDMI_COLOR_LIST.length; i++) {
            if (filterValue ==null) {
                break;
            }
            if (filterValue.contains(OutputModeManager.HDMI_COLOR_LIST[i])) {
                if (curColorSpaceValue.contains(OutputModeManager.HDMI_COLOR_LIST[i])) {
                    actions.add(new Action.Builder().key(OutputModeManager.HDMI_COLOR_LIST[i])
                        .title(OutputModeManager.HDMI_COLOR_TITLE_LIST[i])
                        .checked(true).build());
                } else {
                    actions.add(new Action.Builder().key(OutputModeManager.HDMI_COLOR_LIST[i])
                        .title(OutputModeManager.HDMI_COLOR_TITLE_LIST[i])
                        .description("").build());
                }
            }
        }
        if (actions.size() == 0) {
            actions.add(new Action.Builder().key(DEFAULT_VALUE)
                .title(DEFAULT_TITLE)
                .checked(true).build());
        }
        return actions;
    }
    @Override
    public void onResume() {
        mSetModeUEventObserver = SetModeUEventObserver.getInstance();
        mSetModeUEventObserver.setOnUEventRunnable(() -> mHandler.sendEmptyMessage(MSG_FRESH_UI));
        mSetModeUEventObserver.startObserving();
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
        mHandler.removeMessages(MSG_FRESH_UI);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        mOldColorSpace = mNewColorSpace;
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                mNewColorSpace = radioPreference.getKey();
                Log.d(LOG_TAG, "mOldColorSpace = " + mOldColorSpace + ", mNewColorSpace = " + mNewColorSpace);
                if (onClickHandle(mNewColorSpace) == true) {
                    radioPreference.setChecked(true);
                }
                String newHdrPolicyTitle = radioPreference.getTitle().toString();
                mProgressingDialogUtil.showWarningDialogOnResolutionChange(themedContext, newHdrPolicyTitle,
                        new ProgressingDialogUtil.DialogCallBackInterface() {
                            @Override
                            public void positiveCallBack() {

                            }
                            @Override
                            public void negativeCallBack() {
                                onClickHandle(mOldColorSpace);
                                mHandler.sendEmptyMessage(MSG_FRESH_UI);
                            }
                        });
            } else {
                radioPreference.setChecked(true);
                Log.i(LOG_TAG,"not checked");
            }
        }
      return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    public boolean onClickHandle(String key) {
        curValue = key;
        saveValue= mDisplayCapabilityManager.getCurrentColorAttribute();
        if (saveValue.equals("default")) {
            saveValue = DEFAULT_VALUE;
        }
        curMode = mDisplayCapabilityManager.getCurrentMode();
        Log.i(LOG_TAG,"Set Color Space Value: "+curValue + "CurValue: "+saveValue);
        if (!curValue.equals(saveValue)) {
            if (!isModeSupportColor(curMode, curValue)) {
                curValue = DEFAULT_VALUE;
            }
            mDisplayCapabilityManager.setColorAttribute(curValue);
            return true;
        }
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRESH_UI:
                    updatePreferenceFragment();
                    break;
                default:
                    break;
            }
        }
    };

    private boolean isHdmiMode() {
        return !mDisplayCapabilityManager.isCvbsMode();
    }
}
