package com.hardkernel.odroid.settings.shortcut;

import android.content.Context;

public class ShortcutReceiver {
    private final String TAG = "ShortcutReceiver";

    public static void onReceive(Context context) {
        ShortcutManager.initShortcuts(context);
    }
}