package com.hardkernel.odroid.settings.dtbo;

import com.hardkernel.odroid.settings.ConfigEnv;

public class Overlay {
    public static String get() {
        return ConfigEnv.getOverlay();
    }

    public static void set(String overlay) {
        ConfigEnv.setOverlay(overlay);
    }

    public static String getSize() {
        return ConfigEnv.getOverlaySize();
    }

    public static void setSize(String size) {
        ConfigEnv.setOverlaySize(size);
    }
}
