/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.hardkernel.odroid.settings.pqsettings;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.provider.Settings;
import android.widget.Button;

import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class AdjustValueFragment extends LeanbackAddBackPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "AdjustValueFragment";

    private SeekBar seekbar_brightness;
    private SeekBar seekbar_contrast;
    private SeekBar seekbar_saturation;
    private SeekBar seekbar_hue;
    private TextView text_brightness;
    private TextView text_contrast;
    private TextView text_saturation;
    private TextView text_hue;
    private PQSettingsManager mPQSettingsManager;
    private boolean isSeekBarInited = false;

    /*
    private SeekBar seekbar_sharpness;
    private TextView text_sharpness;
    */

	private final String KEY_PQ_RESET = "pq_reset";

    public static AdjustValueFragment newInstance() {
        return new AdjustValueFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.seekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    private void initSeekBar(View view) {
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        int status = -1;
        boolean hasfocused = false;

        seekbar_brightness = (SeekBar) view.findViewById(R.id.seekbar_brightness);
        text_brightness = (TextView) view.findViewById(R.id.text_brightness);

        status = mPQSettingsManager.getBrightnessStatus();
        seekbar_brightness.setOnSeekBarChangeListener(this);
        seekbar_brightness.setProgress(status);
        setShow(R.id.seekbar_brightness, status);
        seekbar_brightness.requestFocus();
        hasfocused = true;

        seekbar_contrast = (SeekBar) view.findViewById(R.id.seekbar_contrast);
        text_contrast = (TextView) view.findViewById(R.id.text_contrast);

        status = mPQSettingsManager.getContrastStatus();
        seekbar_contrast.setOnSeekBarChangeListener(this);
        seekbar_contrast.setProgress(status);
        setShow(R.id.seekbar_contrast, status);
        if (!hasfocused) {
            seekbar_contrast.requestFocus();
            hasfocused = true;
        }

        seekbar_saturation = (SeekBar) view.findViewById(R.id.seekbar_saturation);
        text_saturation = (TextView) view.findViewById(R.id.text_saturation);

        status = mPQSettingsManager.getColorStatus();
        seekbar_saturation.setOnSeekBarChangeListener(this);
        seekbar_saturation.setProgress(status);
        setShow(R.id.seekbar_saturation, status);
        if (!hasfocused) {
            seekbar_saturation.requestFocus();
            hasfocused = true;
        }

        /*
        seekbar_sharpness = (SeekBar) view.findViewById(R.id.seekbar_sharpness);
        text_sharpness = (TextView) view.findViewById(R.id.text_sharpness);

        status = mPQSettingsManager.getSharpnessStatus();
        seekbar_sharpness.setOnSeekBarChangeListener(this);
        seekbar_sharpness.setProgress(status);
        setShow(R.id.seekbar_sharpness, status);
        if (!hasfocused) {
            seekbar_sharpness.requestFocus();
            hasfocused = true;
        }
        */

        seekbar_hue= (SeekBar) view.findViewById(R.id.seekbar_hue);
        text_hue= (TextView) view.findViewById(R.id.text_hue);

        status = mPQSettingsManager.getToneStatus();
        seekbar_hue.setOnSeekBarChangeListener(this);
        seekbar_hue.setProgress(status);
        setShow(R.id.seekbar_hue, status);
        if (!hasfocused) {
            seekbar_hue.requestFocus();
            hasfocused = true;
        }

        isSeekBarInited = true;

        Button pq_reset = (Button) view.findViewById(R.id.pq_reset);
        pq_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShow(R.id.seekbar_brightness, 50);
                seekbar_brightness.setProgress(50);

                setShow(R.id.seekbar_contrast , 50);
                seekbar_contrast.setProgress(50);

                setShow(R.id.seekbar_saturation, 50);
                seekbar_saturation.setProgress(50);

                setShow(R.id.seekbar_hue, 50);
                seekbar_hue.setProgress(50);
                mPQSettingsManager.resetPQ();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.seekbar_brightness:{
                setShow(R.id.seekbar_brightness, progress);
                mPQSettingsManager.setBrightness(progress - mPQSettingsManager.getBrightnessStatus());
                break;
            }
            case R.id.seekbar_contrast:{
                setShow(R.id.seekbar_contrast, progress);
                mPQSettingsManager.setContrast(progress - mPQSettingsManager.getContrastStatus());
                break;
            }
            case R.id.seekbar_saturation:{
                setShow(R.id.seekbar_saturation, progress);
                mPQSettingsManager.setColor(progress - mPQSettingsManager.getColorStatus());
                break;
            }
            case R.id.seekbar_hue:{
                setShow(R.id.seekbar_hue, progress);
                mPQSettingsManager.setTone(progress - mPQSettingsManager.getToneStatus());
                break;
            }
            /*
            case R.id.seekbar_sharpness:{
                setShow(R.id.seekbar_sharpness, progress);
                mPQSettingsManager.setSharpness(progress - mPQSettingsManager.getSharpnessStatus());
                break;
            }
            */
           default:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    private void setShow(int id, int value) {
        switch (id) {
            case R.id.seekbar_brightness:{
                text_brightness.setText(getShowString(R.string.pq_brightness, value));
                break;
            }
            case R.id.seekbar_contrast:{
                text_contrast.setText(getShowString(R.string.pq_contrast, value));
                break;
            }
            case R.id.seekbar_saturation:{
                text_saturation.setText(getShowString(R.string.pq_saturation, value));
                break;
            }
            case R.id.seekbar_hue:{
                text_hue.setText(getShowString(R.string.pq_hue, value));
                break;
            }
            /*
            case R.id.seekbar_sharpness:{
                text_sharpness.setText(getShowString(R.string.pq_sharpness, value));
                break;
            }
            */
            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + ": " + value + "%";
    }
}
