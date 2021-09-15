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
package hardkernel.odroid.settings.cpu.frequency;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.widget.Toast;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;

import hardkernel.odroid.settings.RadioPreference;
//import com.hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.cpu.CPU;

public class FrequencyFragment extends SettingsPreferenceFragment {
    private static final String TAG = "FrequencyFragment";
    public static CPU cpu = null;

    public static FrequencyFragment newInstance() { return new FrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (cpu == null) {
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        }
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        if (cpu.cluster == CPU.Cluster.Big)
            screen.setTitle(R.string.big_core_clock);
        else if (cpu.cluster == CPU.Cluster.Little)
            screen.setTitle(R.string.little_core_clock);
        else
            screen.setTitle(R.string.cpu);

        setPreferenceScreen(screen);

        String[] frequencyList = cpu.frequency.getFrequencies(getContext());

        for (final String frequency : frequencyList) {
            if (Integer.parseInt(frequency) < cpu.frequency.getPolicyMin())
                continue;

            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(frequency);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(frequency);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (cpu.frequency.getScalingCurrent().equals(frequency)) {
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedFrequency = radioPreference.getKey();
                if (cpu.frequency.getPolicyMax()
                        < Integer.valueOf(selectedFrequency)) {
                    Toast.makeText(getContext(),
                            "Selected Clock will be applied after reboot",
                            Toast.LENGTH_LONG).show();
                }
                cpu.frequency.setScalingMax(selectedFrequency);
                saveFrequency(selectedFrequency);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveFrequency(String frequency) {
        /*
        if (cpu.cluster == CPU.Cluster.Little)
            ConfigEnv.setLittleCoreFreq(frequency);
        else if (cpu.cluster == CPU.Cluster.Big)
            ConfigEnv.setBigCoreFreq(frequency);
            */
    }
}
