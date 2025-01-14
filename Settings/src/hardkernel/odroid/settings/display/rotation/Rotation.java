/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import hardkernel.odroid.settings.ConfigEnv;

public class Rotation {
    private static final String TAG = "Rotation";
    public static void setOrientation(int degree, Context context) {
        Log.e(TAG, "set rotation : " + degree);
        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, degree / 90);

        ConfigEnv.setOrientation(Integer.toString(degree));
    }

    public static int getOrientation() {
        return Integer.valueOf(ConfigEnv.getOrientation());
    }
}
