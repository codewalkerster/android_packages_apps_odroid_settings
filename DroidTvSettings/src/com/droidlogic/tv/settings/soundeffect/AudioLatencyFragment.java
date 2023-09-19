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

package com.droidlogic.tv.settings.soundeffect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.AudioConfigManager;
import com.droidlogic.app.SystemControlManager;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;
import com.droidlogic.tv.settings.SoundFragment;
import com.droidlogic.tv.settings.SettingsConstant;
import androidx.preference.SeekBarPreference;


public class AudioLatencyFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "AudioLatencyFragment";

    private AudioConfigManager mAudioConfigManager;
    private SystemControlManager mSystemControlManager;
    private static final String KEY_TV_SOUND_AUDIO_SOURCE_SELECT    = "key_tv_sound_audio_source_select";
    private static final String KEY_HDMI_AUDIO_LATENCY              = "box_hdmi_audio_latency";
    public static final String  AUDIO_LATENCY                       = "vendor.media.dtv.passthrough.latencyms";

    private static int mCurrentSettingSourceId = AudioConfigManager.AUDIO_OUTPUT_DELAY_SOURCE_ATV;
    private ListPreference mTvSourceSelectPref;
    private SeekBar mSeekBarAudioOutputDelaySpeaker;
    private SeekBar mSeekBarAudioOutputDelaySpdif;
    private SeekBar mSeekBarAudioOutputDelayHeadphone;
    private TextView mTextAudioOutputDelaySpeaker;
    private TextView mTextAudioOutputDelaySpdif;
    private TextView mTextAudioOutputDelayHeadphone;
    private boolean mIsDelayAndPrescaleSeekBarInited = false;

    public static AudioLatencyFragment newInstance() {
        return new AudioLatencyFragment();
    }

    private boolean CanDebug() {
        return OptionParameterManager.CanDebug();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTvSourceSelectPref.setValueIndex(mCurrentSettingSourceId);
        final Preference hdmiAudioLatency = (Preference) findPreference(KEY_HDMI_AUDIO_LATENCY);
        hdmiAudioLatency.setSummary(getHdmiAudioLatency() + "ms");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mAudioConfigManager = AudioConfigManager.getInstance(getActivity());
        mSystemControlManager = SystemControlManager.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View innerView = super.onCreateView(inflater, container, savedInstanceState);
        return innerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.audio_latency, null);

        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
                    && (SystemProperties.getBoolean("vendor.tv.soc.as.mbox", false) == false);

        mTvSourceSelectPref = (ListPreference) findPreference(KEY_TV_SOUND_AUDIO_SOURCE_SELECT);
        mTvSourceSelectPref.setValueIndex(mCurrentSettingSourceId);
        mTvSourceSelectPref.setOnPreferenceChangeListener(this);
        mTvSourceSelectPref.setEnabled(true);

        final SeekBarPreference audioOutputLatencyPref = (SeekBarPreference) findPreference(SoundFragment.KEY_AUDIO_OUTPUT_LATENCY);
        audioOutputLatencyPref.setOnPreferenceChangeListener(this);
        audioOutputLatencyPref.setMax(AudioConfigManager.HAL_AUDIO_OUT_DEV_DELAY_MAX);
        audioOutputLatencyPref.setMin(AudioConfigManager.HAL_AUDIO_OUT_DEV_DELAY_MIN);
        audioOutputLatencyPref.setSeekBarIncrement(SoundFragment.KEY_AUDIO_OUTPUT_LATENCY_STEP);
        audioOutputLatencyPref.setValue(mAudioConfigManager.getAudioOutputAllDelay());

        audioOutputLatencyPref.setVisible(tvFlag);

        final Preference hdmiAudioLatency = (Preference) findPreference(KEY_HDMI_AUDIO_LATENCY);
        if (tvFlag) {
            hdmiAudioLatency.setTitle(getActivity().getResources().getString(R.string.arc_hdmi_audio_latency));
        }
        hdmiAudioLatency.setSummary(getHdmiAudioLatency() + "ms");
    }

    private boolean initView() {
        if (!DroidLogicUtils.isTv()) {
            mTvSourceSelectPref.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_TV_SOUND_AUDIO_SOURCE_SELECT)) {
            final int selection = Integer.parseInt((String)newValue);
            mCurrentSettingSourceId = selection;
            createOutputDelayAndPrescaleUiDialog();
        } else if (TextUtils.equals(preference.getKey(), SoundFragment.KEY_AUDIO_OUTPUT_LATENCY)) {
            mAudioConfigManager.setAudioOutputAllDelay((int)newValue);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void createOutputDelayAndPrescaleUiDialog() {
        Context context = (Context) (getActivity());
        LayoutInflater inflater = context.getSystemService(LayoutInflater.class);
        View view = inflater.inflate(R.xml.tv_sound_audio_settings_seekbar, null);//tv_sound_audio_settings_seekbar.xml
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsDelayAndPrescaleSeekBarInited = false;
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
        initOutputDelayAndPrescaleSeekBar(view);
    }

    private void initOutputDelayAndPrescaleSeekBar(View view) {
        int delayMs = 0;

        mSeekBarAudioOutputDelaySpeaker = (SeekBar) view.findViewById(R.id.id_seek_bar_audio_delay_speaker);
        mTextAudioOutputDelaySpeaker = (TextView) view.findViewById(R.id.id_text_view_audio_delay_speaker);
        delayMs = mAudioConfigManager.getAudioOutputSpeakerDelay(mCurrentSettingSourceId);
        mSeekBarAudioOutputDelaySpeaker.setOnSeekBarChangeListener(this);
        mSeekBarAudioOutputDelaySpeaker.setProgress(delayMs);
        setShow(R.id.id_seek_bar_audio_delay_speaker, delayMs);
        mSeekBarAudioOutputDelaySpeaker.requestFocus();

        mSeekBarAudioOutputDelaySpdif = (SeekBar) view.findViewById(R.id.id_seek_bar_audio_delay_spdif);
        mTextAudioOutputDelaySpdif = (TextView) view.findViewById(R.id.id_text_view_audio_delay_spdif);
        delayMs = mAudioConfigManager.getAudioOutputSpdifDelay(mCurrentSettingSourceId);
        mSeekBarAudioOutputDelaySpdif.setOnSeekBarChangeListener(this);
        mSeekBarAudioOutputDelaySpdif.setProgress(delayMs);
        setShow(R.id.id_seek_bar_audio_delay_spdif, delayMs);

        mSeekBarAudioOutputDelayHeadphone = (SeekBar) view.findViewById(R.id.id_seek_bar_audio_delay_headphone);
        mTextAudioOutputDelayHeadphone = (TextView) view.findViewById(R.id.id_text_view_audio_delay_headphone);
        delayMs = mAudioConfigManager.getAudioOutputHeadphoneDelay(mCurrentSettingSourceId);
        mSeekBarAudioOutputDelayHeadphone.setOnSeekBarChangeListener(this);
        mSeekBarAudioOutputDelayHeadphone.setProgress(delayMs);
        setShow(R.id.id_seek_bar_audio_delay_headphone, delayMs);

        mIsDelayAndPrescaleSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!mIsDelayAndPrescaleSeekBarInited) {
            return;
        }

        switch (seekBar.getId()) {
            case R.id.id_seek_bar_audio_delay_speaker:{
                setShow(R.id.id_seek_bar_audio_delay_speaker, progress);
                mAudioConfigManager.setAudioOutputSpeakerDelay(mCurrentSettingSourceId, progress);
                setDelayEnabled();
                break;
            }
            case R.id.id_seek_bar_audio_delay_spdif:{
                setShow(R.id.id_seek_bar_audio_delay_spdif, progress);
                mAudioConfigManager.setAudioOutputSpdifDelay(mCurrentSettingSourceId, progress);
                setDelayEnabled();
                break;
            }
            case R.id.id_seek_bar_audio_delay_headphone:{
                setShow(R.id.id_seek_bar_audio_delay_headphone, progress);
                mAudioConfigManager.setAudioOutputHeadphoneDelay(mCurrentSettingSourceId, progress);
                setDelayEnabled();
                break;
            }
            default:
                Log.w(TAG, "onProgressChanged unsupported seekbar id:" + seekBar.getId());
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
            case R.id.id_seek_bar_audio_delay_speaker:{
                mTextAudioOutputDelaySpeaker.setText(getAudioDelayShowString(R.string.title_tv_audio_delay_speaker, value));
                break;
            }
            case R.id.id_seek_bar_audio_delay_spdif:{
                mTextAudioOutputDelaySpdif.setText(getAudioDelayShowString(R.string.title_tv_audio_delay_spdif, value));
                break;
            }
            case R.id.id_seek_bar_audio_delay_headphone:{
                mTextAudioOutputDelayHeadphone.setText(getAudioDelayShowString(R.string.title_tv_audio_delay_headphone, value));
                break;
            }
            default:
                break;
        }
    }

    private String getAudioDelayShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + ": " + value + " ms";
    }

    private void setDelayEnabled () {
        SystemControlManager.getInstance().setProperty(AudioConfigManager.PROP_AUDIO_DELAY_ENABLED, "true");
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    public int getHdmiAudioLatency() {
        int result = mSystemControlManager.getPropertyInt(AUDIO_LATENCY, 0);
        Log.d(TAG, "getHdmiAudioLatency = " + result);
        return result;
    }
}
