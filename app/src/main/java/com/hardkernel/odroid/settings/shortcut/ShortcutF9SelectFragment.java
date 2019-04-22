package com.hardkernel.odroid.settings.shortcut;

import android.view.KeyEvent;

public class ShortcutF9SelectFragment extends ShortcutSelectFragment {
    public static ShortcutF9SelectFragment newInstance() {
        return new ShortcutF9SelectFragment();
    }

    public ShortcutF9SelectFragment() {
        keycode = KeyEvent.KEYCODE_F9;
    }
}
