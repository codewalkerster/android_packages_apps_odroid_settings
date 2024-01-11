package com.droidlogic.tv.settings.sliceprovider.manager;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import com.droidlogic.app.DataProviderManager;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class PowerBehaviorContentManager {
    private Context mContext;
    private static final String TAG = PowerBehaviorContentManager.class.getSimpleName();

    private static volatile PowerBehaviorContentManager mPowerBehaviorContentManager;
    private static final String POWER_KEY_DEFINITION = "power_key_definition";
    private static final String PERSISTENT_PROPERTY_POWER_KEY_ACTION = "persist.sys.power.key.action";
    private static final String PERSISTENT_PROPERTY_GTV_VERSION_ACTION = "ro.com.google.gmsversion";
    private static final int SUSPEND = 0;
    private static final int SHUTDOWN = 1;
    private static int PowerKeyDefinition = 0;

    private static final int MSG_BEHAVIOR_GET = 0;
    private static final int MSG_BEHAVIOR_SET = 1;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private final Object mPowerHandlerObject = new Object();

    private static final Map<String, Integer> POWER_BEHAVIOR_LIST = new HashMap();

    public static boolean isInit() {
        return mPowerBehaviorContentManager != null;
    }

    private PowerBehaviorContentManager(final Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("TvSettings_PowerBehavior");
        mHandlerThread.start();
        mHandler = new PowerHandler(mHandlerThread.getLooper());

        POWER_BEHAVIOR_LIST.put("Suspend", SUSPEND);
        POWER_BEHAVIOR_LIST.put("Shutdown", SHUTDOWN);
    }

    public static PowerBehaviorContentManager getPowerBehaviorContentManager(final Context context) {
        if (mPowerBehaviorContentManager == null) {
            synchronized (PowerBehaviorContentManager.class) {
                if (mPowerBehaviorContentManager == null) {
                    mPowerBehaviorContentManager = new PowerBehaviorContentManager(context);
                }
            }
        }
        return mPowerBehaviorContentManager;
    }

    public static void shutdown(final Context context) {
        if (mPowerBehaviorContentManager != null) {
            synchronized (PowerBehaviorContentManager.class) {
                if (mPowerBehaviorContentManager != null) {
                    mPowerBehaviorContentManager = null;
                }
            }
        }
    }

    public HashMap<String, Integer> getpowerBehavioModeList() {
        return (HashMap<String, Integer>) POWER_BEHAVIOR_LIST;
    }

    public void setPowerActionDefinition(String powerKeyAction) {
        Message msg = Message.obtain();
        msg.what = MSG_BEHAVIOR_SET;
        msg.obj = powerKeyAction;
        mHandler.sendMessage(msg);
    }

    public int getCurrentBehavior() {

        synchronized (mPowerHandlerObject) {
            Message msg = Message.obtain();
            msg.what = MSG_BEHAVIOR_GET;
            mHandler.sendMessage(msg);
            try {
                mPowerHandlerObject.wait();
            } catch (Exception e) {
                logDebug(TAG, true,"mPowerHandlerObject error " + e.getMessage());
            }
        }
        logDebug(TAG, true, "PowerKeyDefinition: " + PowerKeyDefinition);
        return PowerKeyDefinition;
    }

    private void setPowerKeyActionDefinition(String powerKeyAction) {
        logDebug(TAG, true, "powerKeyAction: " + powerKeyAction + " powerKey: " + POWER_BEHAVIOR_LIST.get(powerKeyAction));

        DataProviderManager.putIntValue(mContext, POWER_KEY_DEFINITION, POWER_BEHAVIOR_LIST.get(powerKeyAction));
        SystemProperties.set(PERSISTENT_PROPERTY_POWER_KEY_ACTION, String.valueOf(POWER_BEHAVIOR_LIST.get(powerKeyAction)));
    }

    private void whichPowerKeyDefinition() {
        PowerKeyDefinition = DataProviderManager.getIntValue(mContext, POWER_KEY_DEFINITION, SUSPEND);
    }

    private class PowerHandler extends Handler {
        private static final int MSG_SYNC_ATV_MTS_MODE = 1000;

        private PowerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BEHAVIOR_GET:
                    whichPowerKeyDefinition();
                    synchronized (mPowerHandlerObject) {
                        mPowerHandlerObject.notifyAll();
                    }
                    break;
                case MSG_BEHAVIOR_SET:
                    String powerKeyAction = (String) msg.obj;
                    logDebug(TAG, false, "powerKeyAction is : " + powerKeyAction);
                    setPowerKeyActionDefinition(powerKeyAction);
                    break;
                default:
                    break;
            }
        }
    }

    public boolean hasGtvsUiMode() {
        return !TextUtils.isEmpty(SystemProperties.get(PERSISTENT_PROPERTY_GTV_VERSION_ACTION, ""));
    }
}
