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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import java.util.ArrayList;
import java.util.Set;
import android.text.TextUtils;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.bluetooth.BluetoothClass;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import android.bluetooth.BluetoothDevice;

public class DroidCachedDeviceManageService extends Service {

    private static final String TAG = "DroidCachedDeviceManageService";
    private static final boolean DEBUG = false;

    private Context mContext = null;

    private BluetoothDevicesService.LocalBinder mBtDeviceServiceBinder;
    private boolean mBtDeviceServiceBound;

    private CheckBtStatusHandler mHandler = new CheckBtStatusHandler();
    private final int MSG_CONNECT_CACHED_DEVICE = 0;
    private ArrayList<String> mBondAudioDevices = new ArrayList<>();
    private boolean mWaitForDisconnct = false;

    private final ServiceConnection mBtDeviceServiceConnection = new SimplifiedConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBtDeviceServiceBinder = (BluetoothDevicesService.LocalBinder) service;
            mBtDeviceServiceBound = true;
        }

        @Override
        protected void cleanUp() {
            mBtDeviceServiceBound = false;
            mBtDeviceServiceBinder = null;
        }

    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mContext = this;
        if (!mBtDeviceServiceBound) {
            boolean ret = getApplicationContext().bindService(new Intent(mContext, AccessoryUtils.getBluetoothDeviceServiceClass()),
            mBtDeviceServiceConnection, Context.BIND_AUTO_CREATE);
            if (!ret) {
                String msg = "failed to bind btdeviceservice";
                Log.w(TAG, msg);
                throw new IllegalStateException(msg);
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter, mContext.RECEIVER_EXPORTED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            String action = intent.getAction();
            Log.i(TAG, "onReceive:" + action + ",mWaitForDisconnct:" + mWaitForDisconnct);
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (!mWaitForDisconnct) {
                    return;
                }
                mWaitForDisconnct = false;
                connectCachedAudioDevice();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                updateCachedAudioDev();
                mWaitForDisconnct = true;
            }
        }
    };

    private void connectCachedAudioDevice() {
        if (mBondAudioDevices.size()> 0) {
            mHandler.sendEmptyMessage(MSG_CONNECT_CACHED_DEVICE);
        }
    }

    private class CheckBtStatusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_CACHED_DEVICE:
                        String deviceAddress = mBondAudioDevices.get(0);
                        connectDevice(deviceAddress);

                        mBondAudioDevices.remove(0);
                        if (mBondAudioDevices.size() > 0)
                            mHandler.sendEmptyMessageDelayed(MSG_CONNECT_CACHED_DEVICE, 500);
                    break;
                default:
                       Log.d(TAG, "No handler case available for message: " + msg.what);
            }
        }
    }


    private void updateCachedAudioDev() {
         mBondAudioDevices.clear();

         BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBtAdapter == null) {
            Log.w(TAG, "Can't get BT adapter");
            return ;
        }
        final Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();
        if (bondedDevices == null) {
            Log.i(TAG, "No bondedDevices");
            return ;
        }

        for (final BluetoothDevice device : bondedDevices) {
            final String deviceAddress = device.getAddress();
            String deviceName = device.getName();
            if (TextUtils.isEmpty(deviceAddress)) {
                continue;
            }

            BluetoothClass btClass = device.getBluetoothClass();
            if ( btClass != null) {
                int bt_class = btClass.getMajorDeviceClass();
                if (bt_class == BluetoothClass.Device.Major.AUDIO_VIDEO) {
                    mBondAudioDevices.add(device.getAddress());
                    Log.i(TAG, "mBondAudioDevices add: " + device.getAddress());
                    disconnectAudioDev(device);
                }
            }
        }
     }

    private void disconnectAudioDev(BluetoothDevice device) {
        if (mBtDeviceServiceBinder != null) {
            mBtDeviceServiceBinder.disconnectDevice(device);
            Log.i(TAG, "disconnectAudioDev: " + device.getAddress());
        } else {
            Log.e(TAG, "failed to disconnect:" + device.getAddress());
        }
    }
    private void connectDevice(String devAddress) {
        BluetoothDevice device = BluetoothDevicesService.findDevice(devAddress);
        if (device != null) {
            CachedBluetoothDevice cachedDevice =
                      AccessoryUtils.getCachedBluetoothDevice(mContext, device);
            if (cachedDevice != null) {
                boolean isConnect = AccessoryUtils.isConnected(device);
                boolean isCachedDevConnect = cachedDevice.isConnected();
                boolean isBusy = cachedDevice.isBusy();

                if (DEBUG) {
                    String deviceName = device.getName();
                    String deviceName_a = AccessoryUtils.getLocalName(device);
                    Log.d(TAG, "      deviceName:" + deviceName + ",isConnect:" + isConnect + ",isConnected: " + device.isConnected());
                    Log.d(TAG, "cachedDeviceName:" + deviceName_a + "isCachedDevConnect:" + isCachedDevConnect + ",isBusy: " + isBusy);
                    Log.d(TAG, "mWaitForDisconnct:" + mWaitForDisconnct);
                }

                if (isConnect && isCachedDevConnect) {
                    Log.i(TAG, "deive is connected,no need to reconnect:" + devAddress);
                } else {
                    if (!isBusy) {
                        if (mBtDeviceServiceBinder != null) {
                            mBtDeviceServiceBinder.connectDevice(device);
                        } else {
                            Log.e(TAG, "failed to connect:" + devAddress);
                        }
                    }
                }
            }
        }else {
            Log.e(TAG, "cached device isn't found:" + devAddress);
        }
    }
}
