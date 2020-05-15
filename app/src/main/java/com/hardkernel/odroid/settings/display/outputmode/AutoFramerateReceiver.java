package com.hardkernel.odroid.settings.display.outputmode;

public class AutoFramerateReceiver {
    public static void onReceive() {
        AutoFramerateManager manager = AutoFramerateManager.getManager();
        manager.autoApply();
    }
}
