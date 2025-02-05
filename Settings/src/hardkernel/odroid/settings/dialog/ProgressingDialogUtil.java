/*
 * Copyright (C) 2023 The Android Open Source Project
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

package hardkernel.odroid.settings.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.widget.Button;
import hardkernel.odroid.settings.R;
import java.util.concurrent.TimeUnit;

public class ProgressingDialogUtil {

    private static final int DIALOG_TIMEOUT_MILLIS = 12000;

    public void showWarningDialogOnResolutionChange(
            Context context, String currentMode, DialogCallBackInterface callBack) {
        final CountDownTimer[] timerTask = {null};
        final String dialogDescription =
                context.getString(R.string.resolution_selection_dialog_desc,
                        currentMode);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.resolution_selection_dialog_title)
                .setMessage(dialogDescription)
                .setPositiveButton(
                        R.string.resolution_selection_dialog_ok,
                        (dialog1, which) -> {
                            callBack.positiveCallBack();
                            dialog1.dismiss();
                            timerTask[0].cancel();
                        })
                .setNegativeButton(
                        R.string.resolution_selection_dialog_cancel,
                        (dialog12, which) -> {
                            callBack.negativeCallBack();
                            dialog12.dismiss();
                            timerTask[0].cancel();
                        })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button cancelButton =
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                final CharSequence negativeButtonText = cancelButton.getText();
                timerTask[0] = new CountDownTimer(DIALOG_TIMEOUT_MILLIS, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        cancelButton.setText(String.format("%s (%d)", negativeButtonText,
                                //add one to timeout so it never displays zero
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                        ));
                    }
                    @Override
                    public void onFinish() {
                        if (((AlertDialog) dialog).isShowing()) {
                            callBack.negativeCallBack();
                            dialog.dismiss();
                        }
                    }
                };
                timerTask[0].start();
            }
        });
        dialog.show();
    }

    public interface DialogCallBackInterface {
        void positiveCallBack();
        void negativeCallBack();
    }

}