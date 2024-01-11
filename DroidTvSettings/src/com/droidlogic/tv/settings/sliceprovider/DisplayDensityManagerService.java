package com.droidlogic.tv.settings.sliceprovider;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.appcompat.R;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class DisplayDensityManagerService extends Service {
    private static final ImmutableSet<Integer> MANAGED_DISPLAY_TYPES = ImmutableSet.of(0, 1, 2);
    private static final Integer MAX_HEIGHT_OF_UI = 1080;
    private static final ImmutableMap<Integer, Integer> PREFERRED_DPI_BY_DISPLAY_WIDTH =
            new ImmutableMap.Builder<Integer, Integer>()
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_1920, MediaSliceConstants.MEDIA_DISPLAY_DENSITY_HIGH)
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_1280, MediaSliceConstants.MEDIA_DISPLAY_DENSITY_MIDDLE)
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_720, Integer.valueOf((int) R.styleable.AppCompatTheme_windowFixedHeightMajor))
                    .build();

    private static final String TAG = DisplayDensityManagerService.class.getSimpleName();
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private IWindowManager mWindowManagerService;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logDebug(TAG, false, "PREFERRED_DPI_BY_DISPLAY_WIDTH: " + PREFERRED_DPI_BY_DISPLAY_WIDTH);

        mDisplayManager = (DisplayManager) getSystemService(DisplayManager.class);
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        Display[] displays = mDisplayManager.getDisplays();
        for (Display display : displays) {
            if (MANAGED_DISPLAY_TYPES.contains(Integer.valueOf(display.getType()))) {
                resetDisplaySize(display.getDisplayId());
                adjustDisplayDensityByMode(display.getDisplayId());
            }
        }
        mDisplayListener = new DisplayManager.DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (MANAGED_DISPLAY_TYPES.contains(Integer.valueOf(mDisplayManager.getDisplay(displayId).getType()))) {
                    logDebug(TAG, false, "onDisplayChanged displayId: " + displayId);
                    adjustDisplayDensityByMode(displayId);
                }
            }
        };

        mDisplayManager.registerDisplayListener(mDisplayListener, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
    }

    private void resetDisplaySize(int displayId) {
        try {
            mWindowManagerService.clearForcedDisplaySize(displayId);
        } catch (RemoteException e) {
            logDebug(TAG, true, "Cannot reset the display size.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustDisplayDensityByMode(int displayId) {
        Display.Mode mode = mDisplayManager.getDisplay(displayId).getMode();
        logDebug(TAG, false, "adjust display density on display "
                + displayId + " according to the new display mode " + mode);
        try {
            int density = MediaSliceConstants.MEDIA_DISPLAY_DENSITY_HIGH;  // The maximum resolution density is used by default
            if (mode.getPhysicalHeight() <= MAX_HEIGHT_OF_UI.intValue()) {
                ImmutableMap<Integer, Integer> immutableMap = PREFERRED_DPI_BY_DISPLAY_WIDTH;
                if (immutableMap.containsKey(Integer.valueOf(mode.getPhysicalWidth()))) {
                    density = immutableMap.get(Integer.valueOf(mode.getPhysicalWidth())).intValue();
                } else {
                    density =
                            (mode.getPhysicalWidth() * MediaSliceConstants.MEDIA_DISPLAY_DENSITY_HIGH)
                                    / MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_1920;
                    logDebug(TAG, true, "Unexpected display width = "
                            + mode.getPhysicalWidth() + ", change the DPI to " + density);
                }
            }

            logDebug(TAG, false, "density: " + density);
            // A user id constant to indicate the "owner" user of the device.
            mWindowManagerService.setForcedDisplayDensityForUser(displayId, density, UserHandle.USER_OWNER);
        } catch (RemoteException e) {
            logDebug(TAG, true, "Cannot change the display density.The content may be displayed incorrectly. "
                    + "Skip the error since it won't affect the main functionalities.");
        }
    }
}
