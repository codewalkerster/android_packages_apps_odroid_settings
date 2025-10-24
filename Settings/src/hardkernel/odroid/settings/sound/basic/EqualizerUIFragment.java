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
 import hardkernel.odroid.settings.SettingsPreferenceFragment;
 import androidx.preference.Preference;
 import androidx.preference.PreferenceCategory;
 import android.util.Log;
 import android.text.TextUtils;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;

 import hardkernel.odroid.settings.TvSettingsActivity;
 import hardkernel.odroid.settings.R;
 import com.droidlogic.app.DroidAudioEffect;

 public class EqualizerUIFragment extends SettingsPreferenceFragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "EqualizerUIFragment";
    private static final String[] band_5_Range_Title = {"120Hz", "500Hz", " 1.5KHz", "  5KHz", " 10KHz"};
    private static final String[] band_7_Range_Title = {"100Hz", "250Hz", "600Hz", "  1KHz", " 2.5KHz", "  6KHz", " 10KHz"};
    private static final String[] band_9_Range_Title = {"100Hz", "250Hz", "500Hz", "800Hz", "  1KHz", " 2.5KHz", "  5KHz", "  8KHz", " 10KHz"};

    private boolean mIsAudioEqSeekBarInit = false;
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

    private int mSelectedBandNum = 0;

    private DroidAudioEffect mDroidAudioEffect;

    public static EqualizerUIFragment newInstance() {
         return new EqualizerUIFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mDroidAudioEffect == null) {
            mDroidAudioEffect = DroidAudioEffect.getInstance(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.tv_sound_equalizer, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void initSeekBar(View view) {
       int status = -1;
       boolean hasfocused = false;
       mSelectedBandNum = mDroidAudioEffect.getHpeqBandNum();

       int value = -1;
       int unMapVal = 0;
       mBand1Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band1);
       mBand1Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band1);
       value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND1);
       unMapVal = unMappingLine(value, false);
       mBand1Seekbar.setOnSeekBarChangeListener(this);
       mBand1Seekbar.setProgress(unMapVal);
       setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND1, unMapVal);
       mBand1Seekbar.requestFocus();

       mBand2Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band2);
       mBand2Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band2);
       value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND2);
       unMapVal = unMappingLine(value, false);
       mBand2Seekbar.setOnSeekBarChangeListener(this);
       mBand2Seekbar.setProgress(unMapVal);
       setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND2, unMapVal);

       mBand3Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band3);
       mBand3Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band3);
       value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND3);
       unMapVal = unMappingLine(value, false);
       mBand3Seekbar.setOnSeekBarChangeListener(this);
       mBand3Seekbar.setProgress(unMapVal);
       setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND3, unMapVal);

       mBand4Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band4);
       mBand4Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band4);
       value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND4);
       unMapVal = unMappingLine(value, false);
       mBand4Seekbar.setOnSeekBarChangeListener(this);
       mBand4Seekbar.setProgress(unMapVal);
       setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND4, unMapVal);

       mBand5Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band5);
       mBand5Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band5);
       value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND5);
       unMapVal = unMappingLine(value, false);
       mBand5Seekbar.setOnSeekBarChangeListener(this);
       mBand5Seekbar.setProgress(unMapVal);
       setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND5, unMapVal);

       if (mSelectedBandNum > 5) {
            mBand6Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band6);
            mBand6Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band6);
            value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND6);
            unMapVal = unMappingLine(value, false);
            mBand6Seekbar.setOnSeekBarChangeListener(this);
            mBand6Seekbar.setProgress(unMapVal);
            setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND6, unMapVal);

            mBand7Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band7);
            mBand7Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band7);
            value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND7);
            unMapVal = unMappingLine(value, false);
            mBand7Seekbar.setOnSeekBarChangeListener(this);
            mBand7Seekbar.setProgress(unMapVal);
            setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND7, unMapVal);
      }

      if (mSelectedBandNum > 7) {
            mBand8Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band8);
            mBand8Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band8);
            value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND8);
            unMapVal = unMappingLine(value, false);
            mBand8Seekbar.setOnSeekBarChangeListener(this);
            mBand8Seekbar.setProgress(unMappingLine(value, false));
            setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND8, unMapVal);

            mBand9Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band9);
            mBand9Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band9);
            value = mDroidAudioEffect.getUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND9);
            unMapVal = unMappingLine(value, false);
            mBand9Seekbar.setOnSeekBarChangeListener(this);
            mBand9Seekbar.setProgress(unMapVal);
            setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND9, unMapVal);
      }

       if (mSelectedBandNum == 5 || mSelectedBandNum == 7) {
           mBand8Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band8);
           mBand8Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band8);
           mBand8Seekbar.setVisibility(View.GONE);
           mBand8Text.setVisibility(View.GONE);
           mBand9Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band9);
           mBand9Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band9);
           mBand9Seekbar.setVisibility(View.GONE);
           mBand9Text.setVisibility(View.GONE);
       }

       if (mSelectedBandNum == 5) {
           mBand6Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band6);
           mBand6Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band6);
           mBand6Seekbar.setVisibility(View.GONE);
           mBand6Text.setVisibility(View.GONE);
           mBand7Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band7);
           mBand7Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band7);
           mBand7Seekbar.setVisibility(View.GONE);
           mBand7Text.setVisibility(View.GONE);
       }

       mIsAudioEqSeekBarInit = true;
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!mIsAudioEqSeekBarInit) {
            return;
        }
        boolean isNeedRefresh = true;
        int hpeq_band_num = mDroidAudioEffect.getHpeqBandNum();
        switch (seekBar.getId()) {
            case R.id.seekbar_tv_audio_effect_band1:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND1, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND1, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band2:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND2, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND2, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band3:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND3, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND3, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band4:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND4, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND4, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band5:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND5, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND5, MappingLine(progress, false), hpeq_band_num);
                break;
            }

            case R.id.seekbar_tv_audio_effect_band6:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND6, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND6, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band7:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND7, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND7, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band8:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND8, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND8, MappingLine(progress, false), hpeq_band_num);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band9:{
                setShow(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND9, progress);
                mDroidAudioEffect.setUserSoundModeParam(DroidAudioEffect.HPEQ_MODE_EFFECT_BAND9, MappingLine(progress, false), hpeq_band_num);
                break;
            }

            default:
               break;
        }
    }

    //convert -10~10 to 0~100 controlled by need or not
    private int unMappingLine(int mapval, boolean need) {
        if (!need) {
            return mapval;
        }

        final int MIN_UI_VAL = -10;
        final int MAX_UI_VAL = 10;
        final int MIN_VAL = -10; //UI MIN
        final int MAX_VAL = 10;  //UI Max
        if (mapval > MAX_UI_VAL || mapval < MIN_UI_VAL) {
            Log.d(TAG, "unMappingLine: map value:" + mapval + " invalid. set default value:" + (MAX_VAL - MIN_VAL) / 2);
            return (MAX_VAL - MIN_VAL) / 2;
        }
        int retValue = (mapval - MIN_UI_VAL) * (MAX_VAL - MIN_VAL) / (MAX_UI_VAL - MIN_UI_VAL);
        //Log.d(TAG, "unMappingLine UI: " + retValue  + " Applied: " + mapval);
        return retValue;
    }

    //convert 0~100 to -10~10 controlled by need or not
    private int MappingLine(int mapval, boolean need) {
        if (!need) {
            return mapval;
        }
        int retValue = 0;
        final int MIN_UI_VAL = -10; //UI MIN
        final int MAX_UI_VAL = 10; //UI MAX
        final int MIN_VAL = -10;
        final int MAX_VAL = 10;
        if (MIN_VAL < 0) {
            retValue = (mapval - (MAX_UI_VAL + MIN_UI_VAL) / 2) * (MAX_VAL - MIN_VAL)
                   / (MAX_UI_VAL - MIN_UI_VAL);
        } else {
            retValue = (mapval - MIN_UI_VAL) * (MAX_VAL - MIN_VAL) / (MAX_UI_VAL - MIN_UI_VAL);
        }
        //Log.d(TAG, "MappingLine UI: " + mapval + " Applied: " + retValue);
        return retValue;
    }

    private void setShow(int id, int value) {
        switch (id) {
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND1:{
                mBand1Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND2:{
                mBand2Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND3:{
                mBand3Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND4:{
                mBand4Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND5:{
                mBand5Text.setText(getBandTitle(id, value));
                break;
            }

            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND6:{
                mBand6Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND7:{
                mBand7Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND8:{
                mBand8Text.setText(getBandTitle(id, value));
                break;
            }
            case DroidAudioEffect.HPEQ_MODE_EFFECT_BAND9:{
                mBand9Text.setText(getBandTitle(id, value));
                break;
            }

            default:
            break;
        }
    }

    private String getBandTitle(int band, int value) {
        Log.d(TAG, "bandNum: " + mSelectedBandNum + " bandId: " + band);
        switch (mSelectedBandNum) {
            case 5:
                return band_5_Range_Title[band] + ":  " + value + " dB";
            case 7:
                return band_7_Range_Title[band] + ":  " + value + " dB";
            case 9:
                return band_9_Range_Title[band] + ":  " + value + " dB";
            default: {
                return "";
            }
        }
    }

    private String getShowString(int resid, int value) {
         return getActivity().getResources().getString(resid) + ": " + value + "%";
    }
}
