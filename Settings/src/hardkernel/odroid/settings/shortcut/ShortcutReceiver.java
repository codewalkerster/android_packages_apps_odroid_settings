package hardkernel.odroid.settings.shortcut;

import android.content.Context;

public class ShortcutReceiver {
    private final String TAG = "ShortcutReceiver";

    @Override
    public void onReceive(Context context) {
        ShortcutManager.initShortcuts(context);
    }
}
