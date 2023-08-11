package com.droidlogic.tv.settings.sliceprovider.dialog;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

import android.util.Log;

import android.content.DialogInterface;
import android.os.Handler;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager.HdrFormat;

import java.util.concurrent.TimeUnit;

public class AdjustResolutionDialogActivity extends BaseDialogActivity {
    private static final String COUNTDOWN_PLACEHOLDER = "COUNTDOWN_PLACEHOLDER";
    private static final String RESOLUTION_PLACEHOLDER = "RESOLUTION_PLACEHOLDER";
    private static String TAG = AdjustResolutionDialogActivity.class.getSimpleName();
    private static final int DIALOG_START_MILLIS = 1000;
    private static final int DEFAULT_COUNTDOWN_MILLISECONDS = 15000;
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private CountDownTimer mCountDownTimer;

    private Runnable mRestoreCallback = () -> {
    };
    private static String mNextMode;
    private static String mCurrentMode;
    private boolean mWasDolbyVisionChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDisplayCapabilityManager =
                DisplayCapabilityManager.getDisplayCapabilityManager(getApplicationContext());

        mCurrentMode = mDisplayCapabilityManager.getCurrentMode();
        mNextMode = getIntent().getStringExtra(EXTRA_PREFERENCE_KEY);
        Log.d(TAG, "mCurrentMode: " + mCurrentMode + "; mNextMode: " + mNextMode);

        mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(mNextMode);
        mWasDolbyVisionChanged = mDisplayCapabilityManager.adjustDolbyVisionByMode(mNextMode);

        mRestoreCallback =
            () -> {
                mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(mCurrentMode);
                if (mWasDolbyVisionChanged) {
                    mDisplayCapabilityManager.setPreferredFormat(HdrFormat.DOLBY_VISION);
                }
            };

        new Handler().postDelayed(this::showWarningDialogOnResolutionChange, DIALOG_START_MILLIS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
        }
    }

    private void showWarningDialogOnResolutionChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogBackground);
        builder.setCancelable(false);
        builder.setPositiveButton(
                R.string.adjust_resolution_dialog_ok_msg,
                (dialog, which) -> {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    finish();
                });
        builder.setNegativeButton(
                R.string.adjust_resolution_dialog_cancel_msg,
                (dialog, which) -> {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    finish();
                    mRestoreCallback.run();
                });

        builder.setTitle(getString(R.string.adjust_resolution_dialog_title));
        builder.setMessage(createWarningMessage());
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button cancelButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                mCountDownTimer =
                        new CountDownTimer(DEFAULT_COUNTDOWN_MILLISECONDS, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                cancelButton.setText(getString(R.string.adjust_resolution_dialog_cancel_msg)
                                        .replace(COUNTDOWN_PLACEHOLDER,
                                                String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))));
                            }
                            @Override
                            public void onFinish() {
                                mAlertDialog.dismiss();
                                finish();
                                mRestoreCallback.run();
                            }
                        };
                mCountDownTimer.start();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    }

    private String createWarningMessage() {
        String msg;
        if (!mWasDolbyVisionChanged) {
            msg = getString(R.string.adjust_resolution_dialog_desc);
        } else {
            msg = getString(R.string.adjust_resolution_and_disable_dv_dialog_desc);
        }

        return msg.replace(RESOLUTION_PLACEHOLDER, mDisplayCapabilityManager.getTitleByMode(mNextMode));
    }

}
