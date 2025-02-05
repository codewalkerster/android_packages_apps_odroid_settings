package hardkernel.odroid.settings.sliceprovider.ueventobserver;

import android.os.SystemProperties;
import android.os.UEventObserver;

import hardkernel.odroid.settings.util.DroidUtils;

public class SetModeUEventObserver {
    private static final String TAG = SetModeUEventObserver.class.getSimpleName();
    private UEventObserver mUEventObserver;
    private static volatile SetModeUEventObserver mSetModeUEventObserver;
    private boolean mIsObserved = false;
    private String mUeventVoutPath = "DEVPATH=/devices/platform/vout";
    private String mUeventVoutValue = "vout_setmode";

    private final String UEVENT_VOUT_2_PATH = "DEVPATH=/devices/platform/vout2";
    private final String UEVENT_VOUT_2_VALUE = "vout2_setmode";

    Runnable mRunnable = () -> {
    };

    public static SetModeUEventObserver getInstance() {
        if (mSetModeUEventObserver == null)
            synchronized (SetModeUEventObserver.class) {
                if (mSetModeUEventObserver == null)
                    mSetModeUEventObserver = new SetModeUEventObserver();
            }
        return mSetModeUEventObserver;
    }

    public SetModeUEventObserver() {
        mUEventObserver =
            new UEventObserver() {
                @Override
                public void onUEvent(UEvent uEvent) {
                    if (uEvent.get(mUeventVoutValue).equals("0")
                            || uEvent.get(mUeventVoutValue).equals("1")) {
                        mRunnable.run();
                        mIsObserved = true;
                    }
                }
            };
    }

    public void setOnUEventRunnable(Runnable runnable) {
        mRunnable = runnable;
    }

    public void startObserving() {
        initUeventParameter();
        resetObserved();
        mUEventObserver.startObserving(mUeventVoutPath);
    }

    public void stopObserving() {
        mUEventObserver.stopObserving();
    }

    public boolean isObserved() {
        return mIsObserved;
    }

    public void resetObserved() {
        mIsObserved = false;
    }

    private void initUeventParameter() {
        if (DroidUtils.hasBdsUiMode()) {
            mUeventVoutPath = UEVENT_VOUT_2_PATH;
            mUeventVoutValue = UEVENT_VOUT_2_VALUE;
        }
    }
}
