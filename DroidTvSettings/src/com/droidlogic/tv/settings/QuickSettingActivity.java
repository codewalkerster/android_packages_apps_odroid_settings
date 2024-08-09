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

package com.droidlogic.tv.settings;

import androidx.fragment.app.Fragment;
import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.overlay.FlavorUtils;

// DroidLogic start
import android.view.KeyEvent;
import android.content.Intent;
// DroidLogic end

import android.util.Log;

/**
 * Activity to display quick setting.
 */
public class QuickSettingActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(QuickSettingFragment.class.getName(), null);
    }

    private static final String TAG = "QuickSettingUI";

    /*
     * Long press the red button for two seconds and release it,
     * then short press the blue button to open the debug UI.
     */
    boolean redLongPress = false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int eventAction = event.getAction();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        if (eventAction == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_PROG_RED && repeatCount >= 3) {
            redLongPress = true;
            return false;
        } else if (eventAction == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_PROG_BLUE){
            if (redLongPress) {
                /*
                 * SWPL-196256 remove debug audio UI
                 * Keep the code but disable the functionality.
                Intent intent=new Intent();
                intent.setClassName("com.droidlogic.tv.settings","com.droidlogic.tv.settings.soundeffect.DebugAudioUIActivity");
                startActivity(intent);
                */
                redLongPress = false;
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
