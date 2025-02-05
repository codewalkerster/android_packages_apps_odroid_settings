/*
 * Copyright (C) 2019 The Android Open Source Project
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

package hardkernel.odroid.settings.overlay;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

import hardkernel.odroid.settings.BaseSettingsFragment;
import hardkernel.odroid.settings.SettingsFragmentProvider;


/**
 * Default implementation of the feature factory.
 */
@Keep
public class FeatureFactoryImpl implements FeatureFactory {

    private static final String TAG = "FeatureFactoryImpl";

    @Override
    public SettingsFragmentProvider getSettingsFragmentProvider() {
        return SettingsFragment::newInstance;
    }

    /** A settings fragment suitable for displaying in the default (one panel) layout. */
    public static class SettingsFragment extends BaseSettingsFragment {

        private static final String EXTRA_FRAGMENT_CLASS_NAME = "fragmentClassName";

        public SettingsFragment() {}

        /** Constructs a new instance of a settings fragment. */
        public static SettingsFragment newInstance(String className, Bundle arguments) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = arguments == null ? new Bundle() : new Bundle(arguments);
            args.putString(EXTRA_FRAGMENT_CLASS_NAME, className);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            try {
                String className = getArguments().getString(EXTRA_FRAGMENT_CLASS_NAME);
                final Fragment fragment = (Fragment) Class.forName(className).newInstance();
                fragment.setArguments(getArguments());
                startPreferenceFragment(fragment);
            } catch (IllegalAccessException | ClassNotFoundException
                    | java.lang.InstantiationException e) {
                Log.e(TAG, "Unable to start initial preference screen.", e);
            }
        }
    }
}
