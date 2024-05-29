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

package com.droidlogic.tv.settings;

import android.bluetooth.BluetoothDevice;

import com.google.android.tv.btservices.BluetoothDeviceService;
import com.google.android.tv.btservices.remote.DfuProvider;
import com.google.android.tv.btservices.remote.DfuBinary;
import com.google.android.tv.btservices.remote.RemoteProxy;
import com.google.android.tv.btservices.remote.Version;
import com.google.android.tv.btservices.remote.DefaultProxy;

public class DefaultBluetoothDeviceService extends BluetoothDeviceService {

    // Device firmware update is provided by DfuService, so disabling Dfu here
    @Override
    protected DfuProvider getDfuProvider() {
        return null;
    }

    // Default implementation to provide battery level of remote control
    @Override
    protected RemoteProxy createRemoteProxy(BluetoothDevice device) {
        return new DefaultProxy(this, device);
    }
}

