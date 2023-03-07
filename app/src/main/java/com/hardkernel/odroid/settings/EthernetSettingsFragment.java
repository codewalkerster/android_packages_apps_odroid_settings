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

package com.hardkernel.odroid.settings;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.support.v7.preference.PreferenceScreen;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Iterator;

import android.os.ServiceManager;
import android.os.IPowerManager;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;

@Keep
public class EthernetSettingsFragment extends LeanbackAddBackPreferenceFragment
        implements Preference.OnPreferenceChangeListener, OnClickListener {
    private static final String LOG_TAG = "EthernetSettingsFragment";

    final static int DHCP = 0;
    final static int STATIC_IP = 1;
    final static int NOPROXY = 0;
    final static int PROXY_STATIC = 1;


    private View view_dialog;
    private AlertDialog mAlertDialog = null;

    private Spinner mIpSettingsSpinner;
    private Spinner mProxySettingsSpinner;
    private LinearLayout mstaticip;
    private LinearLayout mProxy;
    private EditText mEditTextEthIpaddress;
    private EditText mEditTextEthGateway;
    private EditText mEditTextEthPrefix;
    private EditText mEditTextEthDns1;
    private EditText mEditTextEthDns2;
    private TextView mProxyHostView;
    private TextView mProxyPortView;
    private TextView mProxyExclusionListView;

    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private ProxyInfo mHttpProxy = null;
    private StaticIpConfiguration mStaticIpConfiguration = null;
    private EthernetManager mEthernetManager;
    private String mInterfaceName;
    private IpConfiguration mIpConfiguration;

    private Preference mStaticIPPref;
    private TwoStatePreference wolPref;
    private Preference macPref;

    private static final String KEY_IP_SETTINGS = "pref_static_ip";
    private static final String KEY_MAC_ADDRESS_PREF = "eth0_address";
    private static final String KEY_WAKE_ON_LAN_SWITCH = "wol_switch";

    private static boolean wolSwitch = false;

    private static final String ETH_MAC_NODE ="/sys/class/net/eth0/address";

    public static EthernetSettingsFragment newInstance() {
        return new EthernetSettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.ethernet_settings, null);

        mStaticIPPref = findPreference(KEY_IP_SETTINGS);
        wolPref = (TwoStatePreference) findPreference(KEY_WAKE_ON_LAN_SWITCH);
        macPref = (Preference) findPreference(KEY_MAC_ADDRESS_PREF);

        mEthernetManager = (EthernetManager) getActivity().getSystemService(Context.ETHERNET_SERVICE);
        if (mEthernetManager != null) {
            Log.i(LOG_TAG, "Connected to EthernetManager");
        } else {
            Log.e(LOG_TAG, "Unable connect to EthernetManager");
        }

        wolSwitch = (ConfigEnv.getWakeOnLan() == 1);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ETH_MAC_NODE));
            macPref.setSummary(reader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        wolPref.setChecked(wolSwitch);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();

        if (key == null) {
            return super.onPreferenceTreeClick(preference);
        }

        switch (key) {
            case KEY_WAKE_ON_LAN_SWITCH: {
                if (wolPref.isChecked() != wolSwitch) {
                    wolSwitch = wolPref.isChecked();
                    ConfigEnv.setWakeOnLan(wolSwitch ? 1 : 0);
                    if (wolSwitch) {
                        Toast.makeText(getContext(),
                            "Wake On Lan will be applied after reboot!",
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case KEY_IP_SETTINGS: {
                showDialog();
            }
            break;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void showDialog () {
        if (mAlertDialog == null) {
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view_dialog = inflater.inflate(R.layout.dialog_eth, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            mAlertDialog = builder.create();
            mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

            TextView button_cancel = (TextView)view_dialog.findViewById(R.id.dialog_cancel);
            button_cancel.setOnClickListener(this);

            TextView button_ok = (TextView)view_dialog.findViewById(R.id.dialog_ok);
            button_ok.setOnClickListener(this);
        }

        load();

        mIpSettingsSpinner = (Spinner) view_dialog.findViewById(R.id.ip_settings);
        mProxySettingsSpinner = (Spinner) view_dialog.findViewById(R.id.proxy_settings);
        mstaticip = (LinearLayout) view_dialog.findViewById(R.id.staticip);
        mProxy = (LinearLayout) view_dialog.findViewById(R.id.proxy_fields);
        mEditTextEthIpaddress = (EditText) view_dialog.findViewById(R.id.ipaddress);
        mEditTextEthGateway = (EditText) view_dialog.findViewById(R.id.gateway);
        mEditTextEthPrefix = (EditText) view_dialog.findViewById(R.id.network_prefix_length);
        mEditTextEthDns1 = (EditText) view_dialog.findViewById(R.id.dns1);
        mEditTextEthDns2 = (EditText) view_dialog.findViewById(R.id.dns2);


        mProxyHostView = (TextView) view_dialog.findViewById(R.id.proxy_hostname);
        mProxyPortView = (TextView) view_dialog.findViewById(R.id.proxy_port);
        mProxyExclusionListView = (TextView) view_dialog.findViewById(R.id.proxy_exclusionlist);

        refresh();

        mIpSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
                    mstaticip.setVisibility(LinearLayout.VISIBLE);
                } else {
                    mstaticip.setVisibility(LinearLayout.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        mProxySettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
                    mProxy.setVisibility(LinearLayout.VISIBLE);
                } else {
                    mProxy.setVisibility(LinearLayout.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view_dialog);
        mAlertDialog.setCancelable(false);
        mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_cancel:
                if (mAlertDialog != null) {
                    load();
                    refresh();
                    mAlertDialog.dismiss();
                }
                break;
            case R.id.dialog_ok:
                if (mAlertDialog != null) {
                    if (ipAndProxyFieldsAreValid()) {
                        setIpConfiguration(new IpConfiguration(mIpAssignment, mProxySettings,
                                mStaticIpConfiguration, mHttpProxy));
                        save();
                        mAlertDialog.dismiss();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }


    private boolean ipAndProxyFieldsAreValid() {
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            mStaticIpConfiguration = new StaticIpConfiguration();
            int result = validateIpConfigFields(mStaticIpConfiguration);
            if (result != 0) {
                return false;
            }
        }

        final int selectedPosition = mProxySettingsSpinner.getSelectedItemPosition();
        mProxySettings = ProxySettings.NONE;
        mHttpProxy = null;
        if (selectedPosition == PROXY_STATIC && mProxyHostView != null) {
            mProxySettings = ProxySettings.STATIC;
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                mHttpProxy = new ProxyInfo(host, port, exclusionList);
            } else {
                return false;
            }
        }
        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        if (mEditTextEthIpaddress == null) return 0;

        String ipAddr = mEditTextEthIpaddress.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) return -1;

        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null) {
            return -1;
        }

        int networkPrefixLength = 24;
        try {
            String temp = mEditTextEthPrefix.getText().toString();
            if (temp.length() != 0)
                networkPrefixLength = Integer.parseInt(mEditTextEthPrefix.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return -1;
            }
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            mEditTextEthPrefix.setText("24");
        }

        String gateway = mEditTextEthGateway.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length - 1] = 1;
                mEditTextEthGateway.setText(InetAddress.getByAddress(addr).getHostAddress());
            } catch (RuntimeException ee) {
            } catch (java.net.UnknownHostException u) {
            }
        } else {
            InetAddress gatewayAddr = getIPv4Address(gateway);
            if (gatewayAddr == null) {
                return -1;
            }
            staticIpConfiguration.gateway = gatewayAddr;
        }

        String dns = mEditTextEthDns1.getText().toString();
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            //If everything else is valid, provide hint as a default option
            mEditTextEthDns1.setText("8.8.8.8");
        } else {
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return -1;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }

        if (mEditTextEthDns2.length() > 0) {
            dns = mEditTextEthDns2.getText().toString();
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return -1;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }
        return 0;
    }

    /**
     * validate syntax of hostname and port entries
     *
     * @return 0 on success, string resource ID on failure
     */
    public static int validate(String hostname, String port, String exclList) {
        switch (Proxy.validate(hostname, port, exclList)) {
            case Proxy.PROXY_VALID:
                return 0;
            case Proxy.PROXY_HOSTNAME_EMPTY:
                return R.string.proxy_error_empty_host_set_port;
            case Proxy.PROXY_HOSTNAME_INVALID:
                return R.string.proxy_error_invalid_host;
            case Proxy.PROXY_PORT_EMPTY:
                return R.string.proxy_error_empty_port;
            case Proxy.PROXY_PORT_INVALID:
                return R.string.proxy_error_invalid_port;
            case Proxy.PROXY_EXCLLIST_INVALID:
                return R.string.proxy_error_invalid_exclusion_list;
            default:
                // should neven happen
                Log.e(LOG_TAG, "Unknown proxy settings error");
                return -1;
        }
    }

    public void setIpConfiguration(IpConfiguration configuration) {
        mIpConfiguration = configuration;
    }

    /**
     * Load IpConfiguration from system.
     */
    public void load() {
        String[] ifaces = mEthernetManager.getAvailableInterfaces();
        if (ifaces.length > 0) {
            mInterfaceName = ifaces[0];
            mIpConfiguration = mEthernetManager.getConfiguration(mInterfaceName);
            Log.i(LOG_TAG, "Found " + ifaces.length + " Ethernet interfaces " + mInterfaceName);
        }
    }

    public void save() {
        if (mInterfaceName != null) {
            mEthernetManager.setConfiguration(mInterfaceName, mIpConfiguration);
        }
    }

    public void refresh(){
        if (mIpConfiguration.getIpAssignment() == IpAssignment.STATIC) {
            mStaticIPPref.setSummary("STATIC");
            mIpSettingsSpinner.setSelection(STATIC_IP);
            StaticIpConfiguration staticConfig = mIpConfiguration.getStaticIpConfiguration();

            if (staticConfig.ipAddress != null) {
                mEditTextEthIpaddress.setText(
                        staticConfig.ipAddress.getAddress().getHostAddress());
                mEditTextEthPrefix.setText(Integer.toString(staticConfig.ipAddress
                        .getNetworkPrefixLength()));
            }

            if (staticConfig.gateway != null) {
                mEditTextEthGateway.setText(staticConfig.gateway.getHostAddress());
            }

            Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
            if (dnsIterator.hasNext()) {
                mEditTextEthDns1.setText(dnsIterator.next().getHostAddress());
            }
            if (dnsIterator.hasNext()) {
                mEditTextEthDns2.setText(dnsIterator.next().getHostAddress());
            }


        } else {
            mIpSettingsSpinner.setSelection(DHCP);
            mStaticIPPref.setSummary("DHCP");
        }

        if (mIpConfiguration != null) {
            ProxyInfo proxyProperties = mIpConfiguration.getHttpProxy();
            if (proxyProperties != null) {
                mProxyHostView.setText(proxyProperties.getHost());
                mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                mProxyExclusionListView.setText(proxyProperties.getExclusionListAsString());
                mProxySettingsSpinner.setSelection(PROXY_STATIC);
            } else {
                mProxySettingsSpinner.setSelection(NOPROXY);
            }
        }

    }
}
