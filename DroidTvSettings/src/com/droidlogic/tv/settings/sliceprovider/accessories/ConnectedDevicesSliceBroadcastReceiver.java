/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.droidlogic.tv.settings.sliceprovider.accessories;

import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;
import static android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND;

import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceProvider.KEY_EXTRAS_DEVICE;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.DIRECTION_BACK;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.EXTRAS_DIRECTION;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.EXTRAS_SLICE_URI;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.FIND_MY_REMOTE_PHYSICAL_BUTTON_ENABLED_SETTING;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.notifyDeviceChanged;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.notifyToGoBack;
import static com.droidlogic.tv.settings.sliceprovider.accessories.ConnectedDevicesSliceUtils.setFindMyRemoteButtonEnabled;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.net.Uri;
import android.util.Log;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import android.bluetooth.BluetoothAdapter;

/**
 * This broadcast receiver handles these cases:
 * (a) Bluetooth toggle on.
 * (b) The followup pending intent for "rename"/"forget" preference to notify TvSettings UI flow to
 * go back.
 */
public class ConnectedDevicesSliceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectedSliceReceiver";

    static final String ACTION_FIND_MY_REMOTE = "com.google.android.tv.FIND_MY_REMOTE";
    static final String ACTION_TOGGLE_CHANGED =
            "com.droidlogic.googletv.settings.sliceprovider.accessories.TOGGLE_CHANGED";
    // The extra to specify toggle type. Currently, there is only Bluetooth toggle.
    static final String EXTRA_TOGGLE_TYPE = "TOGGLE_TYPE";
    static final String EXTRA_TOGGLE_STATE = "TOGGLE_STATE";
    // Bluetooth off is handled differently by ResponseActivity with confirmation dialog.
    static final String BLUETOOTH_ON = "BLUETOOTH_ON";
    static final String ACTIVE_AUDIO_OUTPUT = "ACTIVE_AUDIO_OUTPUT";
    static final String ACTION_BACK_AND_UPDATE_SLICE = "BACK_AND_UPDATE_SLICE";

    private ProgressDialog mProgress;
    private static final int MSG_ENABLE_BLUETOOTH_SWITCH = 0;
    private static final int TIME_DELAYED = 50;
    private static final String blueToothDialogMessage =
            "It takes a few seconds to update bluetooth status, please wait...";
    private static Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        final String action = intent.getAction();
        Log.d(TAG, "onReceive action: " + action);

        if (action == null) {
            return;
        }
        switch (action) {
            case ACTION_TOGGLE_CHANGED:
                final boolean isChecked = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, false);
                if (BLUETOOTH_ON.equals(intent.getStringExtra(EXTRA_TOGGLE_TYPE))) {
                    BluetoothAdapter bluetoothAdapter = AccessoryUtils.getDefaultBluetoothAdapter();
                    if (bluetoothAdapter != null) {
                        bluetoothAdapter.enable();
                    }

                    mProgress = new ProgressDialog(context);
                    showBlueToothEnablingDialog(context, mProgress,
                            "It takes a few seconds to update bluetooth status," +
                                    "\nplease wait...");
                    mHandler.sendEmptyMessageDelayed(MSG_ENABLE_BLUETOOTH_SWITCH, TIME_DELAYED);
                } else if (ACTIVE_AUDIO_OUTPUT.equals(intent.getStringExtra(EXTRA_TOGGLE_TYPE))) {

                        boolean enable = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, false);
                        BluetoothDevice device = intent.getParcelableExtra(KEY_EXTRAS_DEVICE,
                                BluetoothDevice.class);
                        AccessoryUtils.setActiveAudioOutput(enable ? device : null);
                        // refresh device
                    notifyDeviceChanged(context, device);
                } else if (FIND_MY_REMOTE_PHYSICAL_BUTTON_ENABLED_SETTING.equals(intent.getStringExtra(EXTRA_TOGGLE_TYPE))) {
                    setFindMyRemoteButtonEnabled(context, isChecked);
                    context.getContentResolver().notifyChange(
                            ConnectedDevicesSliceUtils.FIND_MY_REMOTE_SLICE_URI, null);
                }
                break;
            case ACTION_BACK_AND_UPDATE_SLICE:
                notifyToGoBack(context, Uri.parse(intent.getStringExtra(EXTRAS_SLICE_URI)));
                break;
            case ACTION_FIND_MY_REMOTE:
                context.sendBroadcast(
                        new Intent(ACTION_FIND_MY_REMOTE)
                                .putExtra("reason", "SETTINGS")
                                .setFlags(FLAG_INCLUDE_STOPPED_PACKAGES | FLAG_RECEIVER_FOREGROUND
                                        | FLAG_RECEIVER_INCLUDE_BACKGROUND),
                        "com.google.android.tv.permission.FIND_MY_REMOTE");
                break;
            default:
                break;
                // no-op
        }

    }

    private void showBlueToothEnablingDialog(Context context,
                                               ProgressDialog progressDialog,
                                               String blueToothDialogMessage) {
        progressDialog.setMessage(blueToothDialogMessage);
        progressDialog.setIndeterminate(false);
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_BLUETOOTH_SWITCH:
                    if (mProgress != null && mProgress.isShowing()
                            && AccessoryUtils.isBluetoothEnabled()) {
                        mContext.getContentResolver()
                                .notifyChange(ConnectedDevicesSliceUtils.GENERAL_SLICE_URI, null);
                        mProgress.dismiss();
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_ENABLE_BLUETOOTH_SWITCH, TIME_DELAYED);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
