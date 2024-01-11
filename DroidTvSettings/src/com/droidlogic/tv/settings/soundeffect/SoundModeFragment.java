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
import androidx.preference.TwoStatePreference;
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
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.media.AudioDeviceCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.AudioConfigManager;
import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.app.AudioSystemCmdManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class SoundModeFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "SoundModeFragment";

    private static final String TV_EQ_MODE                                  = "key_tv_sound_mode";
    private static final String TV_TREBLE_BASS_SETTINGS                     = "treble_bass_effect_settings";
    private static final String TV_BALANCE_SETTINGS                         = "balance_effect_settings";
    private static final String TV_VIRTUAL_SURROUND_SETTINGS                = "tv_sound_virtual_surround";
    private static final String KEY_DOLBY_DAP_EFFECT                        = "key_dolby_dap_effect";
    private static final String KEY_DOLBY_DAP_EFFECT_2_4                    = "key_dolby_audio_processing_2_4";
    private static final String AUDIO_ONLY                                  = "tv_sound_audio_only";
    private static final String KEY_DTS_VX                                  = "key_dts_virtualx_settings";
    private static final String KEY_DPE                                     = "key_dpe_audio_effect";
    private static final String KEY_AUDIO_LATENCY                           = "key_audio_latency";
    private static final String KEY_TV_SOUND_AUDIO_DEVICE                   = "key_tv_sound_output_device";
    private static final String KEY_COEXIST_SPDIF_OTHER                     = "key_coexist_spdif_other";
    private static final String KEY_VAD_SWITCH                              = "key_tv_vad_switch";

    /* index value, refer to array_audio_settings_output_dev_entries in xml*/
    public static final int UI_INDEX_DEVICE_OUT_AUTO                        = 0;
    public static final int UI_INDEX_DEVICE_OUT_SPEAKER                     = 1;
    public static final int UI_INDEX_DEVICE_OUT_SPDIF                       = 2;
    public static final int UI_INDEX_DEVICE_OUT_HDMI_ARC                    = 3;
    public static final int UI_INDEX_DEVICE_OUT_HDMI_OUT                    = 4;
    public static final int UI_INDEX_DEVICE_OUT_HEADPHONE                   = 5;
    public static final int UI_INDEX_DEVICE_OUT_USB                         = 6;
    public static final int UI_INDEX_DEVICE_OUT_BLUETOOTH                   = 7;
    public static final int UI_INDEX_DEVICE_OUT_MAX                         = 8;

    private AudioConfigManager mAudioConfigManager;
    private AudioEffectManager mAudioEffectManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private OutputModeManager mOutputModeManager;
    private AudioSystemCmdManager mAudioSystemCmdManager = null;
    private AudioManager mAudioManager;
    private SystemControlManager mSystemControl;
    private ListPreference mAudioOutputDevPref;
    private TwoStatePreference mCoexistSpdifSwitchPref;
    private TwoStatePreference mVadSwitchPref;
    private int mAudioDeviceOutputStrategy = AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO;
    private Context mContext = null;
    private HashSet<AudioDeviceInfo> mAudioOutputDevices = new HashSet<AudioDeviceInfo>();

    private static final int UI_LOAD_TIMEOUT = 50;//100ms
    private static final int LOAD_UI = 0;
    private static final int AUDIO_ONLY_INT = 0;

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_UI:
                    if (!initView()) {
                        myHandler.sendEmptyMessageDelayed(LOAD_UI, UI_LOAD_TIMEOUT);
                    } else {
                        myHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static SoundModeFragment newInstance() {
        return new SoundModeFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAudioEffectManager != null) {
            final ListPreference eqmode = (ListPreference) findPreference(TV_EQ_MODE);
            eqmode.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_HPEQ_UI));
            eqmode.setValueIndex(mAudioEffectManager.getSoundModeStatus());
            final Preference treblebass = (Preference) findPreference(TV_TREBLE_BASS_SETTINGS);
            treblebass.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_TREBLEBASS_UI));
            String treblebass_summary = getShowString(R.string.tv_treble, mAudioEffectManager.getTrebleStatus()) + " " +
                    getShowString(R.string.tv_bass, mAudioEffectManager.getBassStatus());
            treblebass.setSummary(treblebass_summary);
            final Preference balance = (Preference) findPreference(TV_BALANCE_SETTINGS);
            balance.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_BALANCE_UI));
            balance.setSummary(getShowString(R.string.tv_balance_effect, mAudioEffectManager.getBalanceStatus()));
            final ListPreference virtualsurround = (ListPreference) findPreference(TV_VIRTUAL_SURROUND_SETTINGS);
            virtualsurround.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_SURROUND_UI));

            final Preference dts_vx = (Preference) findPreference(KEY_DTS_VX);
            final Preference dpe = (Preference) findPreference(KEY_DPE);
            dpe.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DPE_UI));
            dts_vx.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_X_UI));
            final Preference dap24Pref = (Preference) findPreference(KEY_DOLBY_DAP_EFFECT_2_4);
            dap24Pref.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DAP_2_UI));

            final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
            audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI));

        }
        refreshPref();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
        mAudioManager.registerAudioDeviceCallback(mAudioDeviceCallback, null);
    }
    @Override
    public void onDestroy() {
        mAudioManager.unregisterAudioDeviceCallback(mAudioDeviceCallback);
        super.onDestroy();
    }

    private void init() {
        mAudioConfigManager = AudioConfigManager.getInstance(getActivity());
        mAudioEffectManager = ((TvSettingsActivity)getActivity()).getAudioEffectManager();
        mAudioSystemCmdManager = ((TvSettingsActivity)getActivity()).getAudioSystemCmdManager();
        mSoundParameterSettingManager = ((TvSettingsActivity)getActivity()).getSoundParameterSettingManager();
        mOutputModeManager = OutputModeManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View innerView = super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1) {
            //MainFragment.changeToLiveTvStyle(innerView, getActivity());
        }
        return innerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tv_sound_mode, null);
        myHandler.sendEmptyMessage(LOAD_UI);

        mAudioOutputDevPref = (ListPreference) findPreference(KEY_TV_SOUND_AUDIO_DEVICE);
        mAudioOutputDevPref.setOnPreferenceChangeListener(this);
        mAudioDeviceOutputStrategy = mSystemControl.getPropertyInt(AudioSystemCmdManager.PROP_AUDIO_OUTPUT_STRATEGY, AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO);
        if (mAudioDeviceOutputStrategy < AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO || mAudioDeviceOutputStrategy > AudioSystemCmdManager.OUTPUT_STRATEGY_MANUAL) {
            logDebug(TAG, false, "refreshPref strategy invalid:" + mAudioDeviceOutputStrategy);
            mAudioDeviceOutputStrategy = AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO;
        }

        mCoexistSpdifSwitchPref = (TwoStatePreference) findPreference(KEY_COEXIST_SPDIF_OTHER);
        mCoexistSpdifSwitchPref.setChecked(mSystemControl.getPropertyBoolean("persist.vendor.media.audio.spdif.coexist", true));

        mVadSwitchPref = (TwoStatePreference) findPreference(KEY_VAD_SWITCH);
        mVadSwitchPref.setChecked(mSoundParameterSettingManager.isVadOn());
    }

    private boolean initView() {
        final ListPreference eqmode = (ListPreference) findPreference(TV_EQ_MODE);
        eqmode.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_HPEQ_UI));
        eqmode.setValueIndex(mAudioEffectManager.getSoundModeStatus());
        eqmode.setOnPreferenceChangeListener(this);

        //final Preference dapPref = (Preference) findPreference(KEY_DOLBY_DAP_EFFECT);
        final Preference dap24Pref = (Preference) findPreference(KEY_DOLBY_DAP_EFFECT_2_4);
        dap24Pref.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DAP_2_UI));

        final ListPreference virtualsurround = (ListPreference) findPreference(TV_VIRTUAL_SURROUND_SETTINGS);
        virtualsurround.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_SURROUND_UI));
        virtualsurround.setValueIndex(mAudioEffectManager.getVirtualSurroundStatus());
        virtualsurround.setOnPreferenceChangeListener(this);

        final Preference treblebass = (Preference) findPreference(TV_TREBLE_BASS_SETTINGS);
        treblebass.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_TREBLEBASS_UI));
        String treblebass_summary = getShowString(R.string.tv_treble, mAudioEffectManager.getTrebleStatus()) + " " +
                getShowString(R.string.tv_bass, mAudioEffectManager.getBassStatus());
        treblebass.setSummary(treblebass_summary);

        final Preference balance = (Preference) findPreference(TV_BALANCE_SETTINGS);
        balance.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_BALANCE_UI));
        balance.setSummary(getShowString(R.string.tv_balance_effect, mAudioEffectManager.getBalanceStatus()));

        final Preference audio_only = (Preference) findPreference(AUDIO_ONLY);
        audio_only.setVisible(false); //the function is not finish, temporarily hidden

        final Preference dts_vx = (Preference) findPreference(KEY_DTS_VX);
        final Preference dpe = (Preference) findPreference(KEY_DPE);
        dpe.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DPE_UI));
        dts_vx.setVisible(mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_X_UI));

        final Preference audio_latency = (Preference) findPreference(KEY_AUDIO_LATENCY);
        audio_latency.setVisible(mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI));

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
           String key = preference.getKey();
        if (TextUtils.equals(key, AUDIO_ONLY)) {
            createUiDialog(AUDIO_ONLY_INT);
        } else if (TextUtils.equals(key, KEY_TV_SOUND_AUDIO_DEVICE)) {
            refreshPref();
        } else if (TextUtils.equals(key, KEY_VAD_SWITCH)) {
            mSoundParameterSettingManager.setVadOn(mVadSwitchPref.isChecked());
        } else if (TextUtils.equals(key, KEY_COEXIST_SPDIF_OTHER)) {
            mAudioSystemCmdManager.setCoexistSpdifOther(mCoexistSpdifSwitchPref.isChecked());
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), TV_EQ_MODE)) {
            mAudioEffectManager.setSoundMode(selection);
            if (selection == AudioEffectManager.EQ_SOUND_MODE_CUSTOM) {
                createUiDialog();
            }
        } else if (TextUtils.equals(preference.getKey(), TV_VIRTUAL_SURROUND_SETTINGS)) {
            mAudioEffectManager.setVirtualSurround(selection);
        } else if (TextUtils.equals(preference.getKey(), KEY_TV_SOUND_AUDIO_DEVICE)) {
            byte[] devices = new byte[] {(byte) indexToAudioDev(selection)};
            mAudioSystemCmdManager.setOutputDevices(devices);
            refreshPref();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void createUiDialog () {
        Context context = (Context) (getActivity());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.xml.tv_sound_effect_ui, null);//tv_sound_effect_ui
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsAudioEqSeekBarInited = false;
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
        //mAlertDialog.getWindow().setLayout(150, 320);
        initSoundModeEqBandSeekBar(view);
    }

    private void createUiDialog (int type) {
        Context context = (Context) (getActivity());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.xml.layout_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
        TextView button_cancel = (TextView)view.findViewById(R.id.dialog_cancel);
        TextView dialogtitle = (TextView)view.findViewById(R.id.dialog_title);
        TextView dialogdetails = (TextView)view.findViewById(R.id.dialog_details);
        if (AUDIO_ONLY_INT == type) {
            dialogtitle.setText(getActivity().getResources().getString(R.string.title_tv_sound_audio_only));
            dialogdetails.setText(getActivity().getResources().getString(R.string.msg_tv_sound_audio_only));
        }
        button_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });
        button_cancel.requestFocus();
        TextView button_ok = (TextView)view.findViewById(R.id.dialog_ok);
        button_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AUDIO_ONLY_INT == type) {
                    SystemProperties.set("persist.audio.only.state", "true");
                }
                mAlertDialog.dismiss();
            }
        });
    }

    private boolean mIsAudioEqSeekBarInited = false;
    private SeekBar mBand1Seekbar;
    private TextView mBand1Text;
    private SeekBar mBand2Seekbar;
    private TextView mBand2Text;
    private SeekBar mBand3Seekbar;
    private TextView mBand3Text;
    private SeekBar mBand4Seekbar;
    private TextView mBand4Text;
    private SeekBar mBand5Seekbar;
    private TextView mBand5Text;
    private SeekBar mBand6Seekbar;
    private TextView mBand6Text;
    private SeekBar mBand7Seekbar;
    private TextView mBand7Text;
    private SeekBar mBand8Seekbar;
    private TextView mBand8Text;
    private SeekBar mBand9Seekbar;
    private TextView mBand9Text;

    private void initSoundModeEqBandSeekBar(View view) {
        if (mAudioEffectManager == null) {
            mAudioEffectManager = ((TvSettingsActivity)getActivity()).getAudioEffectManager();
        }
        int hpeq_band_num = mAudioEffectManager.getHpeqBandNum(AudioEffectManager.DEBUG_HPEQ_BAND_NUM_UI);

        int status = -1;
        mBand1Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band1);
        mBand1Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band1);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND1);
        mBand1Seekbar.setOnSeekBarChangeListener(this);
        mBand1Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND1, status);
        mBand1Seekbar.requestFocus();
        mBand2Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band2);
        mBand2Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band2);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND2);
        mBand2Seekbar.setOnSeekBarChangeListener(this);
        mBand2Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND2, status);
        mBand3Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band3);
        mBand3Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band3);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND3);
        mBand3Seekbar.setOnSeekBarChangeListener(this);
        mBand3Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND3, status);
        mBand4Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band4);
        mBand4Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band4);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND4);
        mBand4Seekbar.setOnSeekBarChangeListener(this);
        mBand4Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND4, status);
        mBand5Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band5);
        mBand5Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band5);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND5);
        mBand5Seekbar.setOnSeekBarChangeListener(this);
        mBand5Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND5, status);

        mBand6Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band6);
        mBand6Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band6);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND6);
        mBand6Seekbar.setOnSeekBarChangeListener(this);
        mBand6Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND6, status);

        mBand7Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band7);
        mBand7Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band7);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND7);
        mBand7Seekbar.setOnSeekBarChangeListener(this);
        mBand7Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND7, status);

        mBand8Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band8);
        mBand8Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band8);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND8);
        mBand8Seekbar.setOnSeekBarChangeListener(this);
        mBand8Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND8, status);

        mBand9Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band9);
        mBand9Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band9);
        status = mAudioEffectManager.getUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND9);
        mBand9Seekbar.setOnSeekBarChangeListener(this);
        mBand9Seekbar.setProgress(status);
        setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND9, status);

        if (hpeq_band_num == 5 || hpeq_band_num == 7) {
            mBand8Seekbar.setVisibility(View.GONE);
            mBand8Text.setVisibility(View.GONE);
            mBand9Seekbar.setVisibility(View.GONE);
            mBand9Text.setVisibility(View.GONE);
        }

        if (hpeq_band_num == 5) {
            mBand6Seekbar.setVisibility(View.GONE);
            mBand6Text.setVisibility(View.GONE);
            mBand7Seekbar.setVisibility(View.GONE);
            mBand7Text.setVisibility(View.GONE);
        }

        mIsAudioEqSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!mIsAudioEqSeekBarInited) {
            return;
        }

        int hpeq_band_num = mAudioEffectManager.getHpeqBandNum(AudioEffectManager.DEBUG_HPEQ_BAND_NUM_UI);
        switch (seekBar.getId()) {
            case R.id.seekbar_tv_audio_effect_band1:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND1, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND1, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band2:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND2, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND2, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band3:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND3, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND3, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band4:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND4, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND4, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band5:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND5, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND5, progress, hpeq_band_num);
                break;
            }

            case R.id.seekbar_tv_audio_effect_band6:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND6, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND6, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band7:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND7, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND7, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band8:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND8, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND8, progress, hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band9:{
                setShow(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND9, progress);
                mAudioEffectManager.setUserSoundModeParam(AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND9, progress, hpeq_band_num);
                break;
            }

            default:
                logDebug(TAG, false, "onProgressChanged unsupported seekbar id:" + seekBar.getId());
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
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND1:{
                mBand1Text.setText(getShowString(R.string.tv_audio_effect_band1, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND2:{
                mBand2Text.setText(getShowString(R.string.tv_audio_effect_band2, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND3:{
                mBand3Text.setText(getShowString(R.string.tv_audio_effect_band3, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND4:{
                mBand4Text.setText(getShowString(R.string.tv_audio_effect_band4, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND5:{
                mBand5Text.setText(getShowString(R.string.tv_audio_effect_band5, value));
                break;
            }

            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND6:{
                mBand6Text.setText(getShowString(R.string.tv_audio_effect_band6, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND7:{
                mBand7Text.setText(getShowString(R.string.tv_audio_effect_band7, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND8:{
                mBand8Text.setText(getShowString(R.string.tv_audio_effect_band8, value));
                break;
            }
            case AudioEffectManager.EQ_SOUND_MODE_EFFECT_BAND9:{
                mBand9Text.setText(getShowString(R.string.tv_audio_effect_band9, value));
                break;
            }

            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    private AudioDeviceCallback mAudioDeviceCallback = new AudioDeviceCallback() {
        @Override
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            refreshPref();
        }

        @Override
        public void onAudioDevicesRemoved(AudioDeviceInfo[] devices) {
            refreshPref();
        }

    };

    @Override
    public void onAttach(Context context) {
        mContext = getActivity();
        mSystemControl = SystemControlManager.getInstance();
        mAudioSystemCmdManager = AudioSystemCmdManager.getInstance(mContext);
        super.onAttach(context);
    }

    private void refreshPref() {
        String[] entry = getArrayString(R.array.tv_sound_output_device_entries);
        String[] entryValue = getArrayString(R.array.tv_sound_output_device_entry_values);
        List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
        List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));

        for (int i = 0; i < UI_INDEX_DEVICE_OUT_MAX; i++ ) {
            if (!isConnectedDev(i)) {
                entryList.remove(getActivity().getResources().getString(indexToStringIndex(i)));
                entryValueList.remove(i + "");
            }
        }
        mAudioOutputDevPref.setEntries(entryList.toArray(new String[]{}));
        mAudioOutputDevPref.setEntryValues(entryValueList.toArray(new String[]{}));

        mAudioDeviceOutputStrategy = mSystemControl.getPropertyInt(AudioSystemCmdManager.PROP_AUDIO_OUTPUT_STRATEGY, AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO);
        int uiIndex = convertDevicesToUiDisplay(mAudioSystemCmdManager.getOutputDevices());
        int prefIndex = uiIndex;
        if (mAudioDeviceOutputStrategy == AudioSystemCmdManager.OUTPUT_STRATEGY_AUTO) {
            prefIndex = UI_INDEX_DEVICE_OUT_AUTO;
        }
        mAudioOutputDevPref.setValue(prefIndex + "");

        String strategy = AudioSystemCmdManager.strategyToString(mAudioDeviceOutputStrategy);
        mAudioOutputDevPref.setSummary(getActivity().getResources().getString(indexToStringIndex(uiIndex)) + " (" + strategy +  ")");
        mAudioOutputDevPref.setEnabled(true);
    }

    private int convertDevicesToUiDisplay(byte[] devices) {
        if (devices == null || devices.length == 0) {
            logDebug(TAG, false, "convertDevicesToUiDisplay devices is null");
            return UI_INDEX_DEVICE_OUT_SPEAKER;
        }
        if (devices.length == 1) {
            return audioDevToIndex(devices[0]);
        } else if (devices.length == 2) {
            logDebug(TAG, false, "convertDevicesToUiDisplay not supported dev0:" + devices[0] + ", dev1:" + devices[1]);
            if (devices[0] == AudioDeviceInfo.TYPE_LINE_DIGITAL) {
                return audioDevToIndex(devices[1]);
            } else {
                return audioDevToIndex(devices[0]);
            }
        }
        logDebug(TAG, false, "convertDevicesToUiDisplay not supported device length:" + devices.length);
        return UI_INDEX_DEVICE_OUT_SPEAKER;
    }

    private int indexToStringIndex(int index) {
        switch (index) {
            case UI_INDEX_DEVICE_OUT_AUTO:
                return R.string.title_tv_sound_output_device_auto;
            case UI_INDEX_DEVICE_OUT_SPEAKER:
                return R.string.title_tv_sound_output_device_speaker;
            case UI_INDEX_DEVICE_OUT_SPDIF:
                return R.string.title_tv_sound_output_device_spdif;
            case UI_INDEX_DEVICE_OUT_HDMI_OUT:
                return R.string.title_tv_sound_output_device_hdmi_out;
            case UI_INDEX_DEVICE_OUT_HEADPHONE:
                return R.string.title_tv_sound_output_device_headphone;
            case UI_INDEX_DEVICE_OUT_HDMI_ARC:
                return R.string.title_tv_sound_output_device_hdmi_arc;
            case UI_INDEX_DEVICE_OUT_USB:
                return R.string.title_tv_sound_output_device_usb;
            case UI_INDEX_DEVICE_OUT_BLUETOOTH:
                return R.string.title_tv_sound_output_device_bluetooth;
            default:
                logDebug(TAG, false, "audioDevToIndex not supported device:" + index);
                return 0;
        }
    }

    private int audioDevToIndex(int device) {
        switch (device) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                return UI_INDEX_DEVICE_OUT_SPEAKER;
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return UI_INDEX_DEVICE_OUT_HEADPHONE;
            case AudioDeviceInfo.TYPE_LINE_DIGITAL:
                return UI_INDEX_DEVICE_OUT_SPDIF;
            case AudioDeviceInfo.TYPE_HDMI:
                return UI_INDEX_DEVICE_OUT_HDMI_OUT;
            case AudioDeviceInfo.TYPE_HDMI_ARC:
                return UI_INDEX_DEVICE_OUT_HDMI_ARC;
            case AudioDeviceInfo.TYPE_USB_DEVICE:
            case AudioDeviceInfo.TYPE_USB_ACCESSORY:
            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return UI_INDEX_DEVICE_OUT_USB;
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return UI_INDEX_DEVICE_OUT_BLUETOOTH;
            default:
                logDebug(TAG, false, "audioDevToIndex not supported AudioDeviceInfo device:" + device);
                return UI_INDEX_DEVICE_OUT_SPEAKER;
        }
    }

    private int indexToAudioDev(int index) {
        switch (index) {
            case UI_INDEX_DEVICE_OUT_AUTO:
                return AudioDeviceInfo.TYPE_UNKNOWN;
            case UI_INDEX_DEVICE_OUT_SPEAKER:
                return AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
            case UI_INDEX_DEVICE_OUT_SPDIF:
                return AudioDeviceInfo.TYPE_LINE_DIGITAL;
            case UI_INDEX_DEVICE_OUT_HDMI_OUT:
                return AudioDeviceInfo.TYPE_HDMI;
            case UI_INDEX_DEVICE_OUT_HEADPHONE:
                return AudioDeviceInfo.TYPE_WIRED_HEADPHONES;
            case UI_INDEX_DEVICE_OUT_HDMI_ARC:
                return AudioDeviceInfo.TYPE_HDMI_ARC;
            case UI_INDEX_DEVICE_OUT_USB:
                return AudioDeviceInfo.TYPE_USB_DEVICE;
            case UI_INDEX_DEVICE_OUT_BLUETOOTH:
                return AudioDeviceInfo.TYPE_BLUETOOTH_A2DP;
            default:
                logDebug(TAG, false, "indexToAudioDev not supported ui index:" + index);
                return 0;
        }
    }

    private boolean isAllowSetDevice() {
        AudioDeviceInfo[] outputDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        if (mAudioDeviceOutputStrategy != AudioSystemCmdManager.OUTPUT_STRATEGY_MANUAL) {
            for (AudioDeviceInfo info : outputDevices) {
                if (info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        info.getType() == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
                        info.getType() == AudioDeviceInfo.TYPE_USB_DEVICE ||
                        info.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isConnectedDev(int index) {
        if (index == UI_INDEX_DEVICE_OUT_SPDIF) {
            AudioDeviceInfo[] outputDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo info : outputDevices) {
                if (info.isSink() && info.getType() == AudioDeviceInfo.TYPE_LINE_DIGITAL) {
                    return true;
                }
            }
            return false;
        } else if (index == UI_INDEX_DEVICE_OUT_HDMI_OUT) {
            return !DroidLogicUtils.isTv();
        } else if (index == UI_INDEX_DEVICE_OUT_HDMI_ARC || index == UI_INDEX_DEVICE_OUT_HEADPHONE){
            return DroidLogicUtils.isTv();
        } else {
            return true;
        }
//        // reserve auto ui code.
//        if (mAudioDeviceOutputStrategy == AudioSystemCmdManager.OUTPUT_STRATEGY_SEMI_AUTO) {
//            if (index == UI_INDEX_DEVICE_OUT_SPEAKER  || index == UI_INDEX_DEVICE_OUT_HDMI_ARC || index == UI_INDEX_DEVICE_OUT_SPDIF) {
//                // Semi-Auto need display SPK/ARC/SPDIF
//                return true;
//            }
//        } else if (mAudioDeviceOutputStrategy == AudioSystemCmdManager.OUTPUT_STRATEGY_MANUAL) {
//            return true;
//        } else {
//            return true;
//        }
//        AudioDeviceInfo[] outputDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
//        for (AudioDeviceInfo info : outputDevices) {
//            if (info.isSink()) {
//                if (index == UI_INDEX_DEVICE_OUT_HEADPHONE) {
//                    if (info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
//                            info.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
//                        return true;
//                    }
//                } else if (index == UI_INDEX_DEVICE_OUT_USB) {
//                    if (info.getType() == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
//                            info.getType() == AudioDeviceInfo.TYPE_USB_DEVICE ||
//                            info.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
//                        return true;
//                    }
//                } else if (info.getType() == indexToAudioDev(index)) {
//                    return true;
//                }
//            }
//        }
//        return false;
    }
}
