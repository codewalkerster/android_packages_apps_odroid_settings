package com.hardkernel.odroid.settings.shortcut;

import android.view.KeyEvent;

public class ShortcutF10SelectFragment extends ShortcutSelectFragment {
    public static ShortcutF10SelectFragment newInstance() {
        return new ShortcutF10SelectFragment();
    }

    public ShortcutF10SelectFragment() {
        keycode = KeyEvent.KEYCODE_F10;
    }
}
