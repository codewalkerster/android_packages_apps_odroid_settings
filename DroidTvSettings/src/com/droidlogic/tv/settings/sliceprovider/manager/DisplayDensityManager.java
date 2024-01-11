package com.droidlogic.tv.settings.sliceprovider.manager;

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

import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class DisplayDensityManager {


    private static final String TAG = DisplayDensityManager.class.getSimpleName();
    private DisplayManager mDisplayManager;
    private IWindowManager mWindowManagerService;
    private static final Integer MAX_HEIGHT_OF_UI = 1080;

    private static final ImmutableMap<Integer, Integer> PREFERRED_DPI_BY_DISPLAY_WIDTH =
            new ImmutableMap.Builder<Integer, Integer>()
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_1920, MediaSliceConstants.MEDIA_DISPLAY_DENSITY_HIGH)
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_1280, MediaSliceConstants.MEDIA_DISPLAY_DENSITY_MIDDLE)
                    .put(MediaSliceConstants.MEDIA_DISPLAY_RESOLUTION_720, Integer.valueOf((int) R.styleable.AppCompatTheme_windowFixedHeightMajor))
                    .build();


    public DisplayDensityManager(DisplayManager displayManager) {
        mDisplayManager = displayManager;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
    }

    public void adjustDisplayDensityByMode(Display.Mode mode) {
        //Display.Mode mode = mDisplayManager.getDisplay(displayId).getMode();
        final int displayId = mode.getModeId();
        logDebug(TAG, false, "according to the new display mode: " + mode);
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
                    logDebug(TAG, true, "Unexpected display width = " + mode.getPhysicalWidth()
                            + ", change the DPI to " + density);
                }
            }
            logDebug(TAG, true, "density: " + density);
            // A user id constant to indicate the "owner" user of the device.
            mWindowManagerService.setForcedDisplayDensityForUser(Display.DEFAULT_DISPLAY, density, UserHandle.USER_CURRENT);
        } catch (RemoteException e) {
            logDebug(TAG, true, "Cannot change the display density.The content may be displayed incorrectly. "
                    + "Skip the error since it won't affect the main functionalities.");
        }
    }
}
