package com.hardkernel.odroid.settings.gpu;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;


import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.R;

import java.util.List;

import com.hardkernel.odroid.settings.RadioPreference;

public class scaleModeFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "scaleModeFragment";
    private static GPU gpu = null;

    public static scaleModeFragment newInstance() { return new scaleModeFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (gpu == null) {
            gpu = GPU.getGPU(TAG);
            gpu.initList(getContext());
        }
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.scale_mode);

        setPreferenceScreen(screen);

        List<String> scaleModeList = gpu.getScaleModeList();

        for (int i=0; i < scaleModeList.size(); i++){
            if (i != 0 && i != 2)
                continue;
            final String mode = scaleModeList.get(i);
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(Integer.toString(i));
            radioPreference.setPersistent(false);
            radioPreference.setTitle(mode);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (i == gpu.getScaleModeIdx())
                radioPreference.setChecked(true);
            screen.addPreference(radioPreference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                gpu.setScaleMode(radioPreference.getKey());
                radioPreference.setChecked(true);
            } else
                radioPreference.setChecked(true);
        }
        return super.onPreferenceTreeClick(preference);
    }
}
