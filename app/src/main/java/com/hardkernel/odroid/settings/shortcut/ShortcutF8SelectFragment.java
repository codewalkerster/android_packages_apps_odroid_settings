package com.hardkernel.odroid.settings.shortcut;

import android.view.KeyEvent;

public class ShortcutF8SelectFragment extends ShortcutSelectFragment {
    public static ShortcutF8SelectFragment newInstance() {
        return new ShortcutF8SelectFragment();
    }

    public ShortcutF8SelectFragment() {
        keycode = KeyEvent.KEYCODE_F8;
    }
}
