package com.droidlogic.tv.settings.sliceprovider.manager;

import android.content.Context;
import android.hardware.hdmi.HdmiControlManager;
import android.content.ContentResolver;

import com.droidlogic.tv.settings.R;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;
import com.droidlogic.app.DroidAudioManager;
import com.droidlogic.app.AudioEffectManager;

public class HdmiCecContentManager {
    private static final String TAG = HdmiCecContentManager.class.getSimpleName();

    private Context mContext;
    private static volatile HdmiCecContentManager mHdmiCecContentManager;
    private DroidAudioManager mDroidAudioManager;
    private static AudioEffectManager mAudioEffectManager;
    private HdmiControlManager mHdmiControlManager;

    public static boolean isInit() {
        return mHdmiCecContentManager != null;
    }

    public static HdmiCecContentManager getHdmiCecContentManager(final Context context) {
        if (mHdmiCecContentManager == null) {
            synchronized (HdmiCecContentManager.class) {
                if (mHdmiCecContentManager == null) {
                    mHdmiCecContentManager = new HdmiCecContentManager(context);
                }
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
        mHdmiControlManager = mContext.getSystemService(HdmiControlManager.class);
        if (mDroidAudioManager == null) {
            mDroidAudioManager = DroidAudioManager.getInstance(mContext);
        }
        if (mAudioEffectManager == null) {
           mAudioEffectManager = AudioEffectManager.getInstance(context);
        }
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
        logDebug(TAG, false, "setVolumeControlStatus volume control:" + state);
    }

    public String isHdmiControlEnabledName() {
        boolean cecEnabled = mHdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED;

        return cecEnabled ? "Enabled" : "Disabled";
    }

    public boolean getSoundbarModeStatus() {
        return mDroidAudioManager.isSoundBarModeEnabled();
    }

    public void setSoundbarModeStatus(boolean state) {
        mDroidAudioManager.setSoundBarModeEnabled(state);
        mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_DAP_2_UI, state);
    }

}
