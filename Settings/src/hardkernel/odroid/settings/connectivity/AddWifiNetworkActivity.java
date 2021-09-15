/*
 * Copyright (C) 2014 The Android Open Source Project
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

package hardkernel.odroid.settings.connectivity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.android.internal.logging.nano.MetricsProto;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.connectivity.setup.AdvancedWifiOptionsFlow;
import hardkernel.odroid.settings.connectivity.setup.ChooseSecurityState;
import hardkernel.odroid.settings.connectivity.setup.ConnectFailedState;
import hardkernel.odroid.settings.connectivity.setup.ConnectState;
import hardkernel.odroid.settings.connectivity.setup.EnterPasswordState;
import hardkernel.odroid.settings.connectivity.setup.EnterSsidState;
import hardkernel.odroid.settings.connectivity.setup.OptionsOrConnectState;
import hardkernel.odroid.settings.connectivity.setup.SuccessState;
import hardkernel.odroid.settings.connectivity.setup.UserChoiceInfo;
import hardkernel.odroid.settings.connectivity.util.State;
import hardkernel.odroid.settings.connectivity.util.StateMachine;
import hardkernel.odroid.settings.core.instrumentation.InstrumentedActivity;

/**
 * Manual-style add wifi network (the kind you'd use for adding a hidden or out-of-range network.)
 */
public class AddWifiNetworkActivity extends InstrumentedActivity
        implements State.FragmentChangeListener {
    private static final String TAG = "AddWifiNetworkActivity";
    private final StateMachine.Callback mStateMachineCallback = new StateMachine.Callback() {
        @Override
        public void onFinish(int result) {
            setResult(result);
            finish();
        }
    };
    private State mChooseSecurityState;
    private State mConnectFailedState;
    private State mConnectState;
    private State mEnterPasswordState;
    private State mEnterSsidState;
    private State mSuccessState;
    private State mOptionsOrConnectState;
    private State mFinishState;
    private StateMachine mStateMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_container);
        mStateMachine = ViewModelProviders.of(this).get(StateMachine.class);
        mStateMachine.setCallback(mStateMachineCallback);
        UserChoiceInfo userChoiceInfo = ViewModelProviders.of(this).get(UserChoiceInfo.class);
        userChoiceInfo.getWifiConfiguration().hiddenSSID = true;

        mEnterSsidState = new EnterSsidState(this);
        mChooseSecurityState = new ChooseSecurityState(this);
        mEnterPasswordState = new EnterPasswordState(this);
        mConnectState = new ConnectState(this);
        mConnectFailedState = new ConnectFailedState(this);
        mSuccessState = new SuccessState(this);
        mOptionsOrConnectState = new OptionsOrConnectState(this);
        mFinishState = new FinishState(this);
        AdvancedWifiOptionsFlow.createFlow(
                this, true, true, null, mOptionsOrConnectState,
                mConnectState, AdvancedWifiOptionsFlow.START_DEFAULT_PAGE);

        /* Enter SSID */
        mStateMachine.addState(
                mEnterSsidState,
                StateMachine.CONTINUE,
                mChooseSecurityState
        );

        /* Choose security */
        mStateMachine.addState(
                mChooseSecurityState,
                StateMachine.OPTIONS_OR_CONNECT,
                mOptionsOrConnectState
        );
        mStateMachine.addState(
                mChooseSecurityState,
                StateMachine.CONTINUE,
                mEnterPasswordState
        );

        /* Enter Password */
        mStateMachine.addState(
                mEnterPasswordState,
                StateMachine.OPTIONS_OR_CONNECT,
                mOptionsOrConnectState
        );

        /* Options or Connect */
        mStateMachine.addState(
                mOptionsOrConnectState,
                StateMachine.CONNECT,
                mConnectState
        );
        mStateMachine.addState(
                mOptionsOrConnectState,
                StateMachine.RESTART,
                mEnterSsidState);

        /* Connect */
        mStateMachine.addState(
                mConnectState,
                StateMachine.RESULT_FAILURE,
                mConnectFailedState);
        mStateMachine.addState(
                mConnectState,
                StateMachine.RESULT_SUCCESS,
                mSuccessState);

        /* Connect Failed */
        mStateMachine.addState(
                mConnectFailedState,
                StateMachine.TRY_AGAIN,
                mOptionsOrConnectState
        );
        mStateMachine.addState(
                mConnectFailedState,
                StateMachine.SELECT_WIFI,
                mFinishState
        );

        mStateMachine.setStartState(mEnterSsidState);
        mStateMachine.start(true);
    }

    @Override
    public int getMetricsCategory() {
        // do not log visibility.
        return MetricsProto.MetricsEvent.ACTION_WIFI_ADD_NETWORK;
    }

    @Override
    public void onBackPressed() {
        mStateMachine.back();
    }

    private void updateView(Fragment fragment, boolean movingForward) {
        if (fragment != null) {
            FragmentTransaction updateTransaction = getSupportFragmentManager().beginTransaction();
            if (movingForward) {
                updateTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            } else {
                updateTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            }
            updateTransaction.replace(R.id.wifi_container, fragment, TAG);
            updateTransaction.commit();
        }
    }

    @Override
    public void onFragmentChange(Fragment newFragment, boolean movingForward) {
        updateView(newFragment, movingForward);
    }
}
