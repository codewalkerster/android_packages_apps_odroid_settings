package com.hardkernel.odroid.settings.shortcut;

import android.view.KeyEvent;

public class ShortcutF7SelectFragment extends ShortcutSelectFragment {
    public static ShortcutF7SelectFragment newInstance() {
        return new ShortcutF7SelectFragment();
    }

    public ShortcutF7SelectFragment() {
        keycode = KeyEvent.KEYCODE_F7;
    }
}
