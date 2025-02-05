/*
 * Copyright (C) 2021 The Android Open Source Project
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

package hardkernel.odroid.settings.sliceprovider.dialog;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.core.content.ContextCompat;
import hardkernel.odroid.settings.R;

public class AVSyncTuningActivity extends Activity {

    private static final String TAG = "AVSyncTuningActivity";

    private TextView mShiftText;
    private Button mApplyButton;
    private ImageView mPointView;
    private VideoView mVideoView;

    private static final int MAX_LENGTH = 520;
    //length of one step
    private static final int STEP = 50;
    //Max number of moves
    private static final int MAX_ADJUST = 250 / STEP;
    //Min number of moves
    private static final int MIN_ADJUST = -150 / STEP;
    private static int userProgress;

    private int mShift;
    private int mUiBasePoint;
    private float mDensity;
    private float mMoveLength;

    private Context mContext;
    private AudioManager mAudioManager;

    private final String PARAM_HAL_AUDIO_OUT_DEV_DELAY = "hal_param_out_dev_delay_time_ms";
    private final String HAL_AUDIO_OUT_DEV_DELAY_SPDIF = " db_id_audio_output_spdif_delay_media";
    private final int AUDIO_OUTPUT_DELAY = 1;
    private final int HAL_AUDIO_OUT_DEV_DELAY_DEFAULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mAudioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        mMoveLength = MAX_LENGTH * mDensity / (MAX_ADJUST - MIN_ADJUST);
        initView();
        moveToBasePoint();
    }
    private void moveToBasePoint() {
        mUiBasePoint = MIN_ADJUST + (MAX_ADJUST - MIN_ADJUST) / 2;
        startAnim(-mUiBasePoint * mMoveLength, -mUiBasePoint * mMoveLength);
    }
    private void initView() {
        setContentView(R.layout.av_sync_tuning_activity);
        mVideoView = findViewById(R.id.video_view);
        mShiftText = findViewById(R.id.text_shift);
        mPointView = findViewById(R.id.point);
        mApplyButton = findViewById(R.id.button_apply);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userProgress = Settings.Global.getInt(getContentResolver(),
                HAL_AUDIO_OUT_DEV_DELAY_SPDIF, HAL_AUDIO_OUT_DEV_DELAY_DEFAULT);
        shiftMove(userProgress / STEP);
        Log.i(TAG, "userProgress: " + userProgress);
        mShiftText.setText(String.format("%dms", userProgress));

        mApplyButton.setFocusable(false);

        String uri = "android.resource://" + getPackageName()
                + "/" + R.raw.audio_video_sync_test;
        mVideoView.setVideoURI(Uri.parse(uri));
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
        mVideoView.start();
    }

    @Override
    public void onPause() {
        showToastAndFinish();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            shiftMove(-1);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            shiftMove(1);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.i(TAG, "OK button pressed");
                setUserPreferredProgress(userProgress);
                mApplyButton.setBackground(ContextCompat
                        .getDrawable(mContext, R.drawable.button_high_brightness));
                mApplyButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mApplyButton.setBackground(ContextCompat
                                .getDrawable(mContext, R.drawable.button_default));
                    }
                }, 200);
                return true;
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    private void setUserPreferredProgress(int userProgress) {
        Log.i(TAG, "setUserPreferredProgress: userProgress = " + userProgress);
        short delayMsShort = (short) userProgress;
        mAudioManager.setParameters(PARAM_HAL_AUDIO_OUT_DEV_DELAY + "=" +
                (AUDIO_OUTPUT_DELAY << 16 | (delayMsShort & 0xFFFF)));
        Settings.Global.putInt(getContentResolver(), HAL_AUDIO_OUT_DEV_DELAY_SPDIF, userProgress);
    }

    private void showToastAndFinish() {
        Log.i(TAG, "showToastAndFinish");
        int userSavedProgress = Settings.Global.getInt(getContentResolver(),
                HAL_AUDIO_OUT_DEV_DELAY_SPDIF, HAL_AUDIO_OUT_DEV_DELAY_DEFAULT);
        if (userSavedProgress == userProgress) {
            Toast.makeText(getApplicationContext(), R.string.av_sync_apply_toast, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void shiftMove(int change) {
        Log.i(TAG, "shiftMove: " + change + " mShift: " + mShift);
        int newShift = mShift + change;
        if (newShift > MAX_ADJUST || newShift < MIN_ADJUST) {
            return;
        }
        startAnim((mShift - mUiBasePoint) * mMoveLength, (newShift - mUiBasePoint) * mMoveLength);
        mShift = newShift;
        userProgress = mShift * STEP;
        Log.i(TAG, "userProgress: " + userProgress);
        mShiftText.setText(String.format("%dms", userProgress));
    }

    private void startAnim(float startX, float endX) {
        Log.i(TAG, "startAnim: " + startX + "   " + endX);
        Animation translateAnimation = new TranslateAnimation(startX, endX, 0, 0);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        mPointView.startAnimation(translateAnimation);

    }

}