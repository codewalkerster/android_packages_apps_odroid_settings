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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;
import com.droidlogic.tv.settings.PreferenceControllerFragment;
import com.droidlogic.tv.settings.sliceprovider.dialog.AdjustResolutionDialogActivity;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.ueventobserver.SetModeUEventObserver;

public class OutputmodeFragment extends SettingsPreferenceFragment {
    private static final String TAG = "OutputmodeFragment";
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private SetModeUEventObserver mSetModeUEventObserver;
    private static String curMode;
    RadioPreference prePreference;
    RadioPreference curPreference;
    private static final int MSG_PLUG_FRESH_UI = 0;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessage(MSG_PLUG_FRESH_UI);
        }
    };

    public static OutputmodeFragment newInstance() {
        return new OutputmodeFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mDisplayCapabilityManager = DisplayCapabilityManager.getDisplayCapabilityManager(getActivity());
        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        updatePreferenceFragment();
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
    }

    private List<Action> getMainActions() {
        List<Action> actions = new ArrayList<Action>();
        List<String> outputModeValueList = mDisplayCapabilityManager.getHdmiModeLists();
        List<String> outputModeTitleList = mDisplayCapabilityManager.getHdmiTitleLists();
        String currentMode = mDisplayCapabilityManager.getCurrentMode();
        Log.i(TAG, "outputModeValueList: " + outputModeValueList + "; \n" +
                "outputModeTitleList: " + outputModeTitleList);
        for (String outputMode : outputModeTitleList) {
            if (outputMode.equals(currentMode)) {
                actions.add(new Action.Builder().key(outputMode)
                        .title("        " + mDisplayCapabilityManager.getTitleByMode(outputMode))
                        .checked(true).build());
            } else {
                actions.add(new Action.Builder().key(outputMode)
                        .title("        " + mDisplayCapabilityManager.getTitleByMode(outputMode))
                        .description("").build());
            }
        }
        return actions;
    }

    @Override
    public void onResume() {
        mSetModeUEventObserver = SetModeUEventObserver.getInstance();
        mSetModeUEventObserver.setOnUEventRunnable(() -> mHandler.sendEmptyMessage(MSG_PLUG_FRESH_UI));
        mSetModeUEventObserver.startObserving();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mIntentReceiver);
        mSetModeUEventObserver.stopObserving();
        mHandler.removeMessages(MSG_PLUG_FRESH_UI);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String sysCurrentMode = mDisplayCapabilityManager.getCurrentMode();
                String UserPreferredDisplayMode = radioPreference.getKey();
                curMode = UserPreferredDisplayMode; //update currentmode
                String UserPreferredDisplayModeTitle = (String)radioPreference.getTitle();
                curPreference = radioPreference;

                Log.d(TAG, "currentMode:" + sysCurrentMode + "; NewMode:"
                        + UserPreferredDisplayMode + "modeTitle:" + UserPreferredDisplayModeTitle);
                Intent intent = new Intent();
                intent.setClass(getActivity(), AdjustResolutionDialogActivity.class);
                intent.putExtra(EXTRA_PREFERENCE_KEY, UserPreferredDisplayMode);
                getActivity().startActivity(intent);
                curPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLUG_FRESH_UI:
                    updatePreferenceFragment();
                    break;
            }
        }
    };

    /**
     * Display Outputmode list based on RadioPreference style.
     */
    private void updatePreferenceFragment() {
        Log.d(TAG, "updatePreferenceFragment");

        mDisplayCapabilityManager.refresh();
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(
                themedContext);
        screen.setTitle(R.string.device_displaymode);
        setPreferenceScreen(screen);

        final List<Action> InfoList = getMainActions();
        for (final Action Info : InfoList) {
            final String InfoTag = Info.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(InfoTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(Info.getTitle());
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (Info.isChecked()) {
                radioPreference.setChecked(true);
                curMode = InfoTag;
                prePreference = curPreference = radioPreference;
            }
            screen.addPreference(radioPreference);
        }
    }
}
