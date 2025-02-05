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

package hardkernel.odroid.settings.soundeffect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
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
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.media.AudioDeviceCallback;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.app.DroidLogicUtils;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.app.SystemControlManager;

import hardkernel.odroid.settings.TvSettingsActivity;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.tvoption.SoundParameterSettingManager;
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;
import hardkernel.odroid.settings.SettingsConstant;

public class SoundModeFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SoundModeFragment";
    private static final String TV_AUTO_SOUND_MODE                          = "key_auto_sound_mode_switch";
    private static final String TV_SOUND_MODE_STYLE                         = "key_tv_sound_mode";
    private static final String TV_SOUND_MODE_PROCESSING                    = "key_sound_processing";
    private static final String KEY_ENGINEER_MODE                           = "key_audio_engineer_mode";
    private static final String KEY_ADVANCE_SOUND                           = "advanced_sound_settings";
    private static final String KEY_AUDIO_DEVICES_ROUTING                   = "audio_devices_routing";

    private AudioEffectManager mAudioEffectManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private DroidAudioManager mDroidAudioManager = null;
    private AudioManager mAudioManager;
    private SystemControlManager mSystemControl;
    private Context mContext = null;

    private Preference mAdvanced_sound_settings_pref;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    public static final int DIALOG_UI_RESET                                 = 0xF1;

    public static SoundModeFragment newInstance() {
        return new SoundModeFragment();
    }

    private static final int MESSAGE_AUDIO_SETTING_RESET                    = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_AUDIO_SETTING_RESET:
                mSystemControl.setProperty(DroidAudioManager.PROP_AUDIO_OUTPUT_STRATEGY, DroidAudioManager.OUTPUT_STRATEGY_AUTO + "");
                mDroidAudioManager.reset();
                mAudioEffectManager.reset();
                int soundMode = mAudioEffectManager.getSoundModeStatus();
                final ListPreference soundModePref = (ListPreference) findPreference(TV_SOUND_MODE_STYLE);
                soundModePref.setValue(soundMode + "");
                Log.d(TAG, "-Reset Audio Setting done!");
                break;
            default:
                Log.d(TAG, "Unknown message:" + msg.what);
                break;
            }
        }
    };

    @Override
    public void onResume() {
        /*If the user adjusts the customized parameters, update the current sound mode UI
          If all effect of then switch sound to STANDARD
        */
        int mode = mAudioEffectManager.getBasicEffectMode();
        boolean isBasicProcessingOff = (mode == AudioEffectManager.BASIC_EFFECT_MODE_OFF ? true : false);
        mode = mAudioEffectManager.getDualEffectMode();
        boolean isDualEffectOff = (mode == AudioEffectManager.EFFECT_MODE_OFF ? true : false);
        final ListPreference soundModePref = (ListPreference) findPreference(TV_SOUND_MODE_STYLE);
        int soundMode = mAudioEffectManager.getSoundModeStatus();
        if (isBasicProcessingOff && isDualEffectOff && (soundMode != AudioEffectManager.COMMON_SOUND_MODE_STANDARD)) {
            mAudioEffectManager.setSoundMode(AudioEffectManager.COMMON_SOUND_MODE_STANDARD);
            soundMode = AudioEffectManager.COMMON_SOUND_MODE_STANDARD;
        }
        soundModePref.setValue(soundMode + "");

        mSharedPreferences = getActivity().getSharedPreferences("menu_time_count", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putInt("isCountStop", 0);
        mEditor.commit();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getActivity();
        mSystemControl = SystemControlManager.getInstance();
        mDroidAudioManager = DroidAudioManager.getInstance(getActivity());
        mAudioEffectManager = ((TvSettingsActivity)getActivity()).getAudioEffectManager();
        mSoundParameterSettingManager = ((TvSettingsActivity)getActivity()).getSoundParameterSettingManager();
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        //if all effect is off, then hide sound mode and sound processing UI
        final TwoStatePreference autoSoundModePref = (TwoStatePreference) findPreference(TV_AUTO_SOUND_MODE);
        final ListPreference soundModePref = (ListPreference) findPreference(TV_SOUND_MODE_STYLE);
        soundModePref.setValueIndex(mAudioEffectManager.getSoundModeStatus());
        soundModePref.setOnPreferenceChangeListener(this);
        final Preference soundProcessingPref = (Preference)findPreference(TV_SOUND_MODE_PROCESSING);

        if (getOnAudioEffectsCount() == 0) {
            soundModePref.setVisible(false);
            soundProcessingPref.setVisible(false);
            Log.d(TAG, "onCreatePreferences() All Effects is off, hide Sound Mode and Sound Processing!");
        } else if (SettingsConstant.isSoundbarFeature() && !mDroidAudioManager.isSoundBarModeEnabled()) {
            //hide "Sound Mode" & "Sound Processing" when SoundBar Mode is disable
            soundModePref.setVisible(false);
            soundProcessingPref.setVisible(false);
            Log.d(TAG, "onCreatePreferences() hide [Sound Mode] if SoundBarMode is disable");
        }

        //TBD: AI AQ control, engineer mode UI, currently hide it's UI
        autoSoundModePref.setVisible(false);
        final Preference engineerPref = (Preference) findPreference(KEY_ENGINEER_MODE);
        engineerPref.setVisible(false);

        mAdvanced_sound_settings_pref = (Preference)findPreference(KEY_ADVANCE_SOUND);

        Preference resetPref = findPreference("key_audio_reset");
        if (resetPref != null) {
            resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createUiDialog(DIALOG_UI_RESET);
                    return true;
                }
            });
        }

        // if SoundBarModeEnabled is true,hide some UI for SoundBarMode
        if (SettingsConstant.isSoundbarFeature() && mDroidAudioManager.isSoundBarModeEnabled()) {
            logDebug(TAG, true, "SoundBarMode is true, hide Audio Output Device for SoundBarMode");
            final Preference audioOutputDevPref = (Preference)findPreference(KEY_AUDIO_DEVICES_ROUTING);
            audioOutputDevPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
           String key = preference.getKey();
        if (TextUtils.equals(key, TV_AUTO_SOUND_MODE)) {
            final TwoStatePreference autoSoundModePref = (TwoStatePreference) findPreference(TV_AUTO_SOUND_MODE);
            final ListPreference soundModePref = (ListPreference) findPreference(TV_SOUND_MODE_STYLE);
            final Preference soundProcessingPref = (Preference)findPreference(TV_SOUND_MODE_PROCESSING);
            if (autoSoundModePref.isChecked()) {
                soundModePref.setVisible(false);
                soundProcessingPref.setVisible(false);
            } else {
                soundModePref.setVisible(true);
                soundProcessingPref.setVisible(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), TV_SOUND_MODE_STYLE)) {
            mAudioEffectManager.setSoundMode(selection);
        }
        return true;
    }

    private int getOnAudioEffectsCount() {
        int count = 0;
        for (int id = AudioEffectManager.EFFECT_HPEQ_UI_ID; id <= AudioEffectManager.EFFECT_DAP2_UI_ID; id++) {
            if (mAudioEffectManager.isAudioEffectOn(id)) {
                count++;
            }
        }
        return count;
    }

    private void createUiDialog(int type) {
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
        if (DIALOG_UI_RESET == type) {
            dialogtitle.setText("Reset");
            dialogdetails.setText("Are you sure you want to reset all audio settings?");
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
                if (DIALOG_UI_RESET == type) {
                    mHandler.sendEmptyMessage(MESSAGE_AUDIO_SETTING_RESET);
                    Log.d(TAG, "+Reset Audio Setting Start");
                }
                mAlertDialog.dismiss();
            }
        });
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }
}
