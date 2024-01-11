package com.droidlogic.tv.settings.sliceprovider.manager;

//import static android.provider.Settings.Global.HDMI_CONTROL_ENABLED;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.provider.Settings;
import android.hardware.hdmi.HdmiControlManager;
import android.content.ContentResolver;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class HdmiCecContentManager {
    private static final String TAG = HdmiCecContentManager.class.getSimpleName();

    private Context mContext;
    private ContentResolver mResolver;
    private static volatile HdmiCecContentManager mHdmiCecContentManager;
    private HdmiControlManager mHdmiControlManager;  // This is a system service(HdmiControlManager).

    public static boolean isInit() {
        return mHdmiCecContentManager != null;
    }

    public static HdmiCecContentManager getHdmiCecContentManager(final Context context) {
        if (mHdmiCecContentManager == null) {
            synchronized (HdmiCecContentManager.class) {
            if (mHdmiCecContentManager == null)
                mHdmiCecContentManager = new HdmiCecContentManager(context);
            }
        }
        return mHdmiCecContentManager;
    }

    public static void shutdown(final Context context) {
        if (mHdmiCecContentManager != null) {
            synchronized (HdmiCecContentManager.class) {
                if (mHdmiCecContentManager != null) {
                    mHdmiCecContentManager = null;
                }
            }
        }
    }

    private HdmiCecContentManager(final Context context) {
        mContext = context;
        mResolver = mContext.getContentResolver();
        mHdmiControlManager = mContext.getSystemService(HdmiControlManager.class);
    }

    public boolean isHdmiControlEnabled() {
        boolean hdmiCecEnable = (mHdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED);
        logDebug(TAG, false, "isHdmiControlEnabled: " + hdmiCecEnable);
        return hdmiCecEnable;
    }

    public void setHdmiCecEnabled(boolean enable) {
        logDebug(TAG, false, "setHdmiCecEnabled cec switch: " + enable);

        mHdmiControlManager.setHdmiCecEnabled(enable
                ? HdmiControlManager.HDMI_CEC_CONTROL_ENABLED
                : HdmiControlManager.HDMI_CEC_CONTROL_DISABLED);

        mResolver.notifyChange(MediaSliceConstants.DISPLAYSOUND_HDMI_CEC_URI, null);
    }

    public boolean getVolumeControlStatus() {
        if (mHdmiControlManager != null) {
            return mHdmiControlManager.getHdmiCecVolumeControlEnabled() == 1;
        }
        return false;
    }

    public void setVolumeControlStatus(int state) {
        ContentResolver resolver = mContext.getContentResolver();
        if (mHdmiControlManager != null) {
            mHdmiControlManager.setHdmiCecVolumeControlEnabled(state);
        }
        resolver.notifyChange(MediaSliceConstants.DISPLAYSOUND_HDMI_CEC_URI, null);
        logDebug(TAG, false, "setVolumeControlStatus volume control:" + state);
    }

    public String isHdmiControlEnabledName() {
        boolean cecEnabled = mHdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED;

        return cecEnabled ? "Enabled" : "Disabled";
    }

}
