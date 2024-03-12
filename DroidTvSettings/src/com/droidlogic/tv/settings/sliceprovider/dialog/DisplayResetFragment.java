/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.droidlogic.tv.settings.sliceprovider.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.annotation.Nullable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.droidlogic.tv.settings.R;
import com.droidlogic.app.SystemControlManager;

import java.util.List;

import android.hardware.display.DisplayManager;
import android.hardware.display.HdrConversionMode;

public class DisplayResetFragment extends GuidedStepFragment {

    private static final String TAG = "DisplayResetFragment";
    private SystemControlManager mSystemControlManager;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSystemControlManager = SystemControlManager.getInstance();
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                getString(R.string.device_display_reset_Title),
                getString(R.string.device_display_reset_description),
                null,
                getContext().getDrawable(R.drawable.ic_settings_backup_restore_132dp));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions,
                                Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getContext())
                .clickAction(GuidedAction.ACTION_ID_OK)
                .title(getString(R.string.device_display_reset))
                .build());
        actions.add(new GuidedAction.Builder(getContext())
                .clickAction(GuidedAction.ACTION_ID_CANCEL)
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == GuidedAction.ACTION_ID_OK) {
            mSystemControlManager.clearUserDisplayConfig();
            DisplayManager mDisplayManager = (DisplayManager)getContext().getSystemService(DisplayManager.class);
            mDisplayManager.setAreUserDisabledHdrTypesAllowed(true);
            int userDisabledHdrTypes[] = {};
            mDisplayManager.setUserDisabledHdrTypes(userDisabledHdrTypes);
            HdrConversionMode systemHdrConversionMode = new HdrConversionMode(
                    HdrConversionMode.HDR_CONVERSION_SYSTEM);
            mDisplayManager.setHdrConversionMode(systemHdrConversionMode);
            getActivity().finish();
        }  else {
            getActivity().finish();
        }
    }

    @Override
    public int onProvideTheme() {
        return R.style.BluetoothActionGuidedStepTheme;
    }
}
